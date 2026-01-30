package com.ruoyi.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 心理咨询师排班对象 psychologist_schedule
 */
public class PsychologistSchedule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 咨询师ID */
    @Excel(name = "咨询师ID")
    private Long psychologistId;

    /** 咨询师姓名 */
    @Excel(name = "咨询师姓名")
    private String psychologistName;

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "排班日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date scheduleDate;

    /** 可用时间段（逗号分隔的字符串，如：9:00-10:00,10:00-11:00） */
    @Excel(name = "可用时间段")
    private String timeSlots;

    /** 最大预约数 */
    @Excel(name = "最大预约数")
    private Integer maxAppointments;

    /** 是否可用（1可用，0不可用） */
    @Excel(name = "是否可用", readConverterExp = "1=可用,0=不可用")
    private String isAvailable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(String timeSlots) {
        this.timeSlots = timeSlots;
    }

    public Integer getMaxAppointments() {
        return maxAppointments;
    }

    public void setMaxAppointments(Integer maxAppointments) {
        this.maxAppointments = maxAppointments;
    }

    public String getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(String isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("psychologistId", getPsychologistId())
                .append("psychologistName", getPsychologistName())
                .append("scheduleDate", getScheduleDate())
                .append("timeSlots", getTimeSlots())
                .append("maxAppointments", getMaxAppointments())
                .append("isAvailable", getIsAvailable())
                .append("remarks", getRemarks())
                .append("isDeleted", getIsDeleted())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}