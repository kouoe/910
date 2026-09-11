package com.airi.ai.agent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 购物车条目实体类
 *
 * <p>对应数据库表 t_cart。查询购物车时通过 JOIN t_product 附带商品名称、
 * 分类、价格、库存等字段，便于前端直接展示。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 商品编号（如 P001） */
    private String productNo;

    /** 购买数量 */
    private Integer quantity;

    /** 商品名称（JOIN t_product 得到） */
    private String productName;

    /** 商品分类（JOIN t_product 得到） */
    private String category;

    /** 商品价格（JOIN t_product 得到） */
    private Double price;

    /** 库存数量（JOIN t_product 得到） */
    private Integer stock;
}
