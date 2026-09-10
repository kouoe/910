package com.airi.ai.agent.controller;

import com.airi.ai.agent.result.R;
import com.airi.ai.agent.service.IAiAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AiAgentController 单元测试
 *
 * <p>用 Mockito 模拟 IAiAgentService，验证聊天接口空值校验（code=100）与正常返回 R 结构。</p>
 */
class AiAgentControllerTest {

    private IAiAgentService aiAgentService;
    private AiAgentController controller;

    @BeforeEach
    void setUp() {
        aiAgentService = Mockito.mock(IAiAgentService.class);
        controller = new AiAgentController(aiAgentService);
    }

    @Test
    void testChatWithNullBodyReturnsParameterError() {
        R r = controller.chat(null);
        assertEquals(100, r.getCode());
    }

    @Test
    void testChatWithEmptyMessageReturnsParameterError() {
        R r = controller.chat(Map.of("message", ""));
        assertEquals(100, r.getCode());
    }

    @Test
    void testChatWithBlankMessageReturnsParameterError() {
        R r = controller.chat(Map.of("message", "   "));
        assertEquals(100, r.getCode());
    }

    @Test
    void testChatNormalReturnsResponse() {
        when(aiAgentService.chat("你好")).thenReturn("你好，我是智能客服");
        R r = controller.chat(Map.of("message", "你好"));

        assertEquals(0, r.getCode());
        assertEquals("你好，我是智能客服", r.getDataMap().get("response"));
        verify(aiAgentService).chat("你好");
    }
}
