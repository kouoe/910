package com.airi.ai.agent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户实体类
 *
 * <p>对应数据库表 t_user，用于商城登录/注册功能。
 * password 字段存储 SHA-256 加盐哈希（非明文）。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    private Long id;

    /** 用户名（登录账号，唯一） */
    private String username;

    /** 密码（SHA-256 加盐哈希） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 注册时间 */
    private String createTime;
}
