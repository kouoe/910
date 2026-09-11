# 变更日志 · Changelog

本项目所有重要变更都记录在此文件。格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 2026-09-11

首个正式版本发布。

### Added

- **AI 客服对话**：三级问答流水线（敏感词过滤 → Milvus 向量检索 → LLM 生成）
- **RAG 知识库**：FAQ 以 MySQL 为主数据源、Milvus 为向量副本，支持增量同步与全量重建
- **Function Calling**：订单查询（按单号 / 状态）、商品搜索、价格区间筛选、热销排行
- **多轮对话记忆**：基于 Spring AI ChatMemory
- **敏感词治理**：黑白名单存 MySQL，支持运行时热更新
- **链路追踪**：TraceIdFilter + MDC，日志贯穿 traceId
- **业务功能**：用户注册登录、商品浏览、购物车增删改查
- **内置页面**：对话页与 FAQ 管理调试页
- **基础设施编排**：Milvus + etcd + MinIO 一键拉起
- **报告生成工具**：自动生成配图与完整实习报告 Word 文档
- **文档体系**：架构设计、API 文档、数据库设计、贡献指南、安全策略
- **CI**：GitHub Actions 自动编译构建；Dependabot 每周检查依赖更新

### Changed

- 仓库结构重组为 `backend/` `docker/` `docs/` `tools/` 四类目录
- 全部敏感配置改为环境变量注入，代码中不再出现任何明文密钥
- Python 工具脚本改为运行时推导路径，可在任意机器直接运行

### Removed

- 移除旧版报告生成脚本与冗余的空配置文件

### Security

- 关闭 Druid 监控页，规避历史 CVE 未授权访问风险
- 清理 Git 历史中含真实 API Key 的提交记录
