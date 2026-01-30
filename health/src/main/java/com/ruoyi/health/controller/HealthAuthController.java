package com.ruoyi.health.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.health.domain.vo.LoginResultVo;
import com.ruoyi.health.domain.vo.LoginVo;
import com.ruoyi.health.domain.vo.RegisterVo;
import com.ruoyi.health.service.HealthAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/health/auth")
public class HealthAuthController extends BaseController {

    @Autowired
    private HealthAuthService healthAuthService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public AjaxResult login(@Validated @RequestBody LoginVo loginVo, HttpServletRequest request) {
        LoginResultVo result = healthAuthService.login(loginVo, request);
        return AjaxResult.success("登录成功", result);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public AjaxResult register(@Validated @RequestBody RegisterVo registerVo) {
        boolean result = healthAuthService.register(registerVo);
        return result ? AjaxResult.success("注册成功") : AjaxResult.error("注册失败");
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sendSmsCode")
    public AjaxResult sendSmsCode(@RequestParam String phone) {
        boolean result = healthAuthService.sendSmsCode(phone);
        return result ? AjaxResult.success("验证码发送成功") : AjaxResult.error("验证码发送失败");
    }

    /**
     * 短信登录
     */
    @PostMapping("/smsLogin")
    public AjaxResult smsLogin(@RequestParam String phone,
                               @RequestParam String code,
                               HttpServletRequest request) {
        LoginResultVo result = healthAuthService.smsLogin(phone, code, request);
        return AjaxResult.success("登录成功", result);
    }

    /**
     * 重置密码
     */
    @PostMapping("/resetPassword")
    public AjaxResult resetPassword(@RequestParam String phone,
                                    @RequestParam String code,
                                    @RequestParam String newPassword) {
        boolean result = healthAuthService.resetPassword(phone, code, newPassword);
        return result ? AjaxResult.success("密码重置成功") : AjaxResult.error("密码重置失败");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public AjaxResult logout(@RequestParam String token) {
        healthAuthService.logout(token);
        return AjaxResult.success("登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo(@RequestParam String token) {
        // 从Redis获取用户信息
        Object userInfo = healthAuthService.getUserInfo(token);
        return userInfo != null ? AjaxResult.success(userInfo) : AjaxResult.error("用户信息不存在或已过期");
    }
}