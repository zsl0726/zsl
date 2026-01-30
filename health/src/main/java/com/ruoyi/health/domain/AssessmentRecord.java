package com.ruoyi.health.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 评估记录对象 assessment_record
 *
 * @author GGbond
 * @date 2025-12-17
 */
public class AssessmentRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 量表代码（SDS、SAS等） */
    @Excel(name = "量表代码")
    private String scaleCode;

    /** 量表名称 */
    @Excel(name = "量表名称")
    private String scaleName;

    /** 评估时间 */
    @Excel(name = "评估时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date assessmentTime;

    /** 总得分 */
    @Excel(name = "总得分")
    private Long totalScore;

    /** 标准分 */
    @Excel(name = "标准分")
    private Double standardScore;

    /** 评估结果等级 */
    @Excel(name = "评估结果等级")
    private String resultLevel;

    /** 风险等级 */
    @Excel(name = "风险等级")
    private String riskLevel;

    /** 专业建议 */
    @Excel(name = "专业建议")
    private String suggestion;

    /** 答案JSON */
    private String answersJson;


    /** 开始时间（查询条件） */
    private Date beginAssessmentTime;

    /** 结束时间（查询条件） */
    private Date endAssessmentTime;

    /** 开始总分（查询条件） */
    private Long beginTotalScore;

    /** 结束总分（查询条件） */
    private Long endTotalScore;

    // 构造函数
    public AssessmentRecord() {
        this.assessmentTime = new Date();
    }

    // ========== 数据库字段的 Getter 和 Setter ==========
    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }

    public void setUserName(String userName) { this.userName = userName; }
    public String getUserName() { return userName; }

    public void setScaleCode(String scaleCode) { this.scaleCode = scaleCode; }
    public String getScaleCode() { return scaleCode; }

    public void setScaleName(String scaleName) { this.scaleName = scaleName; }
    public String getScaleName() { return scaleName; }

    public void setAssessmentTime(Date assessmentTime) { this.assessmentTime = assessmentTime; }
    public Date getAssessmentTime() { return assessmentTime; }

    public void setTotalScore(Long totalScore) { this.totalScore = totalScore; }
    public Long getTotalScore() { return totalScore; }

    public void setStandardScore(Double standardScore) { this.standardScore = standardScore; }
    public Double getStandardScore() { return standardScore; }

    public void setResultLevel(String resultLevel) { this.resultLevel = resultLevel; }
    public String getResultLevel() { return resultLevel; }

    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskLevel() { return riskLevel; }

    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getSuggestion() { return suggestion; }

    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
    public String getAnswersJson() { return answersJson; }

    // ========== 查询条件字段的 Getter 和 Setter ==========
    public void setBeginAssessmentTime(Date beginAssessmentTime) {
        this.beginAssessmentTime = beginAssessmentTime;
    }
    public Date getBeginAssessmentTime() {
        return beginAssessmentTime;
    }

    public void setEndAssessmentTime(Date endAssessmentTime) {
        this.endAssessmentTime = endAssessmentTime;
    }
    public Date getEndAssessmentTime() {
        return endAssessmentTime;
    }

    public void setBeginTotalScore(Long beginTotalScore) {
        this.beginTotalScore = beginTotalScore;
    }
    public Long getBeginTotalScore() {
        return beginTotalScore;
    }

    public void setEndTotalScore(Long endTotalScore) {
        this.endTotalScore = endTotalScore;
    }
    public Long getEndTotalScore() {
        return endTotalScore;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("scaleCode", getScaleCode())
                .append("scaleName", getScaleName())
                .append("assessmentTime", getAssessmentTime())
                .append("totalScore", getTotalScore())
                .append("standardScore", getStandardScore())
                .append("resultLevel", getResultLevel())
                .append("riskLevel", getRiskLevel())
                .append("suggestion", getSuggestion())
                .append("answersJson", getAnswersJson())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}