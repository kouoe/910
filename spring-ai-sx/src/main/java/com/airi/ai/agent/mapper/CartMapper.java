package com.airi.ai.agent.mapper;

import com.airi.ai.agent.pojo.CartItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 购物车 Mapper 接口（纯 MyBatis 注解方式）
 *
 * <p>提供购物车条目的加入、查询、改数量、删除、清空能力。
 * 加入使用 ON DUPLICATE KEY UPDATE，重复商品自动累加数量。</p>
 */
@Repository
public interface CartMapper {

    /**
     * 加入购物车（重复商品自动累加数量）
     *
     * @param userId    用户 ID
     * @param productNo 商品编号
     * @param quantity  数量
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO t_cart (user_id, product_no, quantity, create_time)
            VALUES (#{userId}, #{productNo}, #{quantity}, NOW())
            ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}
            """)
    int add(@Param("userId") Long userId,
            @Param("productNo") String productNo,
            @Param("quantity") int quantity);

    /**
     * 查询某用户的购物车（JOIN 商品表附带商品信息）
     *
     * @param userId 用户 ID
     * @return 购物车条目列表（按加入时间倒序）
     */
    @Select("""
            SELECT c.id, c.user_id, c.product_no, c.quantity,
                   p.name AS product_name, p.category, p.price, p.stock
            FROM t_cart c
            JOIN t_product p ON c.product_no = p.product_no
            WHERE c.user_id = #{userId}
            ORDER BY c.create_time DESC
            """)
    @Results(id = "cartItemResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "product_no", property = "productNo"),
            @Result(column = "quantity", property = "quantity"),
            @Result(column = "product_name", property = "productName"),
            @Result(column = "category", property = "category"),
            @Result(column = "price", property = "price"),
            @Result(column = "stock", property = "stock")
    })
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    /**
     * 修改购物车条目数量
     *
     * @param userId    用户 ID
     * @param productNo 商品编号
     * @param quantity  新数量
     * @return 影响行数
     */
    @Update("""
            UPDATE t_cart
            SET quantity = #{quantity}
            WHERE user_id = #{userId} AND product_no = #{productNo}
            """)
    int updateQuantity(@Param("userId") Long userId,
                       @Param("productNo") String productNo,
                       @Param("quantity") int quantity);

    /**
     * 删除购物车单条商品
     *
     * @param userId    用户 ID
     * @param productNo 商品编号
     * @return 影响行数
     */
    @Delete("""
            DELETE FROM t_cart
            WHERE user_id = #{userId} AND product_no = #{productNo}
            """)
    int delete(@Param("userId") Long userId, @Param("productNo") String productNo);

    /**
     * 清空用户购物车
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Delete("""
            DELETE FROM t_cart WHERE user_id = #{userId}
            """)
    int clear(@Param("userId") Long userId);
}
