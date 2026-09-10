package com.airi.ai.agent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品实体类
 *
 * <p>对应数据库表 t_product，用于 Function Calling 商品相关查询
 * （商品搜索、价格查询、分类查询、热销排行），通过 {@code ProductMapper} 读取 MySQL。</p>
 * <p>说明：Spring AI 会自动将 @Tool 方法返回的 Product 对象序列化为 JSON，
 * 作为工具结果回传给大模型。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    private Long id;

    /** 商品编号（如 P001） */
    private String productNo;

    /** 商品名称 */
    private String name;

    /** 商品分类（手机数码/电脑办公/智能穿戴/智能家居/网络设备） */
    private String category;

    /** 商品价格（元） */
    private Double price;

    /** 库存数量 */
    private Integer stock;

    /** 累计销量 */
    private Integer sales;

    /** 状态：0-下架  1-在售 */
    private Integer status;

    /** 上架时间 */
    private String createTime;
}
