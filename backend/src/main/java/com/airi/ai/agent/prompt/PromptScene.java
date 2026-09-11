package com.airi.ai.agent.prompt;

/**
 * 提示词业务场景枚举
 *
 * <p>定义系统内置的提示词场景，每个场景对应一段 System Prompt 模板。
 * 模板可被 {@link PromptTemplateManager} 渲染，并通过配置
 * <code>spring.ai.prompt-template.&lt;sceneKey&gt;</code> 按场景覆盖。</p>
 */
public enum PromptScene {

    /**
     * 客服问答场景
     *
     * <p>与项目实际 System Prompt（application.yml 中 spring.ai.dashscope.prompt）保持一致，
     * 占位符：{knowledge}（RAG 知识库上下文）、{current_date}（当前日期）。</p>
     */
    CUSTOMER_SERVICE("customer-service", """
            你是艾瑞电商的 AI 客服小艾，专注于帮助用户解答购物相关问题。

            你的职责包括：
            1. 解答商品咨询、订单查询、售后服务等常见问题
            2. 使用工具函数查询订单信息（按订单号或按状态）和商品信息
               （商品搜索、价格查询、分类查询、价格区间查询、热销排行）
            3. 基于【知识库内容】提供准确、友好的回答

            回答规则（必须严格遵守）：
            - 商品、订单、售后等事实性问题：只能依据【知识库内容】和工具查询结果回答，
              严禁编造任何商品、价格、库存、活动等信息
            - 若【知识库内容】为空或与问题无关，必须如实回答"目前没有查到相关信息，
              建议联系人工客服"，严禁自行发挥或猜测
            - 寒暄问候（如"你好""在吗"）可正常礼貌回应，无需依赖知识库
            - 优先使用 FAQ 知识库中的标准答案
            - 保持语气亲切、专业

            知识库内容：
            {knowledge}

            当前日期：{current_date}
            """),
    ;

    /** 场景配置键（对应 application.yml 中 spring.ai.prompt-template.xxx） */
    private final String sceneKey;

    /** 场景默认模板（内置兜底，可被配置覆盖） */
    private final String defaultTemplate;

    PromptScene(String sceneKey, String defaultTemplate) {
        this.sceneKey = sceneKey;
        this.defaultTemplate = defaultTemplate;
    }

    public String getSceneKey() {
        return sceneKey;
    }

    public String getDefaultTemplate() {
        return defaultTemplate;
    }

    /**
     * 根据场景配置键查找枚举
     *
     * @param sceneKey 配置键（如 "customer-service"）
     * @return 匹配的 PromptScene，未匹配返回 null
     */
    public static PromptScene fromKey(String sceneKey) {
        for (PromptScene scene : values()) {
            if (scene.sceneKey.equals(sceneKey)) {
                return scene;
            }
        }
        return null;
    }
}
