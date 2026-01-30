package com.ruoyi.health.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AppointmentRating extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long appointmentId;
    private Long userId;
    private Long psychologistId;
    private BigDecimal ratingScore;
    private String ratingComment;
    private String delFlag;

    // 扩展字段（用于显示）
    private String userName;
    private String psychologistName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPsychologistId() { return psychologistId; }
    public void setPsychologistId(Long psychologistId) { this.psychologistId = psychologistId; }

    public BigDecimal getRatingScore() { return ratingScore; }
    public void setRatingScore(BigDecimal ratingScore) { this.ratingScore = ratingScore; }

    public String getRatingComment() { return ratingComment; }
    public void setRatingComment(String ratingComment) { this.ratingComment = ratingComment; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPsychologistName() { return psychologistName; }
    public void setPsychologistName(String psychologistName) { this.psychologistName = psychologistName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("appointmentId", getAppointmentId())
                .append("userId", getUserId())
                .append("psychologistId", getPsychologistId())
                .append("ratingScore", getRatingScore())
                .append("ratingComment", getRatingComment())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}