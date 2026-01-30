package com.ruoyi.health.domain.vo;

import lombok.Data;
import java.util.Date;

@Data
public class LoginResultVo {
    private String token;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private String nickname;
        private String phone;
        private String userType;
        private String avatar;
        private String mentalStatus;
        private Date lastLoginTime;
        private Integer assessmentCount;
        private Integer appointmentCount;
    }
}