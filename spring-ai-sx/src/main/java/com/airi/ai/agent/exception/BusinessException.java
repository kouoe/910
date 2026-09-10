package com.airi.ai.agent.exception;

import com.airi.ai.agent.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常（可预期异常）
 *
 * <p>Service 层抛出此异常，由全局异常处理器捕获并转为统一 R 响应。</p>
 * <p>仅通过 getCode() + getMessage() 传递错误信息，不自行遮蔽父类字段。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误状态码 */
    private final Integer code;

    /**
     * 通过自定义 code 和 message 构造
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 通过错误码枚举构造
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
