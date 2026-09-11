package com.airi.ai.agent.result;

/**
 * 统一错误码枚举
 *
 * <p>规范：Success 以 0 表示，通用失败以负数表示，业务错误以正数编码。</p>
 *
 * <p>错误码分段：</p>
 * <ul>
 *   <li>0：成功</li>
 *   <li>-1：通用失败（兜底）</li>
 *   <li>100 起：业务错误（100 参数 / 101 敏感词 / 102 FAQ / 103-107 扩展校验码）</li>
 * </ul>
 */
public enum ErrorCode {

    /** 成功 */
    SUCCESS(0, "成功"),
    /** 通用失败（兜底，未识别异常统一返回） */
    FAIL(-1, "失败"),
    /** 参数有误（含请求体缺失、message 为空等） */
    PARAMETER_ERROR(100, "参数有误"),
    /** 敏感词不存在，无法删除 */
    SENSITIVE_WORD_ID_ERROR(101, "敏感词不存在，无法删除"),
    /** FAQ 不存在 */
    FAQ_NOT_FOUND(102, "FAQ 不存在"),
    /** FAQ 问题和答案不能为空（扩展码，由 FaqServiceImpl 校验抛出） */
    FAQ_CONTENT_EMPTY(103, "FAQ 问题和答案不能为空"),
    /** 敏感词类型必须为 deny 或 allow（扩展码，由敏感词管理接口校验抛出） */
    SENSITIVE_WORD_FORMAT_ERROR(104, "敏感词类型必须为 deny 或 allow"),
    /** 提示词模板不存在 */
    PROMPT_TEMPLATE_NOT_FOUND(105, "提示词模板不存在"),
    /** 提示词模板缺少必需参数 */
    PROMPT_TEMPLATE_MISSING_PARAM(106, "提示词模板缺少必需参数"),
    /** 待处理 FAQ 数据为空 */
    FAQ_DATA_EMPTY(107, "待处理 FAQ 数据为空"),
    ;

    /** 错误码值 */
    private final Integer code;
    /** 错误提示消息 */
    private final String message;

    /**
     * 枚举构造器：绑定错误码与提示消息
     *
     * @param code    错误码值
     * @param message 提示消息
     */
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /** @return 错误码值 */
    public Integer getCode() { return code; }

    /** @return 错误提示消息 */
    public String getMessage() { return message; }
}
