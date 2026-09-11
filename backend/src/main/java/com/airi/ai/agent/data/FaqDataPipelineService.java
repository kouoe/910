package com.airi.ai.agent.data;

import com.airi.ai.agent.exception.BusinessException;
import com.airi.ai.agent.mapper.FaqMapper;
import com.airi.ai.agent.pojo.Faq;
import com.airi.ai.agent.result.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * FAQ 数据流水线服务（数据操作模块核心）
 *
 * <p>与 FaqServiceImpl（单条 CRUD）互补，面向批量数据维护场景，
 * 提供采集→清洗→转换→存储→更新的完整流程。</p>
 */
@Service
public class FaqDataPipelineService {

    private static final Logger log = LoggerFactory.getLogger(FaqDataPipelineService.class);

    /** 全量同步分页大小 */
    private static final int PAGE_SIZE = 100;

    /** 批量入库批次大小 */
    private static final int BATCH_SIZE = 100;

    private final FaqMapper faqMapper;
    private final VectorStore vectorStore;
    private final FaqDataProcessor faqDataProcessor;

    public FaqDataPipelineService(FaqMapper faqMapper,
                                  VectorStore vectorStore,
                                  FaqDataProcessor faqDataProcessor) {
        this.faqMapper = faqMapper;
        this.vectorStore = vectorStore;
        this.faqDataProcessor = faqDataProcessor;
    }

    /** 同步结果统计 */
    public record SyncResult(int total, int cleaned, int discarded, int stored) {
    }

    /**
     * 全量同步：采集 → 清洗 → 转换 → 清场 → 批量存储
     *
     * @return 同步统计
     */
    public SyncResult runFullSync() {
        // 1. 采集（分页从 MySQL 读取全部 FAQ）
        List<Faq> faqs = collectFaqs();
        if (faqs.isEmpty()) {
            throw new BusinessException(ErrorCode.FAQ_DATA_EMPTY);
        }

        // 2. 清洗 + 转换
        List<Document> documents = cleanAndTransform(faqs);
        if (documents.isEmpty()) {
            throw new BusinessException(ErrorCode.FAQ_DATA_EMPTY);
        }

        // 3. 清场：删除 Milvus 中全部 FAQ 向量（含孤儿向量）
        cleanupAllFaqVectors();

        // 4. 分批入库
        int stored = batchStore(documents);

        SyncResult result = new SyncResult(faqs.size(), documents.size(),
                faqs.size() - documents.size(), stored);
        log.info("FAQ 全量同步完成：total={}, cleaned={}, discarded={}, stored={}",
                result.total(), result.cleaned(), result.discarded(), result.stored());
        return result;
    }

    /**
     * 增量更新单条 FAQ 的向量副本
     *
     * <p>流程：清洗校验 → 删除 Milvus 旧向量 → 插入新向量。
     * 清洗不通过抛业务异常；Milvus 操作失败仅记日志（可由全量同步对账恢复）。</p>
     *
     * @param faq 待更新的 FAQ（MySQL 中已持久化）
     */
    public void runIncrementalUpdate(Faq faq) {
        if (!faqDataProcessor.clean(faq)) {
            throw new BusinessException(ErrorCode.FAQ_CONTENT_EMPTY);
        }

        try {
            if (StringUtils.hasText(faq.getId())) {
                vectorStore.delete(List.of(faq.getId()));
            }
            vectorStore.add(List.of(faqDataProcessor.toDocument(faq)));
            log.info("FAQ 向量增量更新完成：id={}", faq.getId());
        } catch (Exception e) {
            log.error("FAQ 向量增量更新失败：id={}, 原因={}，可调用全量同步对账恢复",
                    faq.getId(), e.getMessage());
        }
    }

    /**
     * 采集：分页读取 MySQL 中全部 FAQ
     *
     * @return 全量 FAQ 列表
     */
    public List<Faq> collectFaqs() {
        List<Faq> all = new ArrayList<>();
        int total = faqMapper.count(null, null);
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;

        for (int page = 1; page <= totalPages; page++) {
            int offset = (page - 1) * PAGE_SIZE;
            all.addAll(faqMapper.selectByPage(offset, PAGE_SIZE, null, null));
        }
        log.info("FAQ 采集完成：共 {} 条", all.size());
        return all;
    }

    /**
     * 清洗 + 转换：批量处理 FAQ，剔除脏数据
     *
     * @param faqs 从 MySQL 采集的原始 FAQ 列表
     * @return 通过清洗并完成转换的 Document 列表
     */
    public List<Document> cleanAndTransform(List<Faq> faqs) {
        List<Document> documents = new ArrayList<>();
        int discarded = 0;

        for (Faq faq : faqs) {
            if (faqDataProcessor.clean(faq)) {
                documents.add(faqDataProcessor.toDocument(faq));
            } else {
                discarded++;
                log.warn("FAQ 数据清洗未通过，已丢弃：id={}, question={}", faq.getId(), faq.getQuestion());
            }
        }

        if (discarded > 0) {
            log.warn("FAQ 清洗共丢弃 {} 条脏数据", discarded);
        }
        return documents;
    }

    /**
     * 存储：将 Document 分批写入 Milvus
     *
     * @param documents 待入库的 Document 列表
     * @return 实际成功入库的条数（失败批次仅记日志，可对账重试）
     */
    public int batchStore(List<Document> documents) {
        int stored = 0;
        for (int i = 0; i < documents.size(); i += BATCH_SIZE) {
            List<Document> batch = documents.subList(i, Math.min(i + BATCH_SIZE, documents.size()));
            try {
                vectorStore.add(batch);
                stored += batch.size();
                log.info("Milvus 批量入库：{}/{} 条", stored, documents.size());
            } catch (Exception e) {
                log.error("Milvus 批量入库失败：批次[{}, {})，原因={}，可对账重试",
                        i, i + batch.size(), e.getMessage());
            }
        }
        return stored;
    }

    /** 清场：删除 Milvus 集合中全部 FAQ 向量（metadata 含 faq_id 的记录） */
    private void cleanupAllFaqVectors() {
        try {
            vectorStore.delete(new FilterExpressionBuilder()
                    .ne("faq_id", "")
                    .build());
            log.info("已清理 Milvus 中全部 FAQ 向量");
        } catch (Exception e) {
            log.error("清理 Milvus FAQ 向量异常：{}", e.getMessage());
        }
    }
}
