package com.ruoyi.health.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 心理评估量对象 assessment_scale
 *
 * @author GGbond
 * @date 2025-12-17
 */
public class AssessmentScale extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 量表编码 */
    private String scaleCode;

    /** 量表名称 */
    private String scaleName;

    /** 量表描述 */
    private String description;

    /** 题目数量 */
    private Long questionCount;

    /** 建议时间(分钟) */
    private Long timeRequired;

    /** 状态（0正常 1停用） */
    private String status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setScaleCode(String scaleCode)
    {
        this.scaleCode = scaleCode;
    }

    public String getScaleCode()
    {
        return scaleCode;
    }

    public void setScaleName(String scaleName)
    {
        this.scaleName = scaleName;
    }

    public String getScaleName()
    {
        return scaleName;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setQuestionCount(Long questionCount)
    {
        this.questionCount = questionCount;
    }

    public Long getQuestionCount()
    {
        return questionCount;
    }

    public void setTimeRequired(Long timeRequired)
    {
        this.timeRequired = timeRequired;
    }

    public Long getTimeRequired()
    {
        return timeRequired;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("scaleCode", getScaleCode())
            .append("scaleName", getScaleName())
            .append("description", getDescription())
            .append("questionCount", getQuestionCount())
            .append("timeRequired", getTimeRequired())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
