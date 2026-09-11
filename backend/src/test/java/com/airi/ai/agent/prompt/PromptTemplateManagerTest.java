package com.airi.ai.agent.prompt;

import com.airi.ai.agent.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PromptTemplateManager 单元测试
 *
 * <p>不依赖 Spring 容器：用 ReflectionTestUtils 注入 customerServiceTemplate，
 * 验证参数替换、缺失参数校验、场景渲染三条核心路径。</p>
 */
class PromptTemplateManagerTest {

    private PromptTemplateManager manager;

    @BeforeEach
    void setUp() {
        manager = new PromptTemplateManager();
        ReflectionTestUtils.setField(manager, "customerServiceTemplate",
                "你是客服小艾。知识库：{knowledge}。今天日期：{current_date}");
    }

    @Test
    void testRenderReplacesAllPlaceholders() {
        String result = manager.render("你好，{name}，欢迎回来！", Map.of("name", "小明"));
        assertEquals("你好，小明，欢迎回来！", result);
    }

    @Test
    void testRenderMissingParamThrows() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> manager.render("知识库：{knowledge}", Map.of()));
        assertEquals(106, ex.getCode());
        assertTrue(ex.getMessage().contains("knowledge"));
    }

    @Test
    void testRenderNullParamsThrows() {
        assertThrows(BusinessException.class,
                () -> manager.render("知识库：{knowledge}", null));
    }

    @Test
    void testRenderBlankTemplateThrows() {
        assertThrows(BusinessException.class,
                () -> manager.render("  ", Map.of("a", "1")));
    }

    @Test
    void testRenderCustomerServiceScene() {
        String result = manager.render(PromptScene.CUSTOMER_SERVICE,
                Map.of("knowledge", "Q: 运费多少?\nA: 满99包邮", "current_date", "2026-08-18"));
        assertTrue(result.contains("你是客服小艾"));
        assertTrue(result.contains("Q: 运费多少?\nA: 满99包邮"));
        assertTrue(result.contains("2026-08-18"));
    }

    @Test
    void testRenderSceneMissingParamThrows() {
        assertThrows(BusinessException.class,
                () -> manager.render(PromptScene.CUSTOMER_SERVICE, Map.of("knowledge", "无")));
    }

    @Test
    void testSceneFromKey() {
        assertEquals(PromptScene.CUSTOMER_SERVICE, PromptScene.fromKey("customer-service"));
        assertNull(PromptScene.fromKey("not-exist"));
    }
}
