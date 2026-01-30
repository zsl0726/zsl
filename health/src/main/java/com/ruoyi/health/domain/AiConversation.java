// src/main/java/com/ruoyi/health/domain/AiConversation.java
package com.ruoyi.health.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI对话记录对象 ai_conversation
 */
public class AiConversation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名称 */
    @Excel(name = "用户名称")
    private String userName;

    /** 会话ID */
    private String sessionId;

    /** 消息类型（0用户提问 1AI回复） */
    @Excel(name = "消息类型", readConverterExp = "0=用户提问,1=AI回复")
    private String messageType;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String content;

    /** Token消耗 - 修改为Integer类型 */
    private Integer tokenUsage;

    /** AI模型名称 */
    @Excel(name = "AI模型名称")
    private String modelName;

    /** 对话时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "对话时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date conversationTime;

    /** 删除标志（0代表存在 1代表删除） */
    @Excel(name = "删除标志", readConverterExp = "0=代表存在,1=代表删除")
    private String isDeleted;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    // 修改getter和setter为Integer类型
    public Integer getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(Integer tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setConversationTime(Date conversationTime) {
        this.conversationTime = conversationTime;
    }

    public Date getConversationTime() {
        return conversationTime;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("sessionId", getSessionId())
                .append("messageType", getMessageType())
                .append("content", getContent())
                .append("tokenUsage", getTokenUsage())
                .append("modelName", getModelName())
                .append("conversationTime", getConversationTime())
                .append("isDeleted", getIsDeleted())
                .toString();
    }
}