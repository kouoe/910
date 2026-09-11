# API 接口文档

> [← 返回 README](../README.md)

服务默认监听 `http://localhost:8080`（可在 `application.yml` 中修改 `server.port`）。

## 统一响应结构

所有接口返回同一结构：

```json
{
  "code": 0,
  "msg": "成功",
  "dataMap": {
    "response": "AI 回答内容"
  }
}
```

- `code = 0` 表示成功，非 0 为业务错误（见 `ErrorCode`）
- `dataMap` 承载业务数据，不同接口字段名不同

## 1. AI 客服对话

| 项目 | 值 |
| :--- | :--- |
| 方法 | `POST` |
| 路径 | `/ai/agent/chat` |
| 请求体 | `{"message": "用户问题"}` |
| 响应字段 | `dataMap.response` |

内部执行三级流程：敏感词过滤 → Milvus 向量检索 → LLM 生成。

```bash
curl -X POST http://localhost:8080/ai/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "怎么申请退货？"}'
```

## 2. FAQ 知识库管理

| 方法 | 路径 | 参数 | 说明 |
| :--- | :--- | :--- | :--- |
| `POST` | `/add` | 请求体 `Faq`（categoryId / question / answer / status） | 新增 FAQ，自动同步到 Milvus |
| `POST` | `/update` | 请求体 `Faq` | 更新 FAQ |
| `GET` | `/deleteFaq` | `faqId` | 删除 FAQ |
| `GET` | `/getFaqByPage/{page}/{size}` | 路径参数 | FAQ 分页查询 |
| `GET` | `/syncToMilvus` | 无 | MySQL → Milvus 全量同步 |

`categoryId` 取值：1-订单、2-支付、3-商品、4-账户、5-其他。

```bash
curl -X POST http://localhost:8080/add \
  -H "Content-Type: application/json" \
  -d '{"categoryId":3,"question":"支持七天无理由退货吗？","answer":"支持，签收后7天内可申请。","status":1}'
```

## 3. 商品

| 方法 | 路径 | 参数 | 说明 |
| :--- | :--- | :--- | :--- |
| `GET` | `/product/list` | 无 | 商品列表 |
| `GET` | `/product/hot` | `limit`（默认 6） | 热销排行 |

```bash
curl "http://localhost:8080/product/hot?limit=5"
```

## 4. 购物车

| 方法 | 路径 | 参数 | 说明 |
| :--- | :--- | :--- | :--- |
| `POST` | `/cart/add` | `userId`、`productNo`、`quantity`（默认 1） | 加入购物车 |
| `GET` | `/cart/list` | `userId` | 购物车列表（含商品名称与价格） |
| `POST` | `/cart/update` | `userId`、`productNo`、`quantity` | 修改数量 |
| `POST` | `/cart/remove` | `userId`、`productNo` | 删除条目 |

```bash
curl -X POST "http://localhost:8080/cart/add?userId=1&productNo=P001&quantity=2"
curl "http://localhost:8080/cart/list?userId=1"
```

## 5. 用户

| 方法 | 路径 | 请求体 | 说明 |
| :--- | :--- | :--- | :--- |
| `POST` | `/user/register` | `User`（username / password 等） | 注册，密码 SHA-256 加盐存储 |
| `POST` | `/user/login` | `User` | 登录 |

## 6. 敏感词管理

| 方法 | 路径 | 参数 | 说明 |
| :--- | :--- | :--- | :--- |
| `POST` | `/sensitiveWord/add` | 请求体 `SensitiveWord`（word / type） | 新增敏感词，`type` 取 `deny`（黑名单）或 `allow`（白名单） |
| `DELETE` | `/sensitiveWord/delete` | `id` | 删除敏感词 |
| `GET` | `/sensitiveWord/search` | `page`（默认 1）等 | 敏感词分页查询 |

## 内置测试页面

后端自带两个前端页面，启动后可直接访问，无需额外搭建前端工程：

- `http://localhost:8080/` —— AI 客服对话页
- `http://localhost:8080/faq-test.html` —— FAQ 管理调试页
