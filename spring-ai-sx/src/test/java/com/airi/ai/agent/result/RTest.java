package com.airi.ai.agent.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R 统一响应类单元测试
 *
 * <p>验证 ok/error 工厂方法与链式 data。</p>
 */
class RTest {

    @Test
    void testOk() {
        R r = R.ok();
        assertEquals(0, r.getCode());
        assertEquals("成功", r.getMsg());
    }

    @Test
    void testError() {
        R r = R.error();
        assertEquals(-1, r.getCode());
        assertEquals("失败", r.getMsg());
    }

    @Test
    void testErrorWithErrorCode() {
        R r = R.error(ErrorCode.PARAMETER_ERROR);
        assertEquals(100, r.getCode());
        assertEquals("参数有误", r.getMsg());
    }

    @Test
    void testErrorWithCustomCodeAndMessage() {
        R r = R.error(500, "服务器内部错误");
        assertEquals(500, r.getCode());
        assertEquals("服务器内部错误", r.getMsg());
    }

    @Test
    void testDataChaining() {
        R r = R.ok().data("user", "张三").data("total", 100);
        assertEquals("张三", r.getDataMap().get("user"));
        assertEquals(100, r.getDataMap().get("total"));
    }
}
