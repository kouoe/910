package com.airi.ai.agent.mapper;

import com.airi.ai.agent.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * 用户 Mapper 接口（纯 MyBatis 注解方式）
 *
 * <p>提供用户注册（插入）与登录（按用户名查询）能力。</p>
 */
@Repository
public interface UserMapper {

    /**
     * 插入新用户（注册）
     *
     * @param user 用户对象（username/password/nickname/phone）
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO t_user (username, password, nickname, phone, create_time)
            VALUES (#{username}, #{password}, #{nickname}, #{phone}, NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 按用户名查询用户（用于登录校验与注册查重）
     *
     * @param username 用户名
     * @return 用户对象，未找到返回 null
     */
    @Select("""
            SELECT id, username, password, nickname, phone, create_time
            FROM t_user
            WHERE username = #{username}
            """)
    @Results(id = "userResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "username", property = "username"),
            @Result(column = "password", property = "password"),
            @Result(column = "nickname", property = "nickname"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "create_time", property = "createTime")
    })
    User selectByUsername(@Param("username") String username);
}
