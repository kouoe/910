package com.airi.ai.agent.controller;

import com.airi.ai.agent.mapper.UserMapper;
import com.airi.ai.agent.pojo.User;
import com.airi.ai.agent.result.ErrorCode;
import com.airi.ai.agent.result.R;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 用户控制器（商城登录/注册）
 *
 * <p>密码采用 SHA-256 加盐哈希存储（演示级，非生产级安全方案）。</p>
 */
@RestController
public class UserController {

    /** 密码加盐值（演示用固定盐） */
    private static final String PASSWORD_SALT = "airi-mall";

    private final UserMapper userMapper;

    /**
     * 构造注入用户 Mapper
     *
     * @param userMapper 用户 Mapper
     */
    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 注册新用户
     *
     * @param user 请求体（username/password/nickname）
     * @return 统一响应 R
     */
    @PostMapping("/user/register")
    public R register(@RequestBody User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            return R.error(ErrorCode.PARAMETER_ERROR);
        }
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            return R.error(108, "用户名已存在");
        }
        // 密码加盐哈希后入库，不存明文
        user.setPassword(sha256(user.getPassword() + PASSWORD_SALT));
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        userMapper.insert(user);
        return R.ok();
    }

    /**
     * 用户登录
     *
     * @param user 请求体（username/password）
     * @return 成功时 dataMap 携带 id/username/nickname/phone
     */
    @PostMapping("/user/login")
    public R login(@RequestBody User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            return R.error(ErrorCode.PARAMETER_ERROR);
        }
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (dbUser == null || !dbUser.getPassword().equals(sha256(user.getPassword() + PASSWORD_SALT))) {
            return R.error(109, "用户名或密码错误");
        }
        return R.ok()
                .data("id", dbUser.getId())
                .data("username", dbUser.getUsername())
                .data("nickname", dbUser.getNickname())
                .data("phone", dbUser.getPhone());
    }

    /**
     * SHA-256 十六进制摘要
     *
     * @param input 原始字符串
     * @return 64 位十六进制小写摘要
     */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
}
