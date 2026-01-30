package com.ruoyi.health.domain.entity;

import lombok.Data;
import java.util.Date;

@Data
public class HealthUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String userType; // student/psychologist/admin
    private String status;   // 0正常 1停用
    private Date createTime;
    private Date lastLoginTime;
    private String avatar;
    private Integer assessmentCount = 0;
    private Integer appointmentCount = 0;
    private String mentalStatus = "normal";
    private String emergencyContact;
    private String emergencyPhone;
}