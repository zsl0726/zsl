package com.ruoyi.health.service;

import com.ruoyi.health.domain.vo.LoginResultVo;
import com.ruoyi.health.domain.vo.LoginVo;
import com.ruoyi.health.domain.vo.RegisterVo;
import javax.servlet.http.HttpServletRequest;

public interface HealthAuthService {

    /**
     * 用户登录
     */
    LoginResultVo login(LoginVo loginVo, HttpServletRequest request);

    /**
     * 用户注册
     */
    boolean register(RegisterVo registerVo);

    /**
     * 发送短信验证码
     */
    boolean sendSmsCode(String phone);

    /**
     * 短信登录
     */
    LoginResultVo smsLogin(String phone, String code, HttpServletRequest request);

    /**
     * 重置密码
     */
    boolean resetPassword(String phone, String code, String newPassword);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 根据token获取用户信息
     */
    Object getUserInfo(String token);
}