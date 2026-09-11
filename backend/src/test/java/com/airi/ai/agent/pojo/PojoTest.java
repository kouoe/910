package com.airi.ai.agent.pojo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体类属性读写单元测试
 *
 * <p>覆盖 Faq/SensitiveWord/Order 的 getter/setter、全参构造与无参构造。</p>
 */
class PojoTest {

    @Test
    void testFaqGettersAndSetters() {
        Faq faq = new Faq();
        faq.setId("f1");
        faq.setCategoryId(1);
        faq.setQuestion("问题");
        faq.setAnswer("答案");
        faq.setStatus(1);
        faq.setUseCount(5);

        assertEquals("f1", faq.getId());
        assertEquals(1, faq.getCategoryId());
        assertEquals("问题", faq.getQuestion());
        assertEquals("答案", faq.getAnswer());
        assertEquals(1, faq.getStatus());
        assertEquals(5, faq.getUseCount());
    }

    @Test
    void testFaqAllArgsConstructor() {
        Faq faq = new Faq("f1", 1, "问题", "答案", 1, 0);
        assertEquals("f1", faq.getId());
        assertEquals(0, faq.getUseCount());
    }

    @Test
    void testFaqNoArgsConstructorDefaultUseCount() {
        Faq faq = new Faq();
        assertEquals(0, faq.getUseCount());
    }

    @Test
    void testSensitiveWordGettersAndSetters() {
        SensitiveWord sw = new SensitiveWord();
        sw.setId(1L);
        sw.setWord("假货");
        sw.setType("deny");

        assertEquals(1L, sw.getId());
        assertEquals("假货", sw.getWord());
        assertEquals("deny", sw.getType());
    }

    @Test
    void testSensitiveWordAllArgsConstructor() {
        SensitiveWord sw = new SensitiveWord(1L, "假货", "deny");
        assertEquals(1L, sw.getId());
        assertEquals("deny", sw.getType());
    }

    @Test
    void testSensitiveWordNoArgsConstructor() {
        SensitiveWord sw = new SensitiveWord();
        assertNull(sw.getId());
        assertNull(sw.getWord());
        assertNull(sw.getType());
    }

    @Test
    void testOrderGettersAndSetters() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("AR100");
        order.setProductName("智能手机");
        order.setAmount(2999.0);
        order.setStatus("已签收");
        order.setCreateTime("2026-06-15 10:30:00");

        assertEquals(1L, order.getId());
        assertEquals("AR100", order.getOrderNo());
        assertEquals("智能手机", order.getProductName());
        assertEquals(2999.0, order.getAmount());
        assertEquals("已签收", order.getStatus());
        assertEquals("2026-06-15 10:30:00", order.getCreateTime());
    }

    @Test
    void testOrderAllArgsConstructor() {
        Order order = new Order(1L, "AR100", "智能手机", 2999.0, "已签收", "2026-06-15 10:30:00");
        assertEquals("AR100", order.getOrderNo());
    }

    @Test
    void testOrderNoArgsConstructor() {
        Order order = new Order();
        assertNull(order.getId());
        assertNull(order.getOrderNo());
    }
}
