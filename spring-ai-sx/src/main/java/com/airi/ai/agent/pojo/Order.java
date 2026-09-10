package com.airi.ai.agent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单实体类
 *
 * <p>对应数据库表 t_order，用于 Function Calling 订单查询（通过 OrderMapper 读取 MySQL）。</p>
 * <p>说明：Spring AI 会自动将 @Tool 方法返回的 Order 对象序列化为 JSON，作为工具结果回传给大模型。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    private Long id;

    /** 订单编号（如 AR100） */
    private String orderNo;

    /** 商品名称 */
    private String productName;

    /** 订单金额（元） */
    private Double amount;

    /** 订单状态（待发货/配送中/已签收/待支付/已退货） */
    private String status;

    /** 下单时间 */
    private String createTime;
}
