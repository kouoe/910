# 系统架构设计

> [← 返回 README](../README.md)

## 1. 总体分层

![系统总体架构](images/arch.png)

系统自上而下分为四层，每层职责单一、依赖单向向下：

| 层次 | 职责 | 关键实现 |
| :--- | :--- | :--- |
| **表现层** | HTTP 接口入口、静态测试页面 | `AiAgentController`、`FaqController`、`ProductController`、`CartController`、`UserController`、`SensitiveWordController`；`src/main/resources/static/index.html` 提供简易对话页 |
| **业务服务层** | 业务编排、事务边界 | `AiAgentServiceImpl`、`FaqServiceImpl`、`SensitiveWordServiceImpl` |
| **AI 能力层** | 大模型调用、向量检索、提示词管理 | `ChatClient`、`VectorSearchService`、`PromptTemplateManager`、`FaqDataPipelineService`、`SensitiveWordBs` |
| **数据访问层** | 结构化持久化与语义检索 | MyBatis Mapper → MySQL；Spring AI `VectorStore` → Milvus |

源码包结构与之对应：

```
com.airi.ai.agent
├── config        # AppConfig / SensitiveWordConfig / TraceIdFilter
├── controller    # HTTP 入口
├── service       # 业务接口与实现
├── data          # FAQ 数据管道（MySQL → Milvus）
├── search        # 向量检索策略
├── functioncall  # 供大模型调用的工具
├── prompt        # 系统提示词模板管理
├── mapper        # MyBatis 数据访问
├── pojo          # 数据实体
├── result        # 统一响应 R / ErrorCode
└── exception     # 业务异常与全局处理
```

## 2. 三级问答流水线

核心对话接口 `POST /ai/agent/chat` 在 `AiAgentServiceImpl.chat()` 中执行三级处理：

![智能问答流程](images/flow.png)

1. **敏感词前置过滤** — `SensitiveWordBs` 依据 MySQL 黑白名单表拦截违规输入（黑名单 deny / 白名单 allow 可运行时维护）。
2. **Milvus 语义检索（RAG）** — `VectorSearchService` 将用户问题向量化后在 Milvus 中检索 TopK 相似 FAQ 片段，拼装为系统提示词中的 `{knowledge}` 上下文。
3. **LLM 生成回答** — Spring AI `ChatClient` 调用 `qwen-plus` 模型，在严格的提示词约束下生成自然语言回答。

> **防幻觉约束**：系统提示词明确规定，知识库无命中或与问题无关时，必须如实回复"目前没有查到相关信息，建议联系人工客服"，严禁编造商品、价格、库存与活动信息。这是客服类问答系统可用性的关键保障。

## 3. RAG 知识库同步机制

MySQL 是 FAQ 的主数据源，Milvus 保存其向量副本：

- **增量同步**：新增 / 更新 FAQ 时由 `FaqServiceImpl` 自动写入 Milvus。
- **全量重建**：调用 `GET /syncToMilvus` 触发 MySQL → Milvus 全量刷新。
- **检索配置**：Embedding 模型 `text-embedding-v2`，维度 1536；索引 `IVF_FLAT`，度量 `COSINE`，集合名 `common_response`（见 `backend/src/main/resources/application.yml`）。

## 4. Function Calling（工具调用）

大模型可自主决策调用的本地业务工具，使回答基于真实数据而非编造：

| 工具服务 | 能力 |
| :--- | :--- |
| `OrderService` | 按订单号、按订单状态（待支付 / 待发货 / 配送中 / 已签收 / 已退货）查询订单 |
| `ProductService` | 商品搜索、价格查询、分类查询、价格区间筛选、热销排行 |

工具返回结果回灌大模型后，由模型组织成自然语言答复用户。

## 5. 非功能性设计

| 关注点 | 实现方式 |
| :--- | :--- |
| 链路追踪 | `TraceIdFilter` 生成 TraceId 并写入 MDC，日志 pattern 输出 `%X{traceId}`，便于串联一次请求的全部日志 |
| 统一响应 | `R` + `ErrorCode` + `GlobalExceptionHandler`，所有接口返回结构一致 |
| 连接池 | Druid；已关闭 `stat-view-servlet` 监控页，规避历史 CVE 未授权访问风险 |
| 失败扭矩 | DashScope 读超时放宽至 120s，缓解长文本生成超时 |
| 配置外置 | 密钥与数据库凭据通过环境变量注入，不落库到代码 |

## 6. 技术选型

| 领域 | 选型 | 说明 |
| :--- | :--- | :--- |
| 基础框架 | Spring Boot 3.5.6 / Java 17 | 虚拟线程与生态兼容性的平衡选择 |
| AI 框架 | Spring AI 1.0.0 + Spring AI Alibaba 1.0.0.2 | 统一 ChatClient / VectorStore 抽象，屏蔽底层模型差异 |
| 大模型 | DashScope `qwen-plus` | 中文客服场景性价比高 |
| Embedding | DashScope `text-embedding-v2` | 1536 维，与 Milvus 集合配置匹配 |
| 向量库 | Milvus 3.0.1 | 支持 IVF_FLAT / COSINE，适合中小规模知识库 |
| ORM | MyBatis 3.0.3 + Druid 1.2.27 | 手写 SQL 便于控制复杂查询 |
| 敏感词 | houbb `sensitive-word` 0.21.0 | 支持黑白名单动态配置 |
| 构建 | Maven | CI 中自动编译与测试 |
