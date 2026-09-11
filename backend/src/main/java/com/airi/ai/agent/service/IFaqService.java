package com.airi.ai.agent.service;

import com.airi.ai.agent.pojo.Faq;

import java.util.List;

/**
 * FAQ 知识库服务接口
 *
 * <p>定义 FAQ 的 CRUD 操作及 MySQL ↔ Milvus 数据同步。</p>
 * <p>设计原则：MySQL 为主数据源，Milvus 为向量检索副本，采用"尽力同步"策略。</p>
 */
public interface IFaqService {

    /** 新增 FAQ（同时写入 MySQL 和 Milvus） */
    void add(Faq faq);

    /** 删除 FAQ（同时从 MySQL 和 Milvus 移除） */
    void deleteFaq(String faqId);

    /** 更新 FAQ（MySQL 更新 + Milvus 旧向量删除 + 新向量入库） */
    void updateFaq(Faq faq);

    /**
     * 分页查询 FAQ
     *
     * @param page       页码（从1开始）
     * @param size       每页条数
     * @param categoryId 分类 ID（可选）
     * @param keyword    关键字（可选，匹配问题内容）
     * @return FAQ 列表
     */
    List<Faq> getFaqByPage(int page, int size, Integer categoryId, String keyword);

    /**
     * 统计 FAQ 总数
     *
     * @param categoryId 分类 ID（可选）
     * @param keyword    关键字（可选）
     * @return 符合条件的记录总数
     */
    int countFaq(Integer categoryId, String keyword);

    /**
     * 根据 ID 查询 FAQ
     *
     * @param faqId FAQ ID
     * @return FAQ 对象，未找到返回 null
     */
    Faq getFaqById(String faqId);

    /** 全量同步：将 MySQL 中所有 FAQ 批量写入 Milvus */
    void syncMySQLToVector();
}
