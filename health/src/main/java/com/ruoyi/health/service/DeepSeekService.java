package com.ruoyi.health.service;

import java.util.List;
import java.util.Map;

public interface DeepSeekService {

    /**
     * 发送消息到DeepSeek API
     * @param userMessage 用户消息
     * @param sessionId 会话ID
     * @param history 历史消息列表
     * @return 包含响应内容、token使用等信息
     */
    Map<String, Object> sendMessage(String userMessage, String sessionId, List<Map<String, Object>> history);

    /**
     * 分析用户消息的情绪
     * @param message 用户消息
     * @return 情绪分类（积极、焦虑、低落等）
     */
    String analyzeEmotion(String message);

    /**
     * 清除指定会话的内存历史
     * @param sessionId 会话ID
     */
    void clearSessionHistory(String sessionId);
}