// src/main/java/com/ruoyi/health/service/IAiConversationService.java
package com.ruoyi.health.service;

import com.ruoyi.health.domain.AiConversation;

import java.util.List;
import java.util.Map;

public interface IAiConversationService {

    // 基础CRUD操作
    AiConversation selectAiConversationById(Long id);

    List<AiConversation> selectAiConversationList(AiConversation aiConversation);

    int insertAiConversation(AiConversation aiConversation);

    int updateAiConversation(AiConversation aiConversation);

    int deleteAiConversationById(Long id);

    int deleteAiConversationByIds(Long[] ids);

    // 业务方法（与Controller对应）
    List<Map<String, Object>> getUserSessionList(Long userId);

    Map<String, Object> startNewConversation(Long userId, String userName);

    Map<String, Object> sendMessage(String sessionId, String message, Long userId, String userName);

    List<Map<String, Object>> getUserConversationHistory(String sessionId, Long userId);

    boolean deleteUserSession(String sessionId, Long userId);

    Map<String, Object> getConversationStats(Long userId);
}