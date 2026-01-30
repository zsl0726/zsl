package com.ruoyi.health.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 心理咨询预约
 */
public class ConsultationAppointment {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 咨询师ID */
    private Long psychologistId;

    /** 咨询师姓名 */
    private String psychologistName;

    /** 预约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date appointmentDate;

    /** 时间段 */
    private String timeSlot;

    /** 地点ID */
    private Long locationId;

    /** 地点名称 */
    private String locationName;

    /** 问题描述 */
    private String problemDescription;

    /** 紧急程度 */
    private String urgencyLevel;

    /** 状态 */
    private String status;

    /** 咨询笔记 */
    private String counselingNotes;

    /** 评分ID */
    private Long ratingId;

    /** 评分分数 */
    private BigDecimal ratingScore;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 删除标志 */
    private Integer isDeleted;

    // getters and setters 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getPsychologistId() {
        return psychologistId;
    }

    public void setPsychologistId(Long psychologistId) {
        this.psychologistId = psychologistId;
    }

    public String getPsychologistName() {
        return psychologistName;
    }

    public void setPsychologistName(String psychologistName) {
        this.psychologistName = psychologistName;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCounselingNotes() {
        return counselingNotes;
    }

    public void setCounselingNotes(String counselingNotes) {
        this.counselingNotes = counselingNotes;
    }

    public Long getRatingId() {
        return ratingId;
    }

    public void setRatingId(Long ratingId) {
        this.ratingId = ratingId;
    }

    public BigDecimal getRatingScore() {
        return ratingScore;
    }

    public void setRatingScore(BigDecimal ratingScore) {
        this.ratingScore = ratingScore;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("psychologistId", getPsychologistId())
                .append("psychologistName", getPsychologistName())
                .append("appointmentDate", getAppointmentDate())
                .append("timeSlot", getTimeSlot())
                .append("locationId", getLocationId())
                .append("locationName", getLocationName())
                .append("problemDescription", getProblemDescription())
                .append("urgencyLevel", getUrgencyLevel())
                .append("status", getStatus())
                .append("counselingNotes", getCounselingNotes())
                .append("ratingId", getRatingId())
                .append("ratingScore", getRatingScore())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("isDeleted", getIsDeleted())
                .toString();
    }
}