package com.airi.ai.agent.functioncall;

import com.airi.ai.agent.mapper.OrderMapper;
import com.airi.ai.agent.pojo.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 订单查询 Function Calling 服务
 *
 * <p>使用 Spring AI 1.0 新版 @Tool 注解声明 AI 可调用的工具函数。</p>
 * <p>订单数据存储在 MySQL t_order 表中，通过 {@link OrderMapper} 查询。</p>
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderMapper orderMapper;

    /**
     * 构造注入订单 Mapper
     *
     * @param orderMapper 订单 Mapper（查询 MySQL t_order 表）
     */
    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 根据订单号查询订单详情
     *
     * @param orderNo 订单号（如 AR100）
     * @return 订单信息，未找到返回 null
     */
    @Tool(description = "根据订单号查询订单详情，输入订单号如 AR100 返回该订单的完整信息")
    public Order getOrderByNo(@ToolParam(description = "订单编号，如 AR100") String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return null;
        }
        return orderMapper.selectByNo(orderNo);
    }

    /**
     * 按订单状态查询订单列表
     *
     * @param status 订单状态（如：待发货、配送中、已签收、待支付、已退货）
     * @return 匹配的订单列表
     */
    @Tool(description = "根据订单状态查询订单列表，状态包括：待发货、配送中、已签收、待支付、已退货")
    public List<Order> getOrdersByStatus(@ToolParam(description = "订单状态，如：待发货、配送中、已签收") String status) {
        if (!StringUtils.hasText(status)) {
            return Collections.emptyList();
        }
        List<Order> orders = orderMapper.selectByStatus(status);
        log.info("按状态查询订单：status={}, 命中 {} 条", status, orders.size());
        return orders;
    }
}
