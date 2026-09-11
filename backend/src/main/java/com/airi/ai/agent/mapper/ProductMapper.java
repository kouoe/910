package com.airi.ai.agent.mapper;

import com.airi.ai.agent.pojo.Product;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品 Mapper 接口（纯 MyBatis 注解方式）
 *
 * <p>供 Function Calling 商品查询使用，所有 SQL 通过注解显式声明，
 * 不继承 MyBatis-Plus 的 BaseMapper。</p>
 */
@Repository
public interface ProductMapper {

    /**
     * 根据商品名称精确查询（用于价格查询）
     *
     * @param name 商品名称（如 智能手机）
     * @return 商品对象，未找到返回 null
     */
    @Select("""
            SELECT id, product_no, name, category, price, stock, sales, status, create_time
            FROM t_product
            WHERE name = #{name} AND status = 1
            """)
    @Results(id = "productResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "product_no", property = "productNo"),
            @Result(column = "name", property = "name"),
            @Result(column = "category", property = "category"),
            @Result(column = "price", property = "price"),
            @Result(column = "stock", property = "stock"),
            @Result(column = "sales", property = "sales"),
            @Result(column = "status", property = "status"),
            @Result(column = "create_time", property = "createTime")
    })
    Product selectByName(@Param("name") String name);

    /**
     * 按关键字模糊查询商品（用于商品搜索）
     *
     * @param keyword 商品名称关键字
     * @return 匹配的商品列表（按销量降序）
     */
    @Select("""
            SELECT id, product_no, name, category, price, stock, sales, status, create_time
            FROM t_product
            WHERE name LIKE CONCAT('%', #{keyword}, '%') AND status = 1
            ORDER BY sales DESC
            """)
    @ResultMap("productResultMap")
    List<Product> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 按分类查询商品
     *
     * @param category 商品分类（如 电脑办公）
     * @return 匹配的商品列表（按销量降序）
     */
    @Select("""
            SELECT id, product_no, name, category, price, stock, sales, status, create_time
            FROM t_product
            WHERE category = #{category} AND status = 1
            ORDER BY sales DESC
            """)
    @ResultMap("productResultMap")
    List<Product> selectByCategory(@Param("category") String category);

    /**
     * 按价格区间查询商品
     *
     * @param minPrice 最低价（可为 null）
     * @param maxPrice 最高价（可为 null）
     * @return 匹配的商品列表（按价格升序）
     */
    @Select("""
            <script>
            SELECT id, product_no, name, category, price, stock, sales, status, create_time
            FROM t_product
            <where>
              status = 1
              <if test='minPrice != null'>AND price &gt;= #{minPrice}</if>
              <if test='maxPrice != null'>AND price &lt;= #{maxPrice}</if>
            </where>
            ORDER BY price ASC
            </script>
            """)
    @ResultMap("productResultMap")
    List<Product> selectByPriceRange(@Param("minPrice") Double minPrice,
                                     @Param("maxPrice") Double maxPrice);

    /**
     * 查询热销商品（按累计销量降序取前 N 名）
     *
     * @param limit 返回条数
     * @return 热销商品列表
     */
    @Select("""
            SELECT id, product_no, name, category, price, stock, sales, status, create_time
            FROM t_product
            WHERE status = 1
            ORDER BY sales DESC
            LIMIT #{limit}
            """)
    @ResultMap("productResultMap")
    List<Product> selectHotProducts(@Param("limit") int limit);

    /**
     * 查询全部在售商品（商城首页商品列表）
     *
     * @return 全部在售商品（按销量降序）
     */
    @Select("""
            SELECT id, product_no, name, category, price, stock, sales, status, create_time
            FROM t_product
            WHERE status = 1
            ORDER BY sales DESC
            """)
    @ResultMap("productResultMap")
    List<Product> selectAll();
}
