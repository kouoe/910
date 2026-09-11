package com.airi.ai.agent.mapper;

import com.airi.ai.agent.pojo.Order;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单 Mapper 接口（纯 MyBatis 注解方式）
 *
 * <p>供 Function Calling 订单查询使用。</p>
 * <p>禁止使用 MyBatis-Plus 的 BaseMapper 继承方式。</p>
 */
@Repository
public interface OrderMapper {

    /**
     * 根据订单号查询订单（不区分大小写）
     *
     * @param orderNo 订单编号
     * @return 订单对象，未找到返回 null
     */
    @Select("""
            SELECT id, order_no, product_name, amount, status, create_time
            FROM t_order
            WHERE UPPER(order_no) = UPPER(#{orderNo})
            """)
    @Results(id = "orderResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "product_name", property = "productName"),
            @Result(column = "amount", property = "amount"),
            @Result(column = "status", property = "status"),
            @Result(column = "create_time", property = "createTime")
    })
    Order selectByNo(@Param("orderNo") String orderNo);

    /**
     * 按订单状态模糊查询订单列表
     *
     * @param status 订单状态关键字（如：待发货、配送中）
     * @return 匹配的订单列表
     */
    @Select("""
            SELECT id, order_no, product_name, amount, status, create_time
            FROM t_order
            WHERE status LIKE CONCAT('%', #{status}, '%')
            ORDER BY create_time DESC
            """)
    @ResultMap("orderResultMap")
    List<Order> selectByStatus(@Param("status") String status);
}
