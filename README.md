# 艾瑞电商 AI 客服助手 · E-commerce AI Customer Service Agent

基于 **Spring AI + Milvus** 的电商智能客服系统。核心能力是「三级问答流水线」：**敏感词检测 → Milvus 向量语义检索（RAG）→ LLM 生成回答**，并支持 Function Calling 实时查询订单/商品。仓库同时包含一套用于**自动生成配套实习报告（docx）**的 Python 工具。

![系统总体架构](report_img/arch.png)

---

## ✨ 核心特性

- **三级问答流水线**：敏感词拦截 → 向量召回 → LLM 生成，层层把关，杜绝幻觉与违规内容
- **RAG 知识库问答**：FAQ 以 MySQL 为主数据源，全量同步至 Milvus（`IVF_FLAT` + `COSINE`），回答严格基于知识库，查不到就如实说查不到
- **Function Calling**：AI 可自主调用订单查询（按单号/按状态）、商品搜索/价格区间/热销排行等工具函数
- **多轮对话记忆**：基于 Spring AI ChatMemory，支持上下文追问
- **敏感词黑白名单**：基于 `sensitive-word` 库，支持动态增删，可从 MySQL 热加载
- **链路追踪**：`TraceIdFilter` 为每次请求生成 traceId 并注入 MDC，日志全程可追踪
- **统一响应与全局异常**：`R` 统一返回体 + `GlobalExceptionHandler` 兜底
- **配套测试**：核心模块均有 JUnit 5 测试类

## 🔄 智能问答处理流程

![智能问答流程](report_img/flow.png)

## 🛠️ 技术栈

| 层次 | 技术 |
|------|------|
| 语言/框架 | Java 17 · Spring Boot 3.5.6 · Spring MVC |
| AI 能力 | Spring AI 1.0.0 · Spring AI Alibaba（DashScope：qwen-plus 对话 / text-embedding-v2 向量） |
| 向量数据库 | Milvus v3.0.1（Docker Standalone：etcd + MinIO） |
| 业务存储 | MySQL 8 · MyBatis · Druid 连接池 |
| 内容安全 | sensitive-word 0.21.0（黑白名单） |
| 构建工具 | Maven |
| 报告工具 | Python 3（matplotlib + Pillow） |

## 📁 目录结构

```
910/
├── spring-ai-sx/                 # 后端主工程（艾瑞电商 AI 客服）
│   ├── pom.xml                   # Maven 依赖定义
│   ├── sql/
│   │   └── init.sql              # MySQL 初始化脚本（建库 + FAQ/敏感词/订单初始数据）
│   └── src/
│       ├── main/
│       │   ├── java/com/airi/ai/agent/
│       │   │   ├── SpringAiSxApplication.java   # 启动类
│       │   │   ├── config/       # 配置（AppConfig、SensitiveWordConfig、TraceIdFilter）
│       │   │   ├── controller/   # 接口层（AI 对话、FAQ、商品、购物车、用户、敏感词）
│       │   │   ├── service/      # 业务层（AiAgentService 三级问答核心逻辑）
│       │   │   ├── search/       # Milvus 向量检索（VectorSearchService、SearchStrategy）
│       │   │   ├── functioncall/ # Function Calling 工具（OrderService、ProductService）
│       │   │   ├── data/         # FAQ 数据管道（MySQL → Milvus 同步）
│       │   │   ├── prompt/       # 提示词模板管理（PromptTemplateManager）
│       │   │   ├── mapper/       # MyBatis Mapper
│       │   │   ├── pojo/         # 实体类（Faq、Order、Product、User、CartItem…）
│       │   │   ├── result/       # 统一响应 R、错误码 ErrorCode
│       │   │   └── exception/    # 全局异常处理
│       │   └── resources/
│       │       └── application.yml   # 配置文件（密钥经环境变量注入）
│       └── test/                 # JUnit 5 单元测试
├── docker-compose.yml            # Milvus Standalone 一键部署（etcd + MinIO + Milvus）
├── report_img/                   # 报告配图（架构图/流程图/ER 图/模块图/技术选型图）
├── gen_images.py                 # matplotlib 生成报告配图
├── gen_report3.py                # 基于学校 Word 模板生成完整实习报告（docx）
├── verify.py                     # 校验生成的 docx 结构合法性
└── requirements.txt              # Python 工具依赖
```

## 🚀 快速开始

### 前置要求

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.6+ | 构建工具 |
| Docker | — | 用于启动 Milvus |
| MySQL | 8.x | 业务数据库 |
| DashScope API Key | — | [阿里云百炼平台](https://bailian.console.aliyun.com/)开通后获取 |

### 1. 启动 Milvus 向量数据库

```bash
docker compose up -d
# 等待约 1 分钟，检查健康状态
docker ps   # 三个容器（etcd / minio / milvus-standalone）均为 healthy 即可
```

### 2. 初始化 MySQL 业务库

```bash
mysql -h 127.0.0.1 -P 3307 -u root -p < spring-ai-sx/sql/init.sql
```

> 脚本会创建 `ai-agent` 库，并写入 FAQ（8 条）、敏感词（4 条）、订单（6 条）初始数据。
> 默认连接 `127.0.0.1:3307`，如你的 MySQL 在别的主机/端口，请修改
> `spring-ai-sx/src/main/resources/application.yml` 中的 `spring.datasource.url`。

### 3. 配置环境变量

密钥一律通过环境变量注入，**不要写进代码**：

```bash
# Linux / macOS
export DASHSCOPE_API_KEY=sk-你的DashScope密钥
export DB_USERNAME=root
export DB_PASSWORD=你的数据库密码

# Windows PowerShell
$env:DASHSCOPE_API_KEY="sk-你的DashScope密钥"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
```

### 4. 启动后端

```bash
cd spring-ai-sx
mvn spring-boot:run
# 看到 Started SpringAiSxApplication 即启动成功，监听 8080 端口
```

### 5. 发起一次 AI 对话

```bash
curl -X POST http://localhost:8080/ai/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "运费怎么算？"}'
```

返回示例：

```json
{
  "code": 0,
  "msg": "成功",
  "dataMap": {
    "response": "单笔订单满99元包邮，不满99元收取8元基础运费。"
  }
}
```

> FAQ 命中：该问题来自知识库语义召回，返回标准答案。

## 🔌 API 一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/agent/chat` | **AI 智能对话**（三级问答核心接口），body: `{"message": "..."}` |
| POST | `/add` | 新增 FAQ（自动同步到 Milvus） |
| GET  | `/deleteFaq` | 删除 FAQ |
| POST | `/update` | 更新 FAQ |
| GET  | `/getFaqByPage/{page}/{size}` | FAQ 分页查询 |
| GET  | `/syncToMilvus` | MySQL → Milvus 全量同步 |
| GET  | `/product/list` · `/product/hot` | 商品列表 / 热销排行 |
| POST | `/cart/add` · `/cart/update` · `/cart/remove` | 购物车增改删 |
| GET  | `/cart/list` | 购物车列表 |
| POST | `/user/register` · `/user/login` | 用户注册 / 登录 |
| POST | `/sensitiveWord/add` · DELETE `/sensitiveWord/delete` · GET `/sensitiveWord/search` | 敏感词管理 |

## 🧪 运行测试

```bash
cd spring-ai-sx
mvn test
```

覆盖范围：敏感词配置、FAQ 数据管道、AI 对话接口、Function Calling 订单服务、提示词模板、统一响应与异常等核心模块。

## 📄 实习报告生成工具（Python）

配套学校模板自动生成本项目的实习报告文档：

```bash
pip install -r requirements.txt

python gen_images.py     # 生成 5 张报告配图 → report_img/
python gen_report3.py    # 基于学校 docx 模板生成完整报告（正文 + 配图 + 表格）
python verify.py         # 校验生成的 docx 结构合法性
```

> `gen_report3.py` 中报告模板路径默认为桌面，可按需修改文件顶部的 `SRC` / `DST` 常量。

## ⚠️ 安全说明

- 所有敏感配置（DashScope API Key、数据库密码）均通过**环境变量**注入，仓库中不含任何真实密钥。
- **请勿**将真实 API Key 提交到 Git 仓库；如不慎泄露，请立即前往[百炼控制台](https://bailian.console.aliyun.com/)重置密钥。
