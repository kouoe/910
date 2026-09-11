package com.airi.ai.agent.service.impl;

import com.airi.ai.agent.data.FaqDataPipelineService;
import com.airi.ai.agent.exception.BusinessException;
import com.airi.ai.agent.mapper.FaqMapper;
import com.airi.ai.agent.pojo.Faq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.vectorstore.VectorStore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FaqServiceImpl 单元测试
 *
 * <p>用 Mockito 模拟 FaqMapper 与 VectorStore，验证 getFaqById 命中/未命中/空参异常，
 * 以及 add 的默认值与 ID 生成。</p>
 */
class FaqServiceImplTest {

    private FaqMapper faqMapper;
    private VectorStore vectorStore;
    private FaqDataPipelineService faqDataPipelineService;
    private FaqServiceImpl faqService;

    @BeforeEach
    void setUp() {
        faqMapper = Mockito.mock(FaqMapper.class);
        vectorStore = Mockito.mock(VectorStore.class);
        faqDataPipelineService = Mockito.mock(FaqDataPipelineService.class);
        faqService = new FaqServiceImpl(faqMapper, vectorStore, faqDataPipelineService);
    }

    @Test
    void testGetFaqByIdHit() {
        Faq faq = new Faq("f1", 1, "问题", "答案", 1, 0);
        when(faqMapper.selectById("f1")).thenReturn(faq);

        Faq result = faqService.getFaqById("f1");

        assertNotNull(result);
        assertEquals("f1", result.getId());
    }

    @Test
    void testGetFaqByIdMiss() {
        when(faqMapper.selectById("not-exist")).thenReturn(null);
        assertNull(faqService.getFaqById("not-exist"));
    }

    @Test
    void testGetFaqByIdEmptyParamThrows() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> faqService.getFaqById(""));
        assertEquals(100, ex.getCode());
    }

    @Test
    void testAddDefaultsAndGeneratesId() {
        Faq faq = new Faq();
        faq.setCategoryId(1);
        faq.setQuestion("问题");
        faq.setAnswer("答案");

        faqService.add(faq);

        assertNotNull(faq.getId());
        assertEquals(1, faq.getStatus());
        assertEquals(0, faq.getUseCount());
        verify(faqMapper).insert(faq);
    }
}
