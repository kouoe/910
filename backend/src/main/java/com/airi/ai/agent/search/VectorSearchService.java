package com.airi.ai.agent.search;

import com.airi.ai.agent.mapper.FaqMapper;
import com.airi.ai.agent.pojo.Faq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 向量检索服务（向量检索功能模块核心）
 *
 * <p>封装 Milvus 语义检索的完整链路，对上层屏蔽检索细节：</p>
 * <ul>
 *   <li><b>向量数据库连接</b>：直接使用 Spring AI {@link VectorStore} 抽象
 *       （底层为 spring-ai-starter-vector-store-milvus）；</li>
 *   <li><b>向量生成</b>：由 DashScope Embedding 模型自动完成；</li>
 *   <li><b>相似度计算</b>：Milvus 端 COSINE 相似度，配合 {@link SearchStrategy} 阈值过滤；</li>
 *   <li><b>检索优化</b>：双通道策略、MySQL 反查、一致性兜底、命中计数。</li>
 * </ul>
 */
@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    private final VectorStore vectorStore;
    private final FaqMapper faqMapper;

    public VectorSearchService(VectorStore vectorStore, FaqMapper faqMapper) {
        this.vectorStore = vectorStore;
        this.faqMapper = faqMapper;
    }

    /**
     * 精确命中检索：返回唯一最匹配的 FAQ
     *
     * <p>使用 {@link SearchStrategy#EXACT_MATCH}（topK=1、阈值 0.7）。
     * 命中后递增该 FAQ 的 useCount。未命中返回 null。</p>
     *
     * @param question 用户问题
     * @return 命中的 FAQ；无匹配返回 null
     */
    public Faq searchFAQ(String question) {
        List<Document> documents = search(question, SearchStrategy.EXACT_MATCH);
        if (documents.isEmpty()) {
            return null;
        }

        Document document = documents.get(0);
        Faq faq = faqMapper.selectById(document.getId().toString());
        if (faq == null) {
            log.warn("Milvus 中存在文档但 MySQL 中查不到：docId={}", document.getId());
            return null;
        }

        faqMapper.incrementUseCount(faq.getId());
        return faq;
    }

    /**
     * 构建 RAG 知识库上下文文本
     *
     * <p>使用 {@link SearchStrategy#CONTEXT_INJECTION}（topK=3、阈值 0.5）宽松召回，
     * 将相关 FAQ 拼装为 <code>Q: ... \nA: ...</code> 文本，注入 System Prompt 的
     * <code>{knowledge}</code> 占位符。检索不到时返回"（无）"。</p>
     *
     * @param question 用户问题
     * @return 知识库上下文文本；无相关内容返回"（无）"
     */
    public String buildKnowledgeContext(String question) {
        List<Document> documents = search(question, SearchStrategy.CONTEXT_INJECTION);
        if (documents.isEmpty()) {
            return "（无）";
        }

        StringBuilder sb = new StringBuilder();
        for (Document doc : documents) {
            Faq faq = faqMapper.selectById(doc.getId().toString());
            if (faq != null && StringUtils.hasText(faq.getQuestion()) && StringUtils.hasText(faq.getAnswer())) {
                sb.append("Q: ").append(faq.getQuestion()).append("\nA: ").append(faq.getAnswer()).append("\n\n");
            }
        }
        return sb.length() == 0 ? "（无）" : sb.toString().trim();
    }

    /**
     * 通用检索：按指定策略执行相似度检索
     *
     * <p>向量化、相似度计算、阈值过滤均由 VectorStore + Milvus 完成。</p>
     *
     * @param question 查询文本
     * @param strategy 检索策略（topK + 阈值）
     * @return 按相似度降序的 Document 列表（可能为空）
     */
    public List<Document> search(String question, SearchStrategy strategy) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(strategy.getTopK())
                .similarityThreshold(strategy.getSimilarityThreshold())
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }
}
