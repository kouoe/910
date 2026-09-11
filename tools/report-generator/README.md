# 实习报告生成工具

一套 Python 脚本，用于自动生成本项目配套的《综合创新实践实习报告》——包括五张技术配图与最终 Word 文档。

> 与主系统无运行时依赖，属于项目文档配套工具。

## 安装依赖

```bash
pip install -r requirements.txt
```

依赖仅为 `matplotlib`（绘图）与 `Pillow`（读取图片尺寸以排版）。

## 使用流程

### 1. 生成配图

```bash
python gen_images.py
```

生成五张图到仓库的 `docs/images/` 目录：

| 文件 | 内容 |
| :--- | :--- |
| `arch.png` | 系统总体架构图 |
| `flow.png` | 智能问答（RAG + Function Calling）流程图 |
| `er.png` | 数据库 E-R 图 |
| `modules.png` | 功能模块划分图 |
| `tech.png` | 技术栈选型图 |

### 2. 生成完整报告 Word 文档

```bash
python gen_report3.py
```

读取**桌面**上的 `综合创新实践实习报告模板.docx`，在保留封面、成绩评语等原有页面的基础上追加正文（标题、段落、代码块、表格、配图），输出到桌面的 `综合创新实践实习报告（完整版）.docx`。

> 报告正文 DOCTYPE 由脚本直接拼装 OOXML，因此需要模板文件存在，且图片已先由 `gen_images.py` 生成。

### 3. 校验生成结果

```bash
python verify.py
```

检查 `.docx` 内部结构是否合法：附带 `media` 资源、`document.xml` 是否可被 XML 解析器正常解析、表格与图片数量、图片关系是否正确等。

## 路径说明

脚本内的路径均为**运行时自动推导**，不含任何硬编码的用户目录：

- 仓库根目录：由脚本自身位置（`tools/report-generator/`）向上两级推导
- 图片输出目录：`<仓库根>/docs/images`
- 报告模板与输出：当前用户的桌面（`~/Desktop`）

因此在任何机器上克隆后均可直接运行，无需修改代码。Windows / macOS / Linux 通用。
