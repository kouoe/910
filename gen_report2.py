# -*- coding: utf-8 -*-
"""基于学校模板，填充实习报告正文，输出 docx（仅用标准库 zipfile）"""
import zipfile, os, shutil

SRC = r"C:\Users\shr18\Desktop\综合创新实践实习报告模板.docx"
DST = r"C:\Users\shr18\Desktop\综合创新实践实习报告.docx"

# ---------------------------------------------------------------------------
# 辅助：XML 转义
# ---------------------------------------------------------------------------
def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

# ---------------------------------------------------------------------------
# 段落生成器（严格对齐模板字体体系：黑体标题 / 宋体正文）
# ---------------------------------------------------------------------------
def _rp(font="宋体", bold=False, sz="24"):
    """run 属性"""
    b = "<w:b/><w:bCs/>" if bold else ""
    return ('<w:rPr><w:rFonts w:hint="eastAsia" w:ascii="%s" w:hAnsi="%s" w:eastAsia="%s"/>%s'
            '<w:sz w:val="%s"/><w:szCs w:val="%s"/></w:rPr>') % (font, font, font, b, sz, sz)

def h2(text):
    """二级标题：黑体加粗 四号(28)"""
    return ('<w:p><w:pPr><w:spacing w:before="120" w:after="60" w:line="360" w:lineRule="auto"/>'
            + _rp("黑体", True, "28").replace("w:rPr", "w:rPr", 1) +
            '</w:pPr><w:r>' + _rp("黑体", True, "28") +
            '<w:t xml:space="preserve">' + esc(text) + '</w:t></w:r></w:p>')

def p(text, indent=True):
    """正文：宋体 小四(24)，1.5倍行距，首行缩进2字符"""
    ind = '<w:ind w:firstLine="480" w:firstLineChars="200"/>' if indent else ''
    return ('<w:p><w:pPr><w:spacing w:line="360" w:lineRule="auto"/>' + ind + _rp("宋体", False, "24") +
            '</w:pPr><w:r>' + _rp("宋体", False, "24") +
            '<w:t xml:space="preserve">' + esc(text) + '</w:t></w:r></w:p>')

def bullet(text):
    """项目符号：宋体 小四，悬挂缩进"""
    return ('<w:p><w:pPr><w:spacing w:line="360" w:lineRule="auto"/>'
            '<w:ind w:left="480" w:firstLine="480" w:firstLineChars="200"/>' + _rp("宋体", False, "24") +
            '</w:pPr><w:r>' + _rp("宋体", False, "24") +
            '<w:t xml:space="preserve">● </w:t></w:r><w:r>' + _rp("宋体", False, "24") +
            '<w:t xml:space="preserve">' + esc(text) + '</w:t></w:r></w:p>')

def num(text):
    """编号列表项：宋体 小四"""
    return ('<w:p><w:pPr><w:spacing w:line="360" w:lineRule="auto"/>'
            '<w:ind w:left="480"/>' + _rp("宋体", False, "24") +
            '</w:pPr><w:r>' + _rp("宋体", False, "24") +
            '<w:t xml:space="preserve">' + esc(text) + '</w:t></w:r></w:p>')

def code(text):
    """代码块：Consolas 10.5pt，浅灰底纹，多行用 <w:br/>"""
    runs = []
    for i, line in enumerate(text.split("\n")):
        if i > 0:
            runs.append('<w:r>' + _rp("Consolas", False, "21") + '<w:br/></w:r>')
        runs.append('<w:r>' + _rp("Consolas", False, "21") +
                    '<w:t xml:space="preserve">' + esc(line) + '</w:t></w:r>')
    return ('<w:p><w:pPr><w:spacing w:line="300" w:lineRule="auto"/>'
            '<w:ind w:left="240"/><w:shd w:val="clear" w:color="auto" w:fill="F2F2F2"/>'
            + _rp("Consolas", False, "21") +
            '</w:pPr>' + "".join(runs) + '</w:p>')

# ---------------------------------------------------------------------------
# 正文内容
# ---------------------------------------------------------------------------
def section1():
    s = []
    s.append(h2("1.1 项目背景"))
    s.append(p("随着电子商务的迅猛发展以及人工智能技术的不断成熟，传统电商客服模式已经难以满足用户对服务质量与响应速度日益增长的需求。"
               "传统人工客服普遍面临工作时间受限、重复性咨询量大、人力成本居高不下、知识更新不及时、服务质量参差不齐等问题。与此同时，"
               "以大语言模型（Large Language Model, LLM）为代表的人工智能技术迅速崛起，使机器能够在一定程度上理解人类自然语言，"
               "并生成连贯、合理的回答，为智能客服的落地提供了坚实的技术基础。"))
    s.append(p("在此背景下，检索增强生成（Retrieval-Augmented Generation, RAG）技术应运而生。它通过先检索相关知识、再将检索结果作为上下文注入大模型的方式，"
               "有效缓解了大模型在专业领域知识不足、容易产生“幻觉”等问题，使回答更加准确、可靠。"))
    s.append(p("本项目基于 Spring Boot 框架与 Spring AI Alibaba 生态（对接阿里云通义千问大模型），构建一套面向电商场景的智能客服系统。"
               "系统在提供商品信息展示、用户注册登录、购物车管理等基础商城能力的同时，通过 Agent 工具调用（Function Calling）与向量知识库检索，"
               "实现自然语言驱动的商品查询、价格查询、热销商品查询、订单查询等智能问答能力，从而降低人工客服压力、提升用户体验与转化率。"))

    s.append(h2("1.2 选题意义"))
    s.append(p("本课题的选择具有技术、应用与学习三个层面的意义。从技术层面看，它将大语言模型、向量数据库、提示词工程、检索增强生成、"
               "工具调用等多项前沿技术进行工程化整合，有助于深入理解大模型应用的完整技术链路；从应用层面看，智能客服是当前企业降本增效的重要方向，"
               "本项目构建的原型系统具有较强的现实参考价值；从学习层面看，通过完整参与一个软件项目从需求分析、系统设计、编码实现到测试验证的全过程，"
               "能够全面提升软件工程实践能力、问题分析与解决能力以及团队协作能力。"))

    s.append(h2("1.3 功能需求"))
    s.append(p("通过对电商客服业务场景的分析，本项目确定以下功能需求："))
    s.append(bullet("商品信息展示：能够浏览商品列表、查看商品详情，并支持按分类查看与热销商品展示，帮助用户快速了解商品。"))
    s.append(bullet("用户注册与登录：支持新用户注册账号、已有用户登录系统，为购物车与订单等个性化功能提供身份基础。"))
    s.append(bullet("购物车管理：支持将商品加入购物车、查看购物车内容、修改商品数量、删除购物车中的商品。"))
    s.append(bullet("FAQ 知识库管理：支持常见问题（FAQ）的增、删、改、查，并将 FAQ 数据同步至向量数据库，为智能问答提供知识来源。"))
    s.append(bullet("智能问答：支持用户以自然语言提问，系统优先检索 FAQ 知识库，命中时直接返回标准答案，未命中时结合检索知识调用大模型生成回答。"))
    s.append(bullet("Agent 工具调用：将商品查询、价格查询、热销商品查询、订单编号查询、订单状态查询等业务能力注册为可调用工具，供大模型按需调用。"))
    s.append(bullet("内容安全过滤：对用户输入进行敏感词检测，拦截包含不当内容的问题，保障系统内容安全。"))

    s.append(h2("1.4 非功能需求"))
    s.append(bullet("性能：对于常见 FAQ 问题能够快速响应，向量检索与工具调用需保证较低的延迟。"))
    s.append(bullet("可扩展性：提示词采用模板化管理，业务工具可插拔，便于后续扩展新的业务能力。"))
    s.append(bullet("可维护性：数据流水线统一处理 MySQL 与向量库之间的同步，降低维护成本。"))
    s.append(bullet("可靠性：当向量检索失败或不可用时，系统能够降级为直接生成回答，保证服务可用。"))
    s.append(bullet("可观测性：使用 TraceId 进行链路追踪，便于问题定位与监控。"))
    s.append(bullet("安全性：对用户输入进行敏感词过滤，保护系统与用户的信息安全。"))

    s.append(h2("1.5 需求可行性分析"))
    s.append(p("技术可行性方面，Spring Boot 生态成熟稳定，Spring AI 提供了对 ChatClient、VectorStore、ToolCallingChatOptions 等抽象的支持，"
               "阿里云 DashScope 提供稳定的大模型 API，Milvus 向量数据库与 MySQL 关系型数据库均为业界成熟组件，技术方案具备可行性。"))
    s.append(p("经济可行性方面，系统全部采用开源框架与按量付费的云服务，开发与部署成本较低。"))
    s.append(p("操作可行性方面，系统提供直观的 Web 界面，用户无需专业培训即可使用；同时知识库与提示词模板便于运维人员维护。"))

    s.append(h2("1.6 需求分析小结"))
    s.append(p("综合以上分析，本项目需求明确、技术路线清晰、实施条件具备。系统将围绕“商城基础功能 + 智能问答能力”两大主线进行设计与实现，"
               "以满足电商场景下用户咨询与自助服务的核心诉求。"))
    return s

def section2():
    s = []
    s.append(h2("2.1 总体架构设计"))
    s.append(p("系统采用经典的分层架构，自上而下划分为 Web 表现层、业务服务层、AI 能力层与数据访问层，并结合 Spring AI 提供的 ChatClient、"
               "VectorStore、ToolCallingChatOptions 等抽象形成完整的技术栈。各层职责清晰、耦合度低，便于开发与维护。"))
    s.append(bullet("Web 表现层：基于 Spring MVC 的控制器（AiAgentController、ProductController、UserController、CartController、FaqController），"
                    "对外提供 RESTful 接口，同时提供原生 HTML/CSS/JavaScript 构建的前端页面。"))
    s.append(bullet("业务服务层：包括 AiAgentService、ProductService、OrderService、FaqService 等核心服务，负责业务逻辑的编排与处理。"))
    s.append(bullet("AI 能力层：包括 ChatClient（大模型对话）、VectorSearchService（向量检索）、PromptTemplateManager（提示词模板）、"
                    "FaqDataPipelineService（数据流水线）、SensitiveWordBs（敏感词过滤）等，是系统的智能核心。"))
    s.append(bullet("数据访问层：通过 MyBatis Mapper 访问 MySQL 关系型数据库，通过 VectorStore 访问 Milvus 向量数据库。"))

    s.append(h2("2.2 技术选型"))
    s.append(num("开发语言与运行环境：Java 17、Spring Boot 3.5.6、Maven。"))
    s.append(num("AI 能力：Spring AI Alibaba、阿里云 DashScope（对话模型 qwen-plus、向量模型 text-embedding-v2）。"))
    s.append(num("数据存储：MySQL 8 + MyBatis + Druid（druid-spring-boot-3-starter）；Milvus 2.4.4 向量数据库。"))
    s.append(num("前端：原生 HTML/CSS/JavaScript。"))
    s.append(num("测试：JUnit 5 + Mockito。"))
    s.append(p("技术选型的核心理念是：在成熟稳定的 Spring Boot 生态之上，通过 Spring AI 统一抽象屏蔽底层大模型与向量库的差异，"
               "从而在保证功能完整性的同时，实现良好的可移植性与可维护性。"))

    s.append(h2("2.3 系统模块设计"))
    s.append(p("根据功能需求，系统划分为商城业务模块、知识库模块、智能问答模块、内容安全模块与前端展示模块。"))
    s.append(bullet("商城业务模块：负责商品、用户、购物车、订单等实体的管理，对应 Product、User、Cart、Order 相关的实体类、Mapper 与 Service。"))
    s.append(bullet("知识库模块：负责 FAQ 的增删改查以及 MySQL 与 Milvus 向量库之间的数据同步。"))
    s.append(bullet("智能问答模块：负责用户提问的处理流程，包括向量检索、提示词渲染、大模型调用与工具调用。"))
    s.append(bullet("内容安全模块：基于敏感词库对用户输入进行过滤。"))
    s.append(bullet("前端展示模块：提供商城首页与知识助手页面，展示商品、购物车以及智能问答交互界面。"))

    s.append(h2("2.4 数据库设计"))
    s.append(p("系统使用 MySQL 8 存储业务数据，共设计六张核心数据表，分别为用户表、商品表、购物车表、订单表、FAQ 知识库表与敏感词表。"))
    s.append(bullet("t_user（用户表）：存储用户编号、用户名、密码、手机号等字段，支撑注册与登录功能。"))
    s.append(bullet("t_product（商品表）：存储商品编号、商品名称、价格、分类、销量、库存等字段，支撑商品展示与查询。"))
    s.append(bullet("t_cart（购物车表）：存储购物车条目编号、用户编号、商品编号、数量等字段，支撑购物车管理。"))
    s.append(bullet("t_order（订单表）：存储订单编号、用户编号、商品编号、金额、状态等字段，支撑订单查询。"))
    s.append(bullet("t_faq（FAQ 知识库表）：存储问题、答案、分类等字段，是智能问答的知识来源，其数据同时以向量形式存储在 Milvus 中用于语义检索。"))
    s.append(bullet("t_sensitive_word（敏感词表）：存储敏感词，用于内容安全过滤。"))
    s.append(p("在向量检索方面，FAQ 数据通过向量模型转换为向量后写入 Milvus，实现基于语义相似度的检索，从而支持用户以自然语言而非精确关键词进行提问。"))

    s.append(h2("2.5 关键业务流程设计"))
    s.append(p("智能问答主流程设计如下：用户提交问题后，系统首先进行敏感词检测，若命中敏感词则直接返回拦截提示；随后调用向量检索服务在 FAQ 知识库中检索，"
               "若检索到高相似度 FAQ 则直接返回标准答案；若未命中，则将检索得到的相关知识作为上下文，渲染提示词模板后调用大模型生成回答，"
               "并根据需要调用注册的工具（如商品查询、订单查询）获取实时业务数据。"))
    s.append(p("数据同步流程设计如下：系统提供全量同步与增量同步两种方式。全量同步将 MySQL 中的全部 FAQ 数据转换为向量文档并写入 Milvus；"
               "增量同步在新增或更新单条 FAQ 时，仅同步该条数据，以提高效率。数据转换由独立的 FaqDataProcessor 完成，统一处理元数据与文档结构。"))

    s.append(h2("2.6 界面设计"))
    s.append(p("前端界面分为商城首页与知识助手页面。商城首页提供商品展示、登录注册、购物车等功能入口，配色采用黑金高级感风格，"
               "避免传统蓝紫“AI 模板感”；知识助手页面提供对话输入框与回答展示区，方便用户与智能客服进行交互。"))
    return s

def section3():
    s = []
    s.append(h2("3.1 开发环境搭建"))
    s.append(p("开发环境主要包括：JDK 17、Maven、MySQL 8、Milvus 2.4.4 以及 IntelliJ IDEA。其中 Milvus 通过 Docker 启动 standalone 模式，"
               "并采用内嵌 etcd 与本地存储的配置，以降低环境搭建复杂度。系统所需的大模型能力由阿里云 DashScope 提供，"
               "需在配置文件中填入有效的 API Key。"))

    s.append(h2("3.2 项目结构与依赖配置"))
    s.append(p("项目采用标准的 Maven 目录结构，核心包按照分层与职责组织，包括 pojo（实体类）、mapper（数据访问接口）、service（业务接口与实现）、"
               "controller（控制器）、config（配置）、functioncall（工具函数）、prompt（提示词模板）、search（向量检索）、data（数据流水线）等。"))
    s.append(p("在依赖配置方面，pom.xml 中引入 spring-ai-alibaba-starter、spring-boot-starter-web、mybatis-spring-boot-starter、"
               "druid-spring-boot-3-starter、mysql-connector-j、sensitive-word 以及测试相关的 spring-boot-starter-test 等依赖，"
               "并为 spring-boot-maven-plugin 补充版本号以保证可正常构建与运行。"))

    s.append(h2("3.3 数据层实现"))
    s.append(p("数据层通过 MyBatis 实现。为每个业务实体定义对应的 Mapper 接口，通过注解或 XML 完成 SQL 的映射。"
               "同时定义商品、用户、购物车、订单、FAQ、敏感词等实体类，与数据库表结构一一对应。"))

    s.append(h2("3.4 智能问答核心实现"))
    s.append(p("智能问答的核心是 AiAgentService，其 chat 方法实现了“敏感词过滤 → 向量检索 → 命中原题返回 / 未命中生成回答”的完整流程："))
    s.append(code(
"public String chat(String question) {\n"
"    if (sensitiveWordBs.contains(question)) {\n"
"        return \"您的问题包含不当内容，AI 客服暂时无法回答。\";\n"
"    }\n"
"    Faq matchedFaq = vectorSearchService.searchFAQ(question);\n"
"    if (matchedFaq != null) {\n"
"        return matchedFaq.getAnswer();\n"
"    }\n"
"    return generateAIResponse(question);\n"
"}"))
    s.append(p("对于未命中的问题，系统构建知识上下文并渲染提示词模板后调用大模型生成回答："))
    s.append(code(
"private String generateAIResponse(String question) {\n"
"    String knowledge = vectorSearchService.buildKnowledgeContext(question);\n"
"    String resolvedPrompt = promptTemplateManager.render(\n"
"        PromptScene.CUSTOMER_SERVICE,\n"
"        Map.of(\"current_date\", LocalDate.now().toString(), \"knowledge\", knowledge));\n"
"    return chatClient.prompt().system(resolvedPrompt).user(question).call().content();\n"
"}"))
    s.append(p("提示词模板由 PromptTemplateManager 统一管理，通过 PromptScene 枚举区分不同业务场景，实现提示词的复用与集中维护。"
               "向量检索由 VectorSearchService 完成，支持向量检索与关键词检索的双通道策略，提升召回效果。"))
    s.append(p("数据流水线方面，FaqDataPipelineService 负责全量同步与增量同步，FaqDataProcessor 负责将 FAQ 数据转换为 Spring AI 的 Document 对象，"
               "并正确处理 category_id 为空时的元数据问题："))
    s.append(code(
"Map<String, Object> metadata = new HashMap<>();\n"
"metadata.put(\"faq_id\", faq.getId());\n"
"if (faq.getCategoryId() != null) {\n"
"    metadata.put(\"category_id\", faq.getCategoryId());\n"
"}\n"
"metadata.put(\"question\", faq.getQuestion());\n"
"metadata.put(\"answer\", faq.getAnswer());\n"
"return new Document(faq.getId(), faq.getQuestion(), metadata);"))

    s.append(h2("3.5 商城功能实现"))
    s.append(p("商城功能围绕商品、用户、购物车、订单展开。商品服务提供商品列表查询、详情查询、按分类查询、热销商品查询与价格查询；"
               "用户服务提供注册与登录；购物车服务提供加入购物车、查看购物车、修改数量、删除商品；订单服务提供按订单编号查询订单与按状态查询订单。"))
    s.append(p("为丰富 Agent 能力，系统将商品查询、价格查询、热销商品查询、订单编号查询、订单状态查询等业务能力注册为大模型可调用的工具（Function Calling），"
               "使大模型能够在对话过程中按需获取实时业务数据，而非仅依赖预训练知识。"))

    s.append(h2("3.6 前端实现"))
    s.append(p("前端基于原生 HTML/CSS/JavaScript 实现。商城首页展示商品列表并提供登录、注册、购物车等交互；知识助手页面实现与智能客服的对话交互，"
               "通过调用后端 /ai/agent/chat 接口获取回答并渲染展示。界面配色采用黑金高级感风格，提升视觉体验。"))

    s.append(h2("3.7 开发过程中遇到的问题与解决方案"))
    s.append(bullet("Maven 构建报错：spring-boot-maven-plugin 缺少版本号导致无法识别，通过补充版本号解决。"))
    s.append(bullet("Druid 数据源类找不到：原使用 Boot 2 版本 Starter，与 Spring Boot 3 不兼容，替换为 druid-spring-boot-3-starter 解决。"))
    s.append(bullet("启动报缺少 DashScope API Key：配置文件中原为占位符，填入有效的阿里云百炼 API Key 后解决。"))
    s.append(bullet("Milvus 连接失败：因中间件未启动导致，通过 Docker 启动 Milvus standalone 并配置内嵌 etcd 解决。"))
    s.append(bullet("元数据空值问题：FAQ 的 category_id 为空时会导致 Document 元数据包含 null，通过仅在非空时写入元数据解决。"))
    return s

def section4():
    s = []
    s.append(h2("4.1 测试方案与策略"))
    s.append(p("本项目采用单元测试与集成测试相结合的测试策略。单元测试基于 JUnit 5 与 Mockito，对核心服务与工具函数进行隔离测试；"
               "集成测试结合真实运行环境，验证应用启动、数据库连接、向量检索与大模型调用等端到端链路。"))

    s.append(h2("4.2 单元测试"))
    s.append(p("针对 FAQ 服务、Agent 服务、商品与订单工具函数、提示词模板、向量检索、数据流水线等模块编写单元测试，"
               "通过 Mockito 模拟依赖，验证各模块在隔离环境下的正确性。测试执行命令如下："))
    s.append(code("mvn -q test"))
    s.append(p("测试结果：全部通过，退出码为 0，覆盖核心业务逻辑与工具函数，验证了各模块功能的正确性。"))

    s.append(h2("4.3 集成测试"))
    s.append(bullet("应用启动验证：应用能够正常启动（Started SpringAiSxApplication），无异常。"))
    s.append(bullet("端到端验证：调用 POST /ai/agent/chat 接口，询问“蓝牙耳机多少钱”，返回包含价格 399 元等完整回答，"
                    "验证 RAG 与 Function Calling 链路正常。"))
    s.append(bullet("知识库检索验证：命中 FAQ 时直接返回标准答案；未命中时由大模型结合知识上下文生成回答。"))
    s.append(bullet("敏感词验证：输入包含敏感词的问题时，系统返回拦截提示，验证内容安全过滤生效。"))

    s.append(h2("4.4 功能测试用例"))
    s.append(p("针对主要功能设计并执行了测试用例：商品列表展示、商品详情查看、用户注册、用户登录、加入购物车、修改购物车数量、删除购物车商品、"
               "FAQ 新增与检索、智能问答、敏感词拦截、订单查询等。测试结果表明各项功能均符合预期。"))

    s.append(h2("4.5 测试结果分析与总结"))
    s.append(p("通过系统化的测试，本项目的基本功能与扩展能力均得到验证，能够满足需求分析阶段提出的功能与非功能需求。"
               "RAG 检索、工具调用、数据流水线、提示词模板等扩展能力已正确接入主流程，系统整体运行稳定。"
               "测试过程中发现并修复了若干缺陷，进一步提升了系统的健壮性与可用性。"))
    return s

# ---------------------------------------------------------------------------
# 插入正文：在每个标题段落之后插入对应内容
# ---------------------------------------------------------------------------
TITLES = [
    ("49ED94ED", section1),   # 一、需求分析
    ("17176CFF", section2),   # 二、软件设计
    ("5A598A35", section3),   # 三、软件实现
    ("6670EF78", section4),   # 四、软件测试
]

def insert_after(doc, paraid, extra_xml):
    marker = 'w14:paraId="%s"' % paraid
    idx = doc.find(marker)
    assert idx != -1, "未找到标题段落 " + paraid
    # 找到该段落结束 </w:p>
    end = doc.find("</w:p>", idx)
    assert end != -1, "未找到段落结束 " + paraid
    end += len("</w:p>")
    return doc[:end] + extra_xml + doc[end:]

def build():
    with zipfile.ZipFile(SRC, "r") as zin:
        names = zin.namelist()
        data = {n: zin.read(n) for n in names}

    doc = data["word/document.xml"].decode("utf-8")
    for paraid, sec in TITLES:
        xml = "".join(sec())
        doc = insert_after(doc, paraid, xml)
    data["word/document.xml"] = doc.encode("utf-8")

    # 更新 core.xml 标题（可选）
    if "docProps/core.xml" in data:
        core = data["docProps/core.xml"].decode("utf-8")
        core = core.replace("综合创新实践实习报告模板", "综合创新实践实习报告")
        data["docProps/core.xml"] = core.encode("utf-8")

    if os.path.exists(DST):
        os.remove(DST)
    with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as zout:
        for n in names:
            zout.writestr(n, data[n])
    print("OK ->", DST)
    print("size", os.path.getsize(DST))

if __name__ == "__main__":
    build()
