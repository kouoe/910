package com.airi.ai.agent.exception;

import com.airi.ai.agent.result.ErrorCode;
import com.airi.ai.agent.result.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>统一拦截所有 Controller 抛出的异常，转为标准 R 响应。</p>
 * <p>防止 500 错误直接暴露给前端。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     *
     * <p>Service 层抛出的可预期业务异常统一在此捕获，code/message 原样透传前端。</p>
     *
     * @param e 业务异常
     * @return 携带业务错误码与消息的统一 R 响应
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * <p>如 @RequestParam 类型转换失败、非法参数等，统一转为 code=100 参数有误。</p>
     *
     * @param e 参数异常
     * @return code=100 的统一 R 响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常：{}", e.getMessage());
        return R.error(ErrorCode.PARAMETER_ERROR);
    }

    /**
     * 兜底处理未知异常
     *
     * <p>未识别的异常（含 HTTP 层 404/405 等）最终落入此方法，返回 code=-1，</p>
     * <p>保证任何情况下前端拿到的都是统一 R 结构，而非裸 500 错误页。</p>
     *
     * @param e 未知异常
     * @return code=-1 的统一 R 响应
     */
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        log.error("系统异常", e);
        return R.error(ErrorCode.FAIL);
    }
}
