# -*- coding: utf-8 -*-
"""生成实习报告 docx（仅用标准库 zipfile）"""
import zipfile, html, os

OUT = r"d:\910\spring-ai-sx\实习报告.docx"

# ---------------------------------------------------------------------------
# 报告正文内容：(样式, 文本) 列表
# 样式: title / h1 / h2 / p / bullet / code
# ---------------------------------------------------------------------------
C = []
C.append(("title", "基于 Spring AI 的电商智能客服系统"))
C.append(("subtitle", "实习报告"))
C.append(("p", ""))

C.append(("h1", "1. 需求分析"))
C.append(("h2", "1.1 项目背景"))
C.append(("p", "传统电商客服依赖人工重复回答大量常见问题（商品信息、价格、订单状态、售后政策等），"
            "存在响应慢、人力成本高、知识更新不及时、服务时间受限等问题。随着大语言模型（LLM）与检索增强生成（RAG）"
            "技术的成熟，可以构建一套智能客服系统，让用户通过自然语言即可完成咨询与自助服务。"))
C.append(("p", "本项目基于 Spring Boot 与 Spring AI Alibaba（对接阿里云通义千问）构建一个电商智能客服系统，"
            "在提供商品展示、登录注册、购物车等基础商城能力的同时，通过 Agent 工具调用与向量知识库检索，"
            "实现自然语言的商品查询、价格查询、热销查询、订单查询等智能问答能力。"))

C.append(("h2", "1.2 功能需求"))
C.append(("bullet", "商品信息展示：浏览商品列表、查看商品详情、按分类查看热销商品。"))
C.append(("bullet", "用户管理：用户注册、登录。"))
C.append(("bullet", "购物车：加入购物车、查看购物车、修改数量、删除商品。"))
C.append(("bullet", "FAQ 知识库：常见问题的增删改查，并同步到向量库。"))
C.append(("bullet", "智能问答：基于 RAG 检索 FAQ 知识库并结合大模型生成回答。"))
C.append(("bullet", "Agent 工具调用：商品查询、价格查询、热销查询、订单编号查询、订单状态查询等。"))
C.append(("bullet", "内容安全：敏感词过滤，拦截不当提问。"))

C.append(("h2", "1.3 非功能需求"))
C.append(("bullet", "可扩展性：提示词模板化，业务工具可插拔。"))
C.append(("bullet", "可维护性：数据流水线统一处理 MySQL 与向量库的同步。"))
C.append(("bullet", "可靠性：向量检索失败时降级到直接生成回答。"))
C.append(("bullet", "可观测性：使用 TraceId 进行链路追踪。"))

C.append(("h1", "2. 软件设计"))
C.append(("h2", "2.1 总体架构"))
C.append(("p", "系统采用经典的三层架构（Controller → Service → Mapper），并结合 Spring AI 的"
            "ChatClient、VectorStore、ToolCallingChatOptions 等抽象，形成「Web 层 + 业务服务层 + 数据访问层 + AI 能力层」的整体结构。"))
C.append(("bullet", "Web 层：Spring MVC 控制器（AiAgentController、ProductController、UserController、CartController、FaqController）。"))
C.append(("bullet", "业务服务层：AiAgentService、ProductService、OrderService、FaqService 等。"))
C.append(("bullet", "AI 能力层：ChatClient（大模型对话）、VectorSearchService（向量检索）、PromptTemplateManager（提示词模板）、"
            "FaqDataPipelineService（数据流水线）、SensitiveWordBs（敏感词过滤）。"))
C.append(("bullet", "数据访问层：MyBatis Mapper 访问 MySQL；VectorStore 访问 Milvus 向量数据库。"))

C.append(("h2", "2.2 数据库设计"))
C.append(("p", "使用 MySQL 8 存储业务数据，主要数据表如下："))
C.append(("bullet", "t_user：用户表（用户名、密码、手机号等）。"))
C.append(("bullet", "t_product：商品表（商品名、价格、分类、销量、库存等）。"))
C.append(("bullet", "t_cart：购物车表（用户、商品、数量）。"))
C.append(("bullet", "t_order：订单表（订单编号、用户、商品、金额、状态）。"))
C.append(("bullet", "t_faq：FAQ 知识库表（问题、答案、分类）。"))
C.append(("bullet", "t_sensitive_word：敏感词表。"))
C.append(("p", "其中 FAQ 数据同时以向量形式存储在 Milvus 中，用于语义检索。"))

C.append(("h2", "2.3 关键设计思想"))
C.append(("bullet", "RAG 检索增强生成：先将 FAQ 向量化存入 Milvus，提问时先做相似度检索，命中则直接返回标准答案，"
            "未命中则把检索到的知识作为上下文注入提示词，交由大模型生成回答。"))
C.append(("bullet", "双通道检索：SearchStrategy 支持向量检索与关键词检索，提升召回效果。"))
C.append(("bullet", "提示词模板化：PromptScene 定义不同场景，PromptTemplateManager 负责渲染，便于统一管理与复用。"))
C.append(("bullet", "数据流水线：FaqDataProcessor 负责 MySQL 数据到 Document 的转换，FaqDataPipelineService 负责全量同步与增量同步。"))
C.append(("bullet", "Function Calling：将商品查询、价格查询、订单查询等业务能力注册为工具，让大模型按需调用。"))

C.append(("h1", "3. 软件实现"))
C.append(("h2", "3.1 技术栈与环境"))
C.append(("bullet", "语言与运行环境：Java 17、Spring Boot 3.5.6、Maven。"))
C.append(("bullet", "AI 能力：Spring AI Alibaba、阿里云 DashScope（qwen-plus、text-embedding-v2）。"))
C.append(("bullet", "数据存储：MySQL 8 + MyBatis + Druid（druid-spring-boot-3-starter）；Milvus 2.4.4 向量数据库。"))
C.append(("bullet", "前端：原生 HTML/CSS/JavaScript（index.html、faq-test.html）。"))
C.append(("bullet", "测试：JUnit 5 + Mockito。"))

C.append(("h2", "3.2 核心模块实现"))
C.append(("p", "（1）智能问答主流程 AiAgentService.chat："))
C.append(("code", "public String chat(String question) {\n"
            "    if (sensitiveWordBs.contains(question)) {\n"
            "        return \"您的问题包含不当内容，AI 客服暂时无法回答。\";\n"
            "    }\n"
            "    Faq matchedFaq = vectorSearchService.searchFAQ(question);\n"
            "    if (matchedFaq != null) {\n"
            "        return matchedFaq.getAnswer();\n"
            "    }\n"
            "    return generateAIResponse(question);\n"
            "}"))
C.append(("p", "（2）大模型回答生成，注入知识上下文与提示词模板："))
C.append(("code", "private String generateAIResponse(String question) {\n"
            "    String knowledge = vectorSearchService.buildKnowledgeContext(question);\n"
            "    String resolvedPrompt = promptTemplateManager.render(\n"
            "        PromptScene.CUSTOMER_SERVICE,\n"
            "        Map.of(\"current_date\", LocalDate.now().toString(), \"knowledge\", knowledge));\n"
            "    return chatClient.prompt().system(resolvedPrompt).user(question).call().content();\n"
            "}"))
C.append(("p", "（3）数据流水线接入 FAQ 业务（FaqServiceImpl）："))
C.append(("code", "public FaqServiceImpl(FaqMapper faqMapper, VectorStore vectorStore,\n"
            "                     FaqDataPipelineService faqDataPipelineService) {\n"
            "    this.faqMapper = faqMapper;\n"
            "    this.vectorStore = vectorStore;\n"
            "    this.faqDataPipelineService = faqDataPipelineService;\n"
            "}\n"
            "public void syncMySQLToVector() { faqDataPipelineService.runFullSync(); }"))
C.append(("p", "（4）文档转换与元数据空值保护（FaqDataProcessor）："))
C.append(("code", "Map<String, Object> metadata = new HashMap<>();\n"
            "metadata.put(\"faq_id\", faq.getId());\n"
            "if (faq.getCategoryId() != null) {\n"
            "    metadata.put(\"category_id\", faq.getCategoryId());\n"
            "}\n"
            "metadata.put(\"question\", faq.getQuestion());\n"
            "metadata.put(\"answer\", faq.getAnswer());\n"
            "return new Document(faq.getId(), faq.getQuestion(), metadata);"))

C.append(("h2", "3.3 前端界面实现"))
C.append(("p", "商城首页提供商品展示、登录注册、购物车、知识助手等功能入口；配色采用黑金高级感风格，"
            "避免传统蓝紫「AI 模板感」。知识助手页面支持与智能客服对话，展示 RAG 与工具调用结果。"))

C.append(("h1", "4. 软件测试"))
C.append(("h2", "4.1 单元测试"))
C.append(("p", "使用 JUnit 5 + Mockito 编写单元测试，覆盖核心服务与工具函数。执行命令："))
C.append(("code", "mvn -q test"))
C.append(("p", "测试结果：全部通过（exit code 0），覆盖 FAQ 服务、Agent 服务、商品/订单工具函数、"
            "提示词模板、向量检索、数据流水线等模块。"))

C.append(("h2", "4.2 集成测试"))
C.append(("bullet", "应用启动验证：Started SpringAiSxApplication，正常启动无异常。"))
C.append(("bullet", "端到端验证：POST /ai/agent/chat 询问「蓝牙耳机多少钱」，返回包含价格 399 元等完整回答，"
            "验证 RAG + Function Calling 链路正常。"))
C.append(("bullet", "知识库检索验证：命中 FAQ 时直接返回标准答案；未命中时由大模型结合知识上下文生成回答。"))
C.append(("bullet", "敏感词验证：输入包含敏感词的问题时，返回拦截提示。"))

C.append(("h2", "4.3 测试结论"))
C.append(("p", "系统基本功能均已实现并通过验证，满足需求分析阶段提出的功能与非功能需求；"
            "RAG 检索、工具调用、数据流水线、提示词模板等扩展能力已正确接入主流程，整体运行稳定。"))

# ---------------------------------------------------------------------------
# 生成 docx 的 XML
# ---------------------------------------------------------------------------
def esc(s):
    return html.escape(s, quote=False)

def run_xml(text, bold=False, italic=False, size=None, color=None):
    """生成一个 w:r 元素，text 内支持换行 -> 多个 <w:br/>"""
    props = []
    if bold:
        props.append("<w:b/>")
    if italic:
        props.append("<w:i/>")
    if color:
        props.append('<w:color w:val="%s"/>' % color)
    if size:
        props.append('<w:sz w:val="%d"/><w:szCs w:val="%d"/>' % (size, size))
    rpr = "<w:rPr>%s</w:rPr>" % "".join(props) if props else ""
    lines = text.split("\n")
    runs = []
    for i, ln in enumerate(lines):
        if i > 0:
            runs.append("<w:r>%s<w:br/></w:r>" % rpr)
        runs.append("<w:r>%s<w:t xml:space=\"preserve\">%s</w:t></w:r>" % (rpr, esc(ln)))
    return "".join(runs)

def para_xml(style, text, extra_pr=""):
    """style: title/subtitle/h1/h2/p/bullet/code"""
    if style == "title":
        ppr = '<w:pPr><w:jc w:val="center"/><w:spacing w:after="120"/></w:pPr>'
        runs = run_xml(text, bold=True, size=36, color="1F1F1F")
    elif style == "subtitle":
        ppr = '<w:pPr><w:jc w:val="center"/><w:spacing w:after="240"/></w:pPr>'
        runs = run_xml(text, bold=True, size=28, color="404040")
    elif style == "h1":
        ppr = '<w:pPr><w:spacing w:before="240" w:after="120"/><w:pBdr><w:bottom w:val="single" w:sz="8" w:space="4" w:color="B8860B"/></w:pBdr></w:pPr>'
        runs = run_xml(text, bold=True, size=30, color="8B6508")
    elif style == "h2":
        ppr = '<w:pPr><w:spacing w:before="160" w:after="80"/></w:pPr>'
        runs = run_xml(text, bold=True, size=24, color="B8860B")
    elif style == "bullet":
        ppr = '<w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr><w:spacing w:after="60"/></w:pPr>'
        runs = run_xml(text, size=22)
    elif style == "code":
        ppr = '<w:pPr><w:ind w:left="360"/><w:spacing w:after="120"/><w:shd w:val="clear" w:color="auto" w:fill="F5F5F5"/></w:pPr>'
        runs = run_xml(text, size=20, color="333333")
    else:  # p
        ppr = '<w:pPr><w:spacing w:after="80" w:line="300" w:lineRule="auto"/></w:pPr>'
        runs = run_xml(text, size=22)
    return "<w:p>%s%s</w:p>" % (ppr, runs)

def build_document_xml():
    body = []
    for style, text in C:
        body.append(para_xml(style, text))
    sect = ('<w:sectPr><w:pgSz w:w="11906" w:h="16838"/>'
            '<w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" '
            'w:header="720" w:footer="720" w:gutter="0"/></w:sectPr>')
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
            '<w:body>' + "".join(body) + sect + '</w:body></w:document>')

CONTENT_TYPES = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
    '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
    '<Default Extension="xml" ContentType="application/xml"/>'
    '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
    '<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>'
    '</Types>')

RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>'
    '</Relationships>')

DOC_RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>'
    '</Relationships>')

STYLES = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
    '<w:style w:type="paragraph" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr>'
    '<w:rFonts w:ascii="宋体" w:eastAsia="宋体" w:hAnsi="宋体"/><w:sz w:val="22"/></w:rPr></w:style>'
    '</w:styles>')

def build():
    if not os.path.isdir(os.path.dirname(OUT)):
        os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", CONTENT_TYPES)
        z.writestr("_rels/.rels", RELS)
        z.writestr("word/document.xml", build_document_xml())
        z.writestr("word/_rels/document.xml.rels", DOC_RELS)
        z.writestr("word/styles.xml", STYLES)
    print("OK ->", OUT)

if __name__ == "__main__":
    build()
