package com.airi.ai.agent.data;

import com.airi.ai.agent.exception.BusinessException;
import com.airi.ai.agent.mapper.FaqMapper;
import com.airi.ai.agent.pojo.Faq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FaqDataPipelineService 单元测试
 *
 * <p>使用 Mockito 模拟依赖，验证流水线各环节与全量同步的统计结果
 * （不触发真实 MySQL/Milvus 连接）。</p>
 */
class FaqDataPipelineServiceTest {

    private FaqMapper faqMapper;
    private VectorStore vectorStore;
    private FaqDataPipelineService pipeline;

    @BeforeEach
    void setUp() {
        faqMapper = Mockito.mock(FaqMapper.class);
        vectorStore = Mockito.mock(VectorStore.class);
        pipeline = new FaqDataPipelineService(faqMapper, vectorStore, new FaqDataProcessor());
    }

    @Test
    void testCollectFaqsPaged() {
        when(faqMapper.count(null, null)).thenReturn(250);
        when(faqMapper.selectByPage(0, 100, null, null)).thenReturn(List.of(
                new Faq("1", 1, "q1", "a1", 1, 0)));
        when(faqMapper.selectByPage(100, 100, null, null)).thenReturn(List.of(
                new Faq("2", 1, "q2", "a2", 1, 0)));
        when(faqMapper.selectByPage(200, 100, null, null)).thenReturn(List.of(
                new Faq("3", 1, "q3", "a3", 1, 0)));

        assertEquals(3, pipeline.collectFaqs().size());
    }

    @Test
    void testCleanAndTransformDiscardsDirty() {
        Faq good = new Faq("1", 1, "好问题", "好答案", 1, 0);
        Faq dirty = new Faq("2", 1, "   ", "空问题", 1, 0);

        List<Document> docs = pipeline.cleanAndTransform(List.of(good, dirty));

        assertEquals(1, docs.size());
        assertEquals("1", docs.get(0).getId());
    }

    @Test
    void testRunFullSyncEmptyThrows() {
        when(faqMapper.count(null, null)).thenReturn(0);
        assertThrows(BusinessException.class, pipeline::runFullSync);
    }

    @Test
    void testRunFullSyncStoresAll() {
        when(faqMapper.count(null, null)).thenReturn(1);
        when(faqMapper.selectByPage(0, 100, null, null)).thenReturn(List.of(
                new Faq("1", 1, "问题", "答案", 1, 0)));

        FaqDataPipelineService.SyncResult result = pipeline.runFullSync();

        assertEquals(1, result.total());
        assertEquals(1, result.cleaned());
        assertEquals(0, result.discarded());
        assertEquals(1, result.stored());
        verify(vectorStore).add(Mockito.anyList());
    }

    @Test
    void testRunIncrementalUpdateDeletesThenAdds() {
        Faq faq = new Faq("10", 1, "新问题", "新答案", 1, 0);
        pipeline.runIncrementalUpdate(faq);

        verify(vectorStore).delete(List.of("10"));
        verify(vectorStore).add(Mockito.anyList());
    }

    @Test
    void testRunIncrementalUpdateDirtyThrows() {
        Faq dirty = new Faq("11", 1, "  ", "", 1, 0);
        assertThrows(BusinessException.class, () -> pipeline.runIncrementalUpdate(dirty));
    }
}
