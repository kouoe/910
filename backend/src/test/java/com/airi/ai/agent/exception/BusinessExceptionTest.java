package com.airi.ai.agent.exception;

import com.airi.ai.agent.result.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessException 单元测试
 *
 * <p>验证业务异常构造、错误码与消息透传。</p>
 */
class BusinessExceptionTest {

    @Test
    void testExceptionWithErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.FAQ_NOT_FOUND);
        assertEquals(102, ex.getCode());
        assertEquals("FAQ 不存在", ex.getMessage());
    }

    @Test
    void testExceptionWithCustomCodeAndMessage() {
        BusinessException ex = new BusinessException(500, "自定义错误消息");
        assertEquals(500, ex.getCode());
        assertEquals("自定义错误消息", ex.getMessage());
    }

    @Test
    void testGetCodeFromErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.PARAMETER_ERROR);
        assertEquals(100, ex.getCode());
    }

    @Test
    void testIsRuntimeException() {
        BusinessException ex = new BusinessException(ErrorCode.FAIL);
        assertTrue(ex instanceof RuntimeException);
    }
}
