package com.airi.ai.agent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * FAQ 知识库实体类
 *
 * <p>对应数据库表 t_faq，使用纯 Lombok 注解，不依赖 MyBatis-Plus。</p>
 * <p>核心用途：</p>
 * <ul>
 *   <li>作为 MySQL 持久化的数据载体（由 FaqMapper 读写）</li>
 *   <li>question 字段是 Milvus 向量化的文本来源</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Faq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** FAQ ID（UUID 字符串） */
    private String id;

    /**
     * 问题分类
     * 1：订单问题  2：支付问题  3：商品问题  4：账户问题  5：其他问题
     */
    private Integer categoryId;

    /** 常见问题内容（用于 Milvus 向量化的文本） */
    private String question;

    /** 标准答案 */
    private String answer;

    /** 状态：0-禁用  1-启用 */
    private Integer status;

    /** 命中次数（默认 0，用于热度排序） */
    private Integer useCount = 0;
}
