package com.airi.ai.agent.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorCode 单元测试
 *
 * <p>验证错误码枚举的 code/message 取值。</p>
 */
class ErrorCodeTest {

    @Test
    void testSuccess() {
        assertEquals(0, ErrorCode.SUCCESS.getCode());
        assertEquals("成功", ErrorCode.SUCCESS.getMessage());
    }

    @Test
    void testFail() {
        assertEquals(-1, ErrorCode.FAIL.getCode());
        assertEquals("失败", ErrorCode.FAIL.getMessage());
    }

    @Test
    void testParameterError() {
        assertEquals(100, ErrorCode.PARAMETER_ERROR.getCode());
        assertEquals("参数有误", ErrorCode.PARAMETER_ERROR.getMessage());
    }

    @Test
    void testFaqNotFound() {
        assertEquals(102, ErrorCode.FAQ_NOT_FOUND.getCode());
        assertEquals("FAQ 不存在", ErrorCode.FAQ_NOT_FOUND.getMessage());
    }
}
