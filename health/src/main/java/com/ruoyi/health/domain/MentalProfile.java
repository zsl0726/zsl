package com.ruoyi.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 心理档案对象 mental_profile
 */
public class MentalProfile {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 真实姓名 */
    private String realName;

    /** 性别（0男 1女 2未知） */
    private String gender;

    /** 年龄 */
    private Integer age;

    /** 职业/身份 */
    private String occupation;

    /** 情绪状态评分（1-5） */
    private Integer emotionalState;

    /** 睡眠质量评分（1-5） */
    private Integer sleepQuality;

    /** 压力水平（0-100） */
    private Integer stressLevel;

    /** 主要困扰（多个用逗号分隔） */
    private String mainConcerns;

    /** 其他困扰描述 */
    private String otherConcerns;

    /** 期望获得的帮助 */
    private String expectations;

    /** 心理咨询经历 */
    private String therapyExperience;

    /** 心理服务偏好 */
    private String preference;

    /** 补充说明 */
    private String additionalNotes;

    /** 状态（0正常 1停用） */
    private String status;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 备注 */
    private String remark;

    /** 是否对咨询师可见 0-否 1-是 */
    private Integer isVisibleToConsultant;

    /** 可见性更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date visibilityUpdatedTime;

    // 非数据库字段
    private String genderName;
    private String[] mainConcernsArray;
    private String emotionalStateLabel;
    private String sleepQualityLabel;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getGender() { return gender; }
    public void setGender(String gender) {
        this.gender = gender;
        if ("0".equals(gender)) {
            this.genderName = "男";
        } else if ("1".equals(gender)) {
            this.genderName = "女";
        } else if ("2".equals(gender)) {
            this.genderName = "不愿透露";
        } else {
            this.genderName = "未填写";
        }
    }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public Integer getEmotionalState() { return emotionalState; }
    public void setEmotionalState(Integer emotionalState) {
        this.emotionalState = emotionalState;
        if (emotionalState != null) {
            String[] labels = {"非常差", "较差", "一般", "良好", "非常好"};
            if (emotionalState >= 1 && emotionalState <= 5) {
                this.emotionalStateLabel = labels[emotionalState - 1];
            } else {
                this.emotionalStateLabel = "未填写";
            }
        } else {
            this.emotionalStateLabel = "未填写";
        }
    }

    public Integer getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(Integer sleepQuality) {
        this.sleepQuality = sleepQuality;
        if (sleepQuality != null) {
            String[] labels = {"非常差", "较差", "一般", "良好", "非常好"};
            if (sleepQuality >= 1 && sleepQuality <= 5) {
                this.sleepQualityLabel = labels[sleepQuality - 1];
            } else {
                this.sleepQualityLabel = "未填写";
            }
        } else {
            this.sleepQualityLabel = "未填写";
        }
    }

    public Integer getStressLevel() { return stressLevel; }
    public void setStressLevel(Integer stressLevel) { this.stressLevel = stressLevel; }

    public String getMainConcerns() { return mainConcerns; }
    public void setMainConcerns(String mainConcerns) {
        this.mainConcerns = mainConcerns;
        if (mainConcerns != null && !mainConcerns.trim().isEmpty()) {
            this.mainConcernsArray = mainConcerns.split(",");
        } else {
            this.mainConcernsArray = new String[0];
        }
    }

    public String getOtherConcerns() { return otherConcerns; }
    public void setOtherConcerns(String otherConcerns) { this.otherConcerns = otherConcerns; }

    public String getExpectations() { return expectations; }
    public void setExpectations(String expectations) { this.expectations = expectations; }

    public String getTherapyExperience() { return therapyExperience; }
    public void setTherapyExperience(String therapyExperience) { this.therapyExperience = therapyExperience; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getIsVisibleToConsultant() { return isVisibleToConsultant; }
    public void setIsVisibleToConsultant(Integer isVisibleToConsultant) {
        this.isVisibleToConsultant = isVisibleToConsultant;
        if (isVisibleToConsultant != null) {
            this.visibilityUpdatedTime = new Date();
        }
    }

    public Date getVisibilityUpdatedTime() { return visibilityUpdatedTime; }
    public void setVisibilityUpdatedTime(Date visibilityUpdatedTime) { this.visibilityUpdatedTime = visibilityUpdatedTime; }

    public String getGenderName() { return genderName; }
    public void setGenderName(String genderName) { this.genderName = genderName; }

    public String[] getMainConcernsArray() { return mainConcernsArray; }
    public void setMainConcernsArray(String[] mainConcernsArray) {
        this.mainConcernsArray = mainConcernsArray;
        if (mainConcernsArray != null && mainConcernsArray.length > 0) {
            this.mainConcerns = String.join(",", mainConcernsArray);
        } else {
            this.mainConcerns = "";
        }
    }

    public String getEmotionalStateLabel() { return emotionalStateLabel; }
    public void setEmotionalStateLabel(String emotionalStateLabel) { this.emotionalStateLabel = emotionalStateLabel; }

    public String getSleepQualityLabel() { return sleepQualityLabel; }
    public void setSleepQualityLabel(String sleepQualityLabel) { this.sleepQualityLabel = sleepQualityLabel; }

    @Override
    public String toString() {
        return "MentalProfile{" +
                "id=" + id +
                ", userId=" + userId +
                ", realName='" + realName + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", occupation='" + occupation + '\'' +
                ", emotionalState=" + emotionalState +
                ", sleepQuality=" + sleepQuality +
                ", stressLevel=" + stressLevel +
                ", mainConcerns='" + mainConcerns + '\'' +
                ", otherConcerns='" + otherConcerns + '\'' +
                ", expectations='" + expectations + '\'' +
                ", therapyExperience='" + therapyExperience + '\'' +
                ", preference='" + preference + '\'' +
                ", additionalNotes='" + additionalNotes + '\'' +
                ", status='" + status + '\'' +
                ", isVisibleToConsultant=" + isVisibleToConsultant +
                ", visibilityUpdatedTime=" + visibilityUpdatedTime +
                '}';
    }
}