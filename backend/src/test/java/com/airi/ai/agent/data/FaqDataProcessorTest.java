package com.airi.ai.agent.data;

import com.airi.ai.agent.pojo.Faq;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FaqDataProcessor 单元测试
 *
 * <p>验证清洗（trim/空值/长度/默认值）与转换（metadata 结构）逻辑。</p>
 */
class FaqDataProcessorTest {

    private final FaqDataProcessor processor = new FaqDataProcessor();

    @Test
    void testCleanTrimsAndDefaults() {
        Faq faq = new Faq();
        faq.setQuestion("  商品什么时候发货？  ");
        faq.setAnswer("  付款后24小时内发货  ");

        assertTrue(processor.clean(faq));
        assertEquals("商品什么时候发货？", faq.getQuestion());
        assertEquals("付款后24小时内发货", faq.getAnswer());
        assertNotNull(faq.getId());
        assertEquals(1, faq.getStatus());
        assertEquals(0, faq.getUseCount());
    }

    @Test
    void testCleanBlankQuestionRejected() {
        Faq faq = new Faq();
        faq.setQuestion("   ");
        faq.setAnswer("答案");
        assertFalse(processor.clean(faq));
    }

    @Test
    void testCleanBlankAnswerRejected() {
        Faq faq = new Faq();
        faq.setQuestion("问题");
        faq.setAnswer(null);
        assertFalse(processor.clean(faq));
    }

    @Test
    void testCleanNullRejected() {
        assertFalse(processor.clean(null));
    }

    @Test
    void testCleanOverLengthRejected() {
        Faq faq = new Faq();
        faq.setQuestion("问".repeat(201));
        faq.setAnswer("答案");
        assertFalse(processor.clean(faq));
    }

    @Test
    void testToDocumentMetadata() {
        Faq faq = new Faq("f1", 2, "问题", "答案", 1, 0);
        Document doc = processor.toDocument(faq);

        assertEquals("f1", doc.getId());
        assertEquals("问题", doc.getText());
        assertEquals("f1", doc.getMetadata().get("faq_id"));
        assertEquals(2, doc.getMetadata().get("category_id"));
        assertEquals("问题", doc.getMetadata().get("question"));
        assertEquals("答案", doc.getMetadata().get("answer"));
    }
}
