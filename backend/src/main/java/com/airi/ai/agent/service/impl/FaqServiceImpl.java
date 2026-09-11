package com.airi.ai.agent.service.impl;

import com.airi.ai.agent.data.FaqDataPipelineService;
import com.airi.ai.agent.exception.BusinessException;
import com.airi.ai.agent.mapper.FaqMapper;
import com.airi.ai.agent.pojo.Faq;
import com.airi.ai.agent.result.ErrorCode;
import com.airi.ai.agent.service.IFaqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * FAQ 服务实现类
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>MySQL 数据持久化（主数据源）</li>
 *   <li>Milvus 向量检索副本同步（委托 {@link FaqDataPipelineService}）</li>
 *   <li>增删改操作双向同步保证数据一致性</li>
 * </ul>
 *
 * <p>关键设计原则（v2 优化）：</p>
 * <ul>
 *   <li><b>MySQL 为主、Milvus 尽力同步</b>：Milvus 是外部系统，无法参与 MySQL 本地事务回滚。
 *       因此 <code>@Transactional</code> 只包裹 MySQL 操作；Milvus 同步失败仅记录错误日志、不阻断主流程，
 *       可随时调用 <code>/syncToMilvus</code> 全量对账重建。</li>
 *   <li><b>全量同步先清理再重建</b>：由 {@link FaqDataPipelineService} 负责"清场 + 批量重建"，
 *       避免"MySQL 已删但 Milvus 残留"的孤儿向量永不被清理。</li>
 * </ul>
 */
@Service
public class FaqServiceImpl implements IFaqService {

    private static final Logger log = LoggerFactory.getLogger(FaqServiceImpl.class);

    private final FaqMapper faqMapper;
    private final VectorStore vectorStore;
    private final FaqDataPipelineService faqDataPipelineService;

    /**
     * 构造注入：FaqMapper、VectorStore、FaqDataPipelineService
     *
     * @param faqMapper              FAQ Mapper（MySQL 读写）
     * @param vectorStore            Spring AI 向量存储（用于删除单个 FAQ 向量）
     * @param faqDataPipelineService FAQ 数据流水线（批量同步 + 增量更新）
     */
    public FaqServiceImpl(FaqMapper faqMapper,
                          VectorStore vectorStore,
                          FaqDataPipelineService faqDataPipelineService) {
        this.faqMapper = faqMapper;
        this.vectorStore = vectorStore;
        this.faqDataPipelineService = faqDataPipelineService;
    }

    /**
     * 新增 FAQ（MySQL 主写入 + Milvus 尽力同步）
     *
     * @param faq FAQ 实体（id 为空时由服务端生成 UUID）
     */
    @Override
    public void add(Faq faq) {
        // 参数校验：问题与答案均不可为空
        if (!StringUtils.hasText(faq.getQuestion()) || !StringUtils.hasText(faq.getAnswer())) {
            throw new BusinessException(ErrorCode.FAQ_CONTENT_EMPTY);
        }

        // 生成 UUID 作为 FAQ ID
        if (!StringUtils.hasText(faq.getId())) {
            faq.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        // 默认状态为启用
        if (faq.getStatus() == null) {
            faq.setStatus(1);
        }
        // 默认命中次数为 0
        if (faq.getUseCount() == null) {
            faq.setUseCount(0);
        }

        // 1. 写入 MySQL（主数据源）
        faqMapper.insert(faq);
        // 2. 尽力同步 Milvus（失败不阻断，可对账）
        syncToMilvusWithLog(() -> faqDataPipelineService.runIncrementalUpdate(faq), "新增 FAQ 向量同步");

        log.info("新增 FAQ：id={}, question={}", faq.getId(), faq.getQuestion());
    }

    /**
     * 删除 FAQ（MySQL 删除 + Milvus 尽力同步）
     *
     * @param faqId FAQ ID
     */
    @Override
    public void deleteFaq(String faqId) {
        if (!StringUtils.hasText(faqId)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        // 1. 从 MySQL 删除（主数据源）
        int rows = faqMapper.deleteById(faqId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.FAQ_NOT_FOUND);
        }

        // 2. 尽力从 Milvus 删除向量
        syncToMilvusWithLog(() -> deleteFaqVector(faqId), "删除 FAQ 向量同步");

        log.info("删除 FAQ：id={}", faqId);
    }

    /**
     * 更新 FAQ（MySQL 更新 + Milvus 旧向量删除 + 新向量入库）
     *
     * @param faq FAQ 实体（必须携带 id）
     */
    @Override
    public void updateFaq(Faq faq) {
        if (!StringUtils.hasText(faq.getId())) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        // 先校验 FAQ 是否存在
        Faq existing = faqMapper.selectById(faq.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.FAQ_NOT_FOUND);
        }

        // 1. 更新 MySQL（主数据源）
        faqMapper.updateById(faq);
        // 2. 尽力同步 Milvus：删旧向量 → 插新向量（保证向量内容与最新 FAQ 一致）
        syncToMilvusWithLog(() -> faqDataPipelineService.runIncrementalUpdate(faq), "更新 FAQ 向量同步");

        log.info("更新 FAQ：id={}, question={}", faq.getId(), faq.getQuestion());
    }

    /**
     * 分页查询 FAQ
     *
     * @param page       页码（从 1 开始）
     * @param size       每页条数
     * @param categoryId 分类 ID（可选）
     * @param keyword    关键字（可选）
     * @return 当前页 FAQ 列表
     */
    @Override
    public List<Faq> getFaqByPage(int page, int size, Integer categoryId, String keyword) {
        int offset = (page - 1) * size;
        return faqMapper.selectByPage(offset, size, categoryId, keyword);
    }

    /**
     * 统计 FAQ 总数（分页查询的总记录数）
     *
     * @param categoryId 分类 ID（可选）
     * @param keyword    关键字（可选）
     * @return 符合条件的记录总数
     */
    @Override
    public int countFaq(Integer categoryId, String keyword) {
        return faqMapper.count(categoryId, keyword);
    }

    /**
     * 根据 ID 查询 FAQ
     *
     * @param faqId FAQ ID
     * @return FAQ 对象，未找到返回 null
     */
    @Override
    public Faq getFaqById(String faqId) {
        if (!StringUtils.hasText(faqId)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        return faqMapper.selectById(faqId);
    }

    /**
     * 全量对账同步 MySQL → Milvus
     *
     * <p>委托 {@link FaqDataPipelineService} 执行：采集 → 清洗 → 转换 → 清场 → 批量入库。</p>
     */
    @Override
    public void syncMySQLToVector() {
        faqDataPipelineService.runFullSync();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从 Milvus 删除 FAQ 向量
     *
     * @param faqId FAQ ID（即 Document.id）
     */
    private void deleteFaqVector(String faqId) {
        vectorStore.delete(List.of(faqId));
    }

    /**
     * 尽力同步 Milvus：捕获异常仅记日志，不阻断主流程
     *
     * @param action Milvus 同步动作
     * @param desc   同步动作描述（用于日志）
     */
    private void syncToMilvusWithLog(Runnable action, String desc) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("{}失败：{}，可稍后调用 /syncToMilvus 全量对账恢复", desc, e.getMessage());
        }
    }
}
