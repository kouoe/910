package com.airi.ai.agent.search;

import com.airi.ai.agent.mapper.FaqMapper;
import com.airi.ai.agent.pojo.Faq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VectorSearchService 单元测试
 *
 * <p>用 Mockito 模拟 VectorStore 与 FaqMapper，验证双通道检索、
 * 命中计数、MySQL 反查与一致性兜底（不触发真实 Milvus 连接）。</p>
 */
class VectorSearchServiceTest {

    private VectorStore vectorStore;
    private FaqMapper faqMapper;
    private VectorSearchService searchService;

    private Document doc(String id, String text) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("faq_id", id);
        return new Document(id, text, metadata);
    }

    @BeforeEach
    void setUp() {
        vectorStore = Mockito.mock(VectorStore.class);
        faqMapper = Mockito.mock(FaqMapper.class);
        searchService = new VectorSearchService(vectorStore, faqMapper);
    }

    @Test
    void testSearchFAQHit() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc("f1", "运费多少")));
        when(faqMapper.selectById("f1")).thenReturn(new Faq("f1", 1, "运费多少", "满99包邮", 1, 0));

        Faq faq = searchService.searchFAQ("运费怎么算");

        assertNotNull(faq);
        assertEquals("满99包邮", faq.getAnswer());
        verify(faqMapper).incrementUseCount("f1");
    }

    @Test
    void testSearchFAQMiss() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        assertNull(searchService.searchFAQ("没有匹配的问题"));
        verify(faqMapper, never()).incrementUseCount(Mockito.anyString());
    }

    @Test
    void testSearchFAQMySqlMissingTreatedAsMiss() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc("ghost", "幽灵")));
        when(faqMapper.selectById("ghost")).thenReturn(null);

        assertNull(searchService.searchFAQ("幽灵问题"));
        verify(faqMapper, never()).incrementUseCount(Mockito.anyString());
    }

    @Test
    void testBuildKnowledgeContextHit() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc("f1", "退换货")));
        when(faqMapper.selectById("f1")).thenReturn(new Faq("f1", 1, "退换货", "7天内可退", 1, 0));

        String context = searchService.buildKnowledgeContext("怎么退换货");

        assertTrue(context.contains("Q: 退换货"));
        assertTrue(context.contains("A: 7天内可退"));
    }

    @Test
    void testBuildKnowledgeContextEmpty() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        assertEquals("（无）", searchService.buildKnowledgeContext("随便问问"));
    }

    @Test
    void testSearchPassesStrategyParams() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        searchService.search("问题", SearchStrategy.CONTEXT_INJECTION);

        var captor = org.mockito.ArgumentCaptor.forClass(
                org.springframework.ai.vectorstore.SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());
        assertEquals(3, captor.getValue().getTopK());
        assertEquals(0.5D, captor.getValue().getSimilarityThreshold());
    }
}
