// RegisterBody.java
package com.ruoyi.common.core.domain.model;

/**
 * 用户注册对象
 */
public class RegisterBody
{
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 手机号 - 新增字段
     */
    private String phonenumber;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;

    /**
     * 用户类型 - 新增：区分普通用户和咨询师
     */
    private String userType;

    // Getter 和 Setter 方法
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    // 新增：手机号的 Getter 和 Setter
    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    // 新增：用户类型 Getter 和 Setter
    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }
}