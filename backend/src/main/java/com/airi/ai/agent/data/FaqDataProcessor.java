package com.airi.ai.agent.data;

import com.airi.ai.agent.pojo.Faq;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FAQ 数据处理器（数据清洗与转换）
 *
 * <p>位于数据流水线的清洗/转换环节，提供纯函数式能力：</p>
 * <ul>
 *   <li><b>清洗（clean）</b>：去除首尾空白、剔除空白答案、校验必填字段；</li>
 *   <li><b>转换（toDocument）</b>：将 FAQ 实体转换为 Milvus 可入库的 Spring AI
 *       {@link Document}，metadata 结构与项目既有实现完全一致（faq_id/category_id/question/answer）；</li>
 *   <li><b>幂等与安全</b>：转换前自动补全 id（UUID）、status（1）、useCount（0），保证入库数据完整性。</li>
 * </ul>
 */
@Component
public class FaqDataProcessor {

    /** 问题最大长度（超出视为异常数据，丢弃并告警） */
    private static final int MAX_QUESTION_LENGTH = 200;

    /** 答案最大长度（超出视为异常数据，丢弃并告警） */
    private static final int MAX_ANSWER_LENGTH = 2000;

    /**
     * 清洗并校验单条 FAQ
     *
     * @param faq 待清洗的 FAQ（方法内直接修改）
     * @return true=通过校验可继续处理；false=脏数据需丢弃
     */
    public boolean clean(Faq faq) {
        if (faq == null) {
            return false;
        }

        // 1. 去首尾空白
        if (faq.getQuestion() != null) {
            faq.setQuestion(faq.getQuestion().trim());
        }
        if (faq.getAnswer() != null) {
            faq.setAnswer(faq.getAnswer().trim());
        }

        // 2. 必填字段非空校验
        if (!StringUtils.hasText(faq.getQuestion()) || !StringUtils.hasText(faq.getAnswer())) {
            return false;
        }

        // 3. 长度校验（超长问题/答案不适合向量化与生成）
        if (faq.getQuestion().length() > MAX_QUESTION_LENGTH
                || faq.getAnswer().length() > MAX_ANSWER_LENGTH) {
            return false;
        }

        // 4. 默认值补全
        if (!StringUtils.hasText(faq.getId())) {
            faq.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (faq.getStatus() == null) {
            faq.setStatus(1);
        }
        if (faq.getUseCount() == null) {
            faq.setUseCount(0);
        }
        return true;
    }

    /**
     * 将清洗后的 FAQ 转换为 Milvus 入库 Document
     *
     * <p>Document.id = faq.id（保证 MySQL 与 Milvus 一一对应）；
     * Document.content = faq.question（向量化的文本主体）。</p>
     *
     * @param faq 已通过 {@link #clean(Faq)} 校验的 FAQ
     * @return 可入库的 Document
     */
    public Document toDocument(Faq faq) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("faq_id", faq.getId());
        // Spring AI 的 Document 要求 metadata 值不能为 null，category_id 为空时跳过
        if (faq.getCategoryId() != null) {
            metadata.put("category_id", faq.getCategoryId());
        }
        metadata.put("question", faq.getQuestion());
        metadata.put("answer", faq.getAnswer());
        return new Document(faq.getId(), faq.getQuestion(), metadata);
    }
}
