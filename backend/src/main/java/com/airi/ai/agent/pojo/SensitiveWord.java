package com.airi.ai.agent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 敏感词实体类
 *
 * <p>对应数据库表 t_sensitive_word。</p>
 * <p>type 字段取值：deny=黑名单（拦截）, allow=白名单（放过）。</p>
 * <p>字段读写由 Lombok @Data 生成 getter/setter，支持无参/全参构造。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWord implements Serializable {

    /** 序列化版本号（保证反序列化兼容） */
    private static final long serialVersionUID = 1L;

    /** 主键 ID（自增） */
    private Long id;
    /** 敏感词内容 */
    private String word;
    /** 类型：deny(黑名单) 或 allow(白名单) */
    private String type;
}
