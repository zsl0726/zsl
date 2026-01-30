package com.ruoyi.health.util;

import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

/**
 * 安全工具类
 */
public class HealthSecurityUtils {

    /**
     * 密码加密（使用MD5）
     */
    public static String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证密码
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return encryptPassword(rawPassword).equals(encodedPassword);
    }

    /**
     * 生成随机验证码
     */
    public static String generateSmsCode() {
        return String.valueOf((int)((Math.random() * 9 + 1) * 100000));
    }
}