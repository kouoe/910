package com.airi.ai.agent.config;

import com.airi.ai.agent.mapper.SensitiveWordMapper;
import com.airi.ai.agent.pojo.SensitiveWord;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * SensitiveWordConfig 单元测试
 *
 * <p>用 Mockito 模拟 SensitiveWordMapper，验证敏感词配置 Bean 构建
 * （黑/白名单加载、numCheckLen 数字校验）。</p>
 */
class SensitiveWordConfigTest {

    private SensitiveWordMapper mapper;
    private SensitiveWordBs sensitiveWordBs;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(SensitiveWordMapper.class);
        when(mapper.selectByType("deny")).thenReturn(List.of(new SensitiveWord(1L, "假货", "deny")));
        when(mapper.selectByType("allow")).thenReturn(List.of(new SensitiveWord(2L, "退货", "allow")));

        SensitiveWordConfig config = new SensitiveWordConfig(mapper);
        sensitiveWordBs = config.sensitiveWordBs();
    }

    @Test
    void testSensitiveWordBsNotNull() {
        assertNotNull(sensitiveWordBs);
    }

    @Test
    void testDenyWordDetected() {
        // 黑名单词「假货」应被拦截
        assertTrue(sensitiveWordBs.contains("假货"));
    }

    @Test
    void testAllowWordPassed() {
        // 白名单词「退货」应被放行（不判定为敏感）
        assertFalse(sensitiveWordBs.contains("退货"));
    }

    @Test
    void testNumCheckLen() {
        // 连续 6 位数字判敏感，5 位数字不判敏感
        assertTrue(sensitiveWordBs.contains("123456"));
        assertFalse(sensitiveWordBs.contains("12345"));
    }
}
