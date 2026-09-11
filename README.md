# 艾瑞电商 AI 客服助手 · E-commerce AI Customer Service Agent

[![Java CI](https://github.com/kouoe/910/actions/workflows/ci.yml/badge.svg)](https://github.com/kouoe/910/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)

基于 **Spring AI + Milvus** 的电商智能客服系统。核心是一条「三级问答流水线」：**敏感词检测 → Milvus 向量语义检索（RAG）→ LLM 生成回答**，并支持 Function Calling 实时查询订单与商品。仓库同时提供一套用于**自动生成配套实习报告（docx）**的 Python 工具。

![系统总体架构](docs/images/arch.png)

---

## ✨ 核心特性

- **三级问答流水线** — 敏感词拦截 → 向量召回 → LLM 生成，层层把关，杜绝幻觉与违规内容
- **RAG 知识库问答** — FAQ 以 MySQL 为主数据源、Milvus 为向量副本（`IVF_FLAT` + `COSINE`）；查不到就如实说查不到，不编造商品与价格
- **Function Calling** — AI 自主调用订单查询（按单号/按状态）、商品搜索、价格区间筛选、热销排行
- **多轮对话记忆** — 基于 Spring AI ChatMemory，支持上下文追问
- **敏感词黑白名单** — 基于 `sensitive-word`，数据存 MySQL，支持运行时热更新
- **链路追踪** — `TraceIdFilter` 为每次请求生成 traceId 并注入 MDC，日志全程可追踪
- **统一响应与全局异常** — `R` 统一返回体 + `GlobalExceptionHandler` 兜底
- **配套测试** — 核心模块均有 JUnit 5 测试类，CI 自动编译验证
- **开箱即用** — Docker 一键拉起 Milvus，SQL 脚本一键初始化业务库

---

## 🔄 智能问答处理流程

![智能问答流程](docs/images/flow.png)

---

## 🛠️ 技术栈

| 层次 | 技术 |
| :--- | :--- |
| 语言 / 框架 | Java 17 · Spring Boot 3.5.6 · Spring MVC |
| AI 能力 | Spring AI 1.0.0 · Spring AI Alibaba（DashScope：`qwen-plus` 对话 / `text-embedding-v2` 向量） |
| 向量数据库 | Milvus v3.0.1（Docker Standalone：etcd + MinIO） |
| 业务存储 | MySQL 8 · MyBatis · Druid 连接池 |
| 内容安全 | sensitive-word 0.21.0（黑白名单） |
| 构建 / CI | Maven · GitHub Actions |
| 报告工具 | Python 3（matplotlib + Pillow） |

---

## 📁 目录结构

```
910/
├── backend/                      # 后端主工程（Spring Boot）
│   ├── pom.xml
│   ├── sql/init.sql              # 数据库建表与演示数据
│   └── src/main/java/com/airi/ai/agent/
│       ├── controller/           # HTTP 接口
│       ├── service/              # 业务逻辑（三级问答核心）
│       ├── search/               # 向量检索策略
│       ├── data/                 # MySQL → Milvus 数据管道
│       ├── functioncall/         # 供大模型调用的工具
│       ├── prompt/               # 提示词模板管理
│       ├── mapper/               # MyBatis 数据访问
│       ├── pojo/ result/ exception/
│       └── resources/static/     # 内置对话与 FAQ 管理页面
├── docker/                       # 基础设施编排
│   ├── docker-compose.yml        # Milvus + etcd + MinIO
│   └── embedEtcd.yaml
├── docs/                         # 项目文档
│   ├── images/                   # 架构图 / 流程图 / E-R 图
│   ├── ARCHITECTURE.md           # 架构设计说明
│   ├── API.md                    # 接口文档
│   └── DATABASE.md               # 数据库设计
├── tools/report-generator/       # 实习报告生成工具（Python）
├── .github/workflows/ci.yml      # CI 自动构建
├── LICENSE
└── README.md
```

---

## 🚀 快速开始

### 前置要求

| 依赖 | 版本 | 用途 |
| :--- | :--- | :--- |
| JDK | 17+ | 运行后端 |
| Maven | 3.6+ | 构建 |
| Docker | 任意 | 运行 Milvus |
| MySQL | 8+ | 业务数据 |
| DashScope API Key | — | [阿里云百炼](https://bailian.console.aliyun.com/) 申请 |

### 1. 启动向量数据库（Milvus）

```bash
cd docker
docker compose up -d
docker ps          # 应看到 milvus-standalone / milvus-etcd / milvus-minio
```

Milvus 端口 `19530`，健康检查端口 `9091`，MinIO 控制台 `http://localhost:9001`。
数据卷写在 `docker/volumes/`，已被 `.gitignore` 忽略。

### 2. 初始化业务数据库

```bash
mysql -u root -p < backend/sql/init.sql
```

会创建 `ai-agent` 库及 6 张表，并灌入 FAQ、订单、商品、敏感词演示数据。

### 3. 配置环境变量

密钥**不写入代码**，通过环境变量注入。

Windows（PowerShell，配置后需重启终端 / IDEA）：

```powershell
setx DASHSCOPE_API_KEY "你的百炼APIKey"
setx DB_USERNAME "root"
setx DB_PASSWORD "你的MySQL密码"
```

macOS / Linux：

```bash
export DASHSCOPE_API_KEY="你的百炼APIKey"
export DB_USERNAME="root"
export DB_PASSWORD="你的MySQL密码"
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

看到 `Started SpringAiSxApplication` 即启动成功，服务监听 `http://localhost:8080`。

### 5. 验证

打开内置对话页 **http://localhost:8080/** ，或直接调用接口：

```bash
curl -X POST http://localhost:8080/ai/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "如何查看我的订单物流信息？"}'
```

返回示例：

```json
{"code":0,"msg":"成功","dataMap":{"response":"您可以在\"我的订单\"中找到对应订单，点击\"查看物流\"即可实时跟踪包裹位置。"}}
```

---

## 📖 文档索引

| 文档 | 内容 |
| :--- | :--- |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 分层设计、三级流水线、RAG 同步机制、非功能性设计 |
| [API.md](docs/API.md) | 全部接口清单、请求参数与 curl 示例 |
| [DATABASE.md](docs/DATABASE.md) | 表结构设计、字段说明与初始化方式 |
| [tools/report-generator/README.md](tools/report-generator/README.md) | 实习报告自动生成工具用法 |

---

## 🧪 测试

```bash
cd backend
mvn test
```

> 部分测试用例依赖本地 MySQL / Milvus 实例，CI 中该项配置为 `continue-on-error`，不会阻断构建。

## 📄 实习报告生成

```bash
cd tools/report-generator
pip install -r requirements.txt
python gen_images.py     # 生成五张配图
python gen_report3.py    # 生成完整 Word 报告
python verify.py         # 校验文档合法性
```

详见 [tools/report-generator/README.md](tools/report-generator/README.md)。

---

## 🔒 安全说明

- API Key、数据库账号密码均通过**环境变量**注入，`application.yml` 中只保留 `${VAR}` 占位符
- 请勿在代码中提交任何真实密钥；本地调试建议配合 `.env`（已被 `.gitignore` 忽略）
- Druid 监控页已关闭（`stat-view-servlet.enabled: false`），规避历史 CVE 未授权访问风险

## 📜 许可证

本项目基于 [MIT License](LICENSE) 开源。
