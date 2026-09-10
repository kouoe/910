# -*- coding: utf-8 -*-
"""生成实习报告配图（matplotlib + 中文字体）"""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle
import os

plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei"]
plt.rcParams["axes.unicode_minus"] = False

OUT_DIR = r"d:\910\report_img"
os.makedirs(OUT_DIR, exist_ok=True)

GOLD = "#B8860B"
DARK = "#1F1F1F"
LIGHT = "#F5F1E6"
GREY = "#3A3A3A"

def box(ax, x, y, w, h, text, fc, ec, tc, fs=10, bold=True, lw=1.3):
    p = FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.015,rounding_size=0.025",
                       fc=fc, ec=ec, lw=lw)
    ax.add_patch(p)
    ax.text(x + w/2, y + h/2, text, ha="center", va="center",
            fontsize=fs, color=tc, fontweight="bold" if bold else "normal")

def arrow(ax, x1, y1, x2, y2, color=GOLD, lw=1.6):
    ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle="-|>", color=color, lw=lw))

def clean(ax, xlim, ylim):
    ax.set_xlim(*xlim)
    ax.set_ylim(*ylim)
    ax.axis("off")

# ---------------------------------------------------------------------------
# 图1 系统总体架构图
# ---------------------------------------------------------------------------
def arch():
    fig, ax = plt.subplots(figsize=(10, 7.2), dpi=200)
    clean(ax, (0, 10), (0, 7.4))
    ax.text(5, 7.05, "基于 Spring AI 的电商智能客服系统 · 总体架构",
            ha="center", va="center", fontsize=16, color=DARK, fontweight="bold")

    layers = [
        ("表现层（Web）", 6.05, ["商城前端", "AiAgentController", "ProductController", "UserController", "CartController", "FaqController"]),
        ("业务服务层", 4.65, ["AiAgentService", "ProductService", "OrderService", "FaqService"]),
        ("AI 能力层", 3.25, ["ChatClient", "VectorSearchService", "PromptTemplateManager", "FaqDataPipelineService", "SensitiveWordBs"]),
        ("数据访问层", 1.85, ["MyBatis Mapper → MySQL", "VectorStore → Milvus"]),
    ]
    layer_h = 1.15
    for name, y, items in layers:
        box(ax, 0.2, y, 1.9, layer_h, name, GOLD, GOLD, "white", fs=12)
        n = len(items)
        gap = 0.18
        x0 = 2.4
        total_w = 7.4
        w = (total_w - gap * (n - 1)) / n
        for i, it in enumerate(items):
            x = x0 + i * (w + gap)
            box(ax, x, y, w, layer_h, it, "white", GREY, DARK, fs=10.5)

    for i in range(3):
        y1 = 6.05 - i * 1.4
        y2 = y1 - 0.25
        arrow(ax, 5, y1, 5, y2)
    plt.tight_layout()
    plt.savefig(os.path.join(OUT_DIR, "arch.png"), bbox_inches="tight", facecolor="white")
    plt.close()

# ---------------------------------------------------------------------------
# 图2 智能问答流程图
# ---------------------------------------------------------------------------
def flow():
    fig, ax = plt.subplots(figsize=(9, 8.5), dpi=200)
    clean(ax, (0, 10), (0, 9.2))
    ax.text(5, 8.9, "智能问答（RAG + Function Calling）处理流程",
            ha="center", va="center", fontsize=15, color=DARK, fontweight="bold")

    def node(x, y, w, h, text, fc=LIGHT, tc=DARK, fs=10.5, ec=GREY):
        box(ax, x, y, w, h, text, fc, ec, tc, fs=fs)

    # 主流程
    node(3.4, 8.0, 3.2, 0.55, "用户输入问题", fc="white")
    node(3.4, 7.0, 3.2, 0.55, "敏感词检测", fc="white")
    node(3.4, 6.0, 3.2, 0.55, "向量检索 FAQ 知识库", fc="white")
    node(3.4, 5.0, 3.2, 0.55, "是否命中 FAQ？", fc="#FFF4D6", ec=GOLD)
    node(1.4, 3.6, 2.6, 0.55, "返回标准答案", fc="white")
    node(5.6, 3.6, 3.0, 0.55, "构建知识上下文", fc="white")
    node(5.6, 2.6, 3.0, 0.55, "渲染提示词模板", fc="white")
    node(5.6, 1.6, 3.0, 0.55, "调用大模型生成回答\n（按需调用工具）", fc="white")
    node(3.4, 0.5, 3.2, 0.55, "返回回答给用户", fc=DARK, tc="white", ec=DARK)

    arrow(ax, 5, 8.0, 5, 7.55)
    arrow(ax, 5, 7.0, 5, 6.55)
    arrow(ax, 5, 6.0, 5, 5.55)
    # 命中 -> 是(左) 否(右)
    ax.text(4.0, 4.75, "是", fontsize=11, color=GOLD, fontweight="bold")
    ax.text(6.6, 4.75, "否", fontsize=11, color=GOLD, fontweight="bold")
    arrow(ax, 4.5, 5.0, 2.7, 4.15)
    arrow(ax, 5.5, 5.0, 7.1, 4.15)
    arrow(ax, 2.7, 3.6, 4.3, 1.05)   # 标准答案 -> 返回
    arrow(ax, 7.1, 3.6, 7.1, 3.15)
    arrow(ax, 7.1, 2.6, 7.1, 2.15)
    arrow(ax, 7.1, 1.6, 5.6, 1.05)   # 生成回答 -> 返回

    plt.tight_layout()
    plt.savefig(os.path.join(OUT_DIR, "flow.png"), bbox_inches="tight", facecolor="white")
    plt.close()

# ---------------------------------------------------------------------------
# 图3 数据库 ER 图
# ---------------------------------------------------------------------------
def er():
    fig, ax = plt.subplots(figsize=(10, 7.5), dpi=200)
    clean(ax, (0, 10), (0, 8))
    ax.text(5, 7.7, "核心数据表关系（E-R 简图）",
            ha="center", va="center", fontsize=15, color=DARK, fontweight="bold")

    tables = {
        "t_user\n用户表": {"pos": (0.5, 2.5), "fields": ["user_id (PK)", "username", "password", "phone"]},
        "t_product\n商品表": {"pos": (3.0, 5.6), "fields": ["product_id (PK)", "name", "price", "category", "sales", "stock"]},
        "t_cart\n购物车表": {"pos": (3.0, 2.5), "fields": ["cart_id (PK)", "user_id (FK)", "product_id (FK)", "quantity"]},
        "t_order\n订单表": {"pos": (6.0, 5.6), "fields": ["order_id (PK)", "order_no", "user_id (FK)", "product_id (FK)", "amount", "status"]},
        "t_faq\n知识库表": {"pos": (7.6, 2.5), "fields": ["faq_id (PK)", "question", "answer", "category_id"]},
        "t_sensitive_word\n敏感词表": {"pos": (0.5, 5.6), "fields": ["id (PK)", "word"]},
    }

    for name, info in tables.items():
        x, y = info["pos"]
        box(ax, x, y, 2.6, 1.5, name, GOLD, GOLD, "white", fs=11)
        # 字段区
        ax.add_patch(Rectangle((x, y - 0.6), 2.6, 0.6, fc="white", ec=GREY, lw=1))
        ax.text(x + 1.3, y - 0.3, "\n".join(info["fields"]),
                ha="center", va="center", fontsize=7.5, color=DARK)

    # 关系标注
    rel = [
        ((1.8, 4.1), (2.6, 4.1), "1:N"),
        ((3.1, 5.6), (3.1, 4.0), "1:N"),
        ((4.3, 4.0), (7.3, 4.0), "1:N"),
    ]
    for (x1, y1), (x2, y2), label in rel:
        arrow(ax, x1, y1, x2, y2)
        ax.text((x1+x2)/2, (y1+y2)/2 + 0.12, label, fontsize=9, color=GOLD, ha="center", fontweight="bold")

    # t_user 与 t_cart/t_order
    arrow(ax, 1.8, 3.1, 2.6, 3.1)  # user -> cart
    arrow(ax, 1.8, 4.1, 2.6, 4.1)  # user -> product? 
    # 简化：标注文字
    ax.text(1.4, 3.0, "1:N", fontsize=9, color=GOLD, fontweight="bold", ha="center")

    plt.tight_layout()
    plt.savefig(os.path.join(OUT_DIR, "er.png"), bbox_inches="tight", facecolor="white")
    plt.close()

# ---------------------------------------------------------------------------
# 图4 功能模块图
# ---------------------------------------------------------------------------
def modules():
    fig, ax = plt.subplots(figsize=(10, 7), dpi=200)
    clean(ax, (0, 10), (0, 7.4))
    ax.text(5, 7.0, "系统功能模块划分",
            ha="center", va="center", fontsize=15, color=DARK, fontweight="bold")

    box(ax, 3.6, 3.3, 2.8, 0.9, "电商智能客服系统", DARK, DARK, "white", fs=13)

    mods = [
        (0.3, 5.6, "商品管理", ["列表/详情", "分类查询", "热销查询", "价格查询"]),
        (2.6, 5.6, "用户管理", ["注册", "登录"]),
        (4.9, 5.6, "购物车", ["加入购物车", "查看购物车", "修改数量", "删除"]),
        (7.2, 5.6, "订单查询", ["订单号查询", "状态查询"]),
        (0.3, 2.2, "FAQ 知识库", ["增删改查", "向量同步"]),
        (2.6, 2.2, "智能问答", ["向量检索", "提示词渲染", "大模型生成"]),
        (4.9, 2.2, "Agent 工具", ["商品/订单工具", "Function Calling"]),
        (7.2, 2.2, "内容安全", ["敏感词过滤"]),
    ]
    for x, y, title, items in mods:
        box(ax, x, y, 2.2, 0.85, title, LIGHT, GREY, DARK, fs=11)
        ax.text(x + 1.1, y - 0.32, " / ".join(items), ha="center", va="center",
                fontsize=8, color=GREY)

    # 中心到模块的连线
    for x, y, _, _ in mods:
        arrow(ax, 5.0, 3.3, x + 1.1, y + 0.85)

    plt.tight_layout()
    plt.savefig(os.path.join(OUT_DIR, "modules.png"), bbox_inches="tight", facecolor="white")
    plt.close()

# ---------------------------------------------------------------------------
# 图5 技术栈图
# ---------------------------------------------------------------------------
def tech():
    fig, ax = plt.subplots(figsize=(10, 6.5), dpi=200)
    clean(ax, (0, 10), (0, 6.6))
    ax.text(5, 6.2, "技术栈总览",
            ha="center", va="center", fontsize=15, color=DARK, fontweight="bold")

    groups = [
        ("后端框架", ["Spring Boot 3.5.6", "Spring MVC", "MyBatis", "Druid"], 0.4, 3.6),
        ("AI 能力", ["Spring AI Alibaba", "通义千问 qwen-plus", "text-embedding-v2", "Function Calling"], 2.7, 3.6),
        ("数据存储", ["MySQL 8", "Milvus 2.4.4", "向量检索", "敏感词库"], 5.0, 3.6),
        ("前端", ["HTML", "CSS", "JavaScript"], 7.3, 3.6),
        ("工程化", ["Java 17", "Maven", "JUnit 5", "Mockito", "Docker"], 1.5, 1.2),
    ]
    for title, items, x, y in groups:
        box(ax, x, y + 0.9, 2.2, 0.7, title, GOLD, GOLD, "white", fs=12)
        ax.text(x + 1.1, y + 0.05, "\n".join(items), ha="center", va="center",
                fontsize=9.5, color=DARK)

    plt.tight_layout()
    plt.savefig(os.path.join(OUT_DIR, "tech.png"), bbox_inches="tight", facecolor="white")
    plt.close()

if __name__ == "__main__":
    arch()
    flow()
    er()
    modules()
    tech()
    for f in os.listdir(OUT_DIR):
        print(f, os.path.getsize(os.path.join(OUT_DIR, f)))
