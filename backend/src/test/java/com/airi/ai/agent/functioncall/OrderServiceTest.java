package com.airi.ai.agent.functioncall;

import com.airi.ai.agent.mapper.OrderMapper;
import com.airi.ai.agent.pojo.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderService 单元测试
 *
 * <p>用 Mockito 模拟 OrderMapper，验证 @Tool 工具方法的空值保护与按订单号/状态查询。</p>
 */
class OrderServiceTest {

    private OrderMapper orderMapper;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderMapper = Mockito.mock(OrderMapper.class);
        orderService = new OrderService(orderMapper);
    }

    @Test
    void testGetOrderByNoWithNullReturnsNull() {
        assertNull(orderService.getOrderByNo(null));
    }

    @Test
    void testGetOrderByNoWithBlankReturnsNull() {
        assertNull(orderService.getOrderByNo("   "));
    }

    @Test
    void testGetOrderByNoReturnsOrder() {
        Order order = new Order(1L, "AR100", "智能手机", 2999.0, "已签收", "2026-06-15 10:30:00");
        when(orderMapper.selectByNo("AR100")).thenReturn(order);

        Order result = orderService.getOrderByNo("AR100");

        assertNotNull(result);
        assertEquals("AR100", result.getOrderNo());
        verify(orderMapper).selectByNo("AR100");
    }

    @Test
    void testGetOrderByNoNotFoundReturnsNull() {
        when(orderMapper.selectByNo("AR999")).thenReturn(null);
        assertNull(orderService.getOrderByNo("AR999"));
    }

    @Test
    void testGetOrdersByStatusWithNullReturnsEmpty() {
        List<Order> result = orderService.getOrdersByStatus(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOrdersByStatusWithBlankReturnsEmpty() {
        List<Order> result = orderService.getOrdersByStatus("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOrdersByStatusReturnsList() {
        Order order = new Order(1L, "AR102", "笔记本电脑", 5999.0, "配送中", "2026-06-28 09:15:00");
        when(orderMapper.selectByStatus("配送中")).thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByStatus("配送中");

        assertEquals(1, result.size());
        assertEquals("配送中", result.get(0).getStatus());
    }

    @Test
    void testGetOrdersByStatusEmptyList() {
        when(orderMapper.selectByStatus("已取消")).thenReturn(Collections.emptyList());
        List<Order> result = orderService.getOrdersByStatus("已取消");
        assertTrue(result.isEmpty());
    }
}
