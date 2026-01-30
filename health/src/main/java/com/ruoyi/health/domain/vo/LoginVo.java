package com.ruoyi.health.domain.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginVo {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String code;     // 验证码
    private String uuid;     // 验证码UUID
}