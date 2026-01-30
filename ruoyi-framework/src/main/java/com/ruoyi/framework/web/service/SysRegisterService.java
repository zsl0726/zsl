// SysRegisterService.java
package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 注册校验方法
 *
 * @author ruoyi
 */
@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody)
    {
        System.out.println("=== 开始普通用户注册 ===");
        System.out.println("注册数据: " + registerBody.toString());
        return commonRegister(registerBody, 2);
    }

    // 新增register3方法（绑定角色3）
    public String register3(RegisterBody registerBody)
    {
        System.out.println("=== 开始咨询师注册 ===");
        System.out.println("注册数据: " + registerBody.toString());
        return commonRegister(registerBody, 3);
    }

    // 抽取公共注册逻辑（私有方法，内部调用）
    private String commonRegister(RegisterBody registerBody, Integer roleType)
    {
        String msg = "";
        String username = registerBody.getUsername();
        String password = registerBody.getPassword();
        String phonenumber = registerBody.getPhonenumber(); // 获取手机号

        System.out.println("=== 处理注册 ===");
        System.out.println("用户名: " + username);
        System.out.println("手机号: " + phonenumber);
        System.out.println("角色类型: " + roleType);

        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        // 所有校验逻辑
        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        // 验证手机号格式（如果提供了手机号）
        else if (StringUtils.isNotEmpty(phonenumber) && !isValidPhoneNumber(phonenumber))
        {
            msg = "手机号格式不正确";
        }
        else if (!userService.checkUserNameUnique(sysUser))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            // 设置用户基本信息
            sysUser.setNickName(username);
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));

            // 设置手机号（重要！）
            if (StringUtils.isNotEmpty(phonenumber)) {
                // 检查手机号是否已存在
                SysUser checkUser = new SysUser();
                checkUser.setPhonenumber(phonenumber);
                if (userService.checkPhoneUnique(checkUser))
                {
                    sysUser.setPhonenumber(phonenumber);
                    System.out.println("=== 设置用户手机号: " + phonenumber + " ===");
                }
                else
                {
                    return "保存用户'" + username + "'失败，手机号已存在";
                }
            }

            // 根据传入的roleType绑定对应角色
            boolean regFlag = userService.registerUser(sysUser, roleType);
            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
            }
            else
            {
                System.out.println("=== 用户注册成功 ===");
                System.out.println("用户名: " + username);
                System.out.println("手机号: " + phonenumber);
                System.out.println("角色类型: " + roleType);

                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER,
                        MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            return true; // 手机号可选，为空返回true
        }
        // 手机号验证：11位数字，1开头
        String regex = "^1[3-9]\\d{9}$";
        return phoneNumber.matches(regex);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
}