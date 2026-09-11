# 贡献指南 · Contributing

感谢你愿意为这个项目添砖加瓦 🙌

## 本地开发准备

1. Fork 本仓库并克隆到本地
2. 安装 JDK 17、Maven 3.6+、Docker、MySQL 8
3. 启动依赖服务：

   ```bash
   cd docker && docker compose up -d
   mysql -u root -p < backend/sql/init.sql
   ```

4. 配置环境变量（`DASHSCOPE_API_KEY` / `DB_USERNAME` / `DB_PASSWORD`），详见 [README](README.md)
5. 启动：

   ```bash
   cd backend && mvn spring-boot:run
   ```

## 分支命名

| 前缀 | 用途 |
| :--- | :--- |
| `feature/` | 新功能 |
| `fix/` | 缺陷修复 |
| `refactor/` | 重构 |
| `docs/` | 文档 |
| `chore/` | 构建、依赖、CI 杂项 |

示例：`feature/faq-batch-import`、`fix/milvus-sync-npe`

## Commit Message 规范

本项目采用 **Conventional Commits**，**提交信息一律使用英文**，格式：

```
<type>(<optional scope>): <description>

[optional body]
```

### type 取值

| 类型 | 含义 |
| :--- | :--- |
| `feat` | 新增功能 |
| `fix` | 修复缺陷 |
| `docs` | 仅文档变更 |
| `style` | 格式调整（不影响逻辑） |
| `refactor` | 重构（既不加功能也不修缺陷） |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `build` | 构建与依赖 |
| `ci` | CI 配置 |
| `chore` | 其他杂项 |

### 示例

```text
feat(chat): support streaming response for AI agent
fix(milvus): handle empty vector search result
docs(api): add pagination params to FAQ endpoints
refactor(repo): reorganize project into backend/docker/docs/tools
```

> 正文每行不超过 72 字符，用现在时祈使句（"add" 而非 "added"），结尾不加句号。

## Pull Request 流程

1. 从 `main` 切出分支开发
2. 确保本地编译与测试通过：`mvn clean package`
3. 提交 PR，**不要**在代码中包含任何密钥或个人信息
4. 在描述中说明动机与变更范围，并关联对应 Issue
5. CI 通过后等待 Code Review

## 代码风格

- Java：使用构造器注入而非字段注入，统一返回 `R` 包装对象
- 每个对外方法写清 Javadoc
- 新增功能请附带单元测试
- 敏感配置禁止硬编码，统一通过环境变量注入

## 安全

发现安全相关问题请先阅读 [SECURITY.md](SECURITY.md)，不要公开提交 Issue。
