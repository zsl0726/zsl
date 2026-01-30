package com.ruoyi.health.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.health.domain.entity.HealthUser;
import com.ruoyi.health.domain.vo.LoginResultVo;
import com.ruoyi.health.domain.vo.LoginVo;
import com.ruoyi.health.domain.vo.RegisterVo;
import com.ruoyi.health.service.HealthAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HealthAuthServiceImpl implements HealthAuthService {

    @Autowired
    private RedisCache redisCache;

    // 常量定义
    private static final String USER_KEY_PREFIX = "health:user:";
    private static final String PHONE_KEY_PREFIX = "health:phone:";
    private static final String LOGIN_TOKEN_KEY = "health:login:token:";
    private static final String SMS_CODE_KEY = "health:sms:code:";
    private static final String LOGIN_FAIL_KEY = "health:login:fail:";

    @Override
    public LoginResultVo login(LoginVo loginVo, HttpServletRequest request) {
        String username = loginVo.getUsername();
        String password = loginVo.getPassword();

        // 1. 验证用户名密码
        HealthUser user = getUserByUsername(username);
        if (user == null) {
            throw new ServiceException("用户名不存在");
        }

        if (!"0".equals(user.getStatus())) {
            throw new ServiceException("账号已被停用");
        }

        // 验证密码（这里简单对比，实际应该加密对比）
        if (!user.getPassword().equals(password)) {
            // 记录登录失败次数
            incrementLoginFailCount(username);
            throw new ServiceException("用户名或密码错误");
        }

        // 2. 生成token
        String token = generateToken(username);

        // 3. 更新用户最后登录时间
        user.setLastLoginTime(new Date());
        saveUser(user);

        // 4. 构建返回结果
        LoginResultVo result = new LoginResultVo();
        result.setToken(token);

        LoginResultVo.UserInfo userInfo = new LoginResultVo.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setPhone(user.getPhone());
        userInfo.setUserType(user.getUserType());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setMentalStatus(user.getMentalStatus());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setAssessmentCount(user.getAssessmentCount());
        userInfo.setAppointmentCount(user.getAppointmentCount());

        result.setUserInfo(userInfo);

        // 5. 存储token到Redis，有效期30分钟
        redisCache.setCacheObject(LOGIN_TOKEN_KEY + token, userInfo, 30, TimeUnit.MINUTES);

        log.info("用户 {} 登录成功", username);
        return result;
    }

    @Override
    @Transactional
    public boolean register(RegisterVo registerVo) {
        // 1. 验证参数
        if (!registerVo.getPassword().equals(registerVo.getConfirmPassword())) {
            throw new ServiceException("两次输入的密码不一致");
        }

        // 2. 检查用户名是否已存在
        if (getUserByUsername(registerVo.getUsername()) != null) {
            throw new ServiceException("用户名已存在");
        }

        // 3. 检查手机号是否已存在
        if (getUserByPhone(registerVo.getPhone()) != null) {
            throw new ServiceException("手机号已存在");
        }

        // 4. 创建用户
        HealthUser user = new HealthUser();
        user.setId(generateUserId());
        user.setUsername(registerVo.getUsername());
        user.setPassword(registerVo.getPassword()); // 实际应该加密存储
        user.setPhone(registerVo.getPhone());
        user.setUserType(registerVo.getUserType());
        user.setNickname(StringUtils.isNotEmpty(registerVo.getNickname()) ?
                registerVo.getNickname() : registerVo.getUsername());
        user.setStatus("0");
        user.setCreateTime(new Date());
        user.setLastLoginTime(new Date());
        user.setAvatar("/profile/avatar/default.jpg");
        user.setAssessmentCount(0);
        user.setAppointmentCount(0);
        user.setMentalStatus("normal");

        // 5. 保存用户
        saveUser(user);

        log.info("用户注册成功: {}", registerVo.getUsername());
        return true;
    }

    @Override
    public boolean sendSmsCode(String phone) {
        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new ServiceException("手机号格式不正确");
        }

        // 生成6位验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

        // 存储到Redis，有效期10分钟
        String key = SMS_CODE_KEY + phone;
        redisCache.setCacheObject(key, code, 10, TimeUnit.MINUTES);

        // 这里应该调用短信服务，这里模拟发送
        log.info("发送短信验证码到 {}: {}", phone, code);

        return true;
    }

    @Override
    public LoginResultVo smsLogin(String phone, String code, HttpServletRequest request) {
        // 1. 验证验证码
        String key = SMS_CODE_KEY + phone;
        String cacheCode = redisCache.getCacheObject(key);
        if (StringUtils.isEmpty(cacheCode) || !cacheCode.equals(code)) {
            throw new ServiceException("验证码错误或已过期");
        }

        // 2. 查找用户
        HealthUser user = getUserByPhone(phone);
        if (user == null) {
            throw new ServiceException("手机号未注册");
        }

        // 3. 生成token
        String token = generateToken(user.getUsername());

        // 4. 更新最后登录时间
        user.setLastLoginTime(new Date());
        saveUser(user);

        // 5. 构建返回结果
        LoginResultVo result = new LoginResultVo();
        result.setToken(token);

        LoginResultVo.UserInfo userInfo = new LoginResultVo.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setPhone(user.getPhone());
        userInfo.setUserType(user.getUserType());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setMentalStatus(user.getMentalStatus());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setAssessmentCount(user.getAssessmentCount());
        userInfo.setAppointmentCount(user.getAppointmentCount());

        result.setUserInfo(userInfo);

        // 6. 存储token到Redis
        redisCache.setCacheObject(LOGIN_TOKEN_KEY + token, userInfo, 30, TimeUnit.MINUTES);

        // 7. 删除已使用的验证码
        redisCache.deleteObject(key);

        log.info("用户 {} 短信登录成功", phone);
        return result;
    }

    @Override
    public boolean resetPassword(String phone, String code, String newPassword) {
        // 1. 验证验证码
        String key = SMS_CODE_KEY + phone;
        String cacheCode = redisCache.getCacheObject(key);
        if (StringUtils.isEmpty(cacheCode) || !cacheCode.equals(code)) {
            throw new ServiceException("验证码错误或已过期");
        }

        // 2. 查找用户
        HealthUser user = getUserByPhone(phone);
        if (user == null) {
            throw new ServiceException("手机号未注册");
        }

        // 3. 更新密码
        user.setPassword(newPassword);
        saveUser(user);

        // 4. 删除验证码
        redisCache.deleteObject(key);

        log.info("用户 {} 重置密码成功", phone);
        return true;
    }

    @Override
    public void logout(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String key = LOGIN_TOKEN_KEY + token;
            redisCache.deleteObject(key);
            log.info("用户登出，token: {}", token);
        }
    }

    @Override
    public Object getUserInfo(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String key = LOGIN_TOKEN_KEY + token;
        return redisCache.getCacheObject(key);
    }

    // ========== 私有方法 ==========

    private HealthUser getUserByUsername(String username) {
        String key = USER_KEY_PREFIX + username;
        return redisCache.getCacheObject(key);
    }

    private HealthUser getUserByPhone(String phone) {
        String key = PHONE_KEY_PREFIX + phone;
        String username = redisCache.getCacheObject(key);
        if (username != null) {
            return getUserByUsername(username);
        }
        return null;
    }

    private void saveUser(HealthUser user) {
        // 保存用户信息
        String userKey = USER_KEY_PREFIX + user.getUsername();
        redisCache.setCacheObject(userKey, user, 7, TimeUnit.DAYS);

        // 保存手机号映射
        String phoneKey = PHONE_KEY_PREFIX + user.getPhone();
        redisCache.setCacheObject(phoneKey, user.getUsername(), 7, TimeUnit.DAYS);
    }

    private String generateToken(String username) {
        return "health_" + UUID.randomUUID().toString().replace("-", "");
    }

    private Long generateUserId() {
        return System.currentTimeMillis();
    }

    private void incrementLoginFailCount(String username) {
        String key = LOGIN_FAIL_KEY + username;
        Integer count = redisCache.getCacheObject(key);
        if (count == null) {
            count = 0;
        }
        count++;
        redisCache.setCacheObject(key, count, 30, TimeUnit.MINUTES);

        if (count >= 5) {
            log.warn("用户 {} 登录失败次数过多，建议锁定", username);
        }
    }
}