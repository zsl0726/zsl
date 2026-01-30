package com.ruoyi.health.service.impl;

import com.ruoyi.health.config.DeepSeekConfig;
import com.ruoyi.health.domain.AiConversation;
import com.ruoyi.health.mapper.AiConversationMapper;
import com.ruoyi.health.service.DeepSeekService;
import com.ruoyi.health.service.IAiConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiConversationServiceImpl implements IAiConversationService {

    private static final Logger log = LoggerFactory.getLogger(AiConversationServiceImpl.class);

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    // 用户会话计数器
    private final Map<Long, AtomicInteger> userSessionCounter = new ConcurrentHashMap<>();

    // 基础CRUD方法
    @Override
    public AiConversation selectAiConversationById(Long id) {
        return aiConversationMapper.selectAiConversationById(id);
    }

    @Override
    public List<AiConversation> selectAiConversationList(AiConversation aiConversation) {
        if (aiConversation.getIsDeleted() == null) {
            aiConversation.setIsDeleted("0");
        }
        return aiConversationMapper.selectAiConversationList(aiConversation);
    }

    @Override
    public int insertAiConversation(AiConversation aiConversation) {
        if (aiConversation.getConversationTime() == null) {
            aiConversation.setConversationTime(new Date());
        }
        if (aiConversation.getIsDeleted() == null) {
            aiConversation.setIsDeleted("0");
        }
        if (aiConversation.getModelName() == null) {
            aiConversation.setModelName("deepseek-chat");
        }
        return aiConversationMapper.insertAiConversation(aiConversation);
    }

    @Override
    public int updateAiConversation(AiConversation aiConversation) {
        return aiConversationMapper.updateAiConversation(aiConversation);
    }

    @Override
    public int deleteAiConversationById(Long id) {
        return aiConversationMapper.deleteAiConversationById(id);
    }

    @Override
    public int deleteAiConversationByIds(Long[] ids) {
        return aiConversationMapper.deleteAiConversationByIds(ids);
    }

    // 业务方法实现
    @Override
    public List<Map<String, Object>> getUserSessionList(Long userId) {
        List<Map<String, Object>> sessions = new ArrayList<>();

        try {
            // 获取用户的所有会话
            List<String> sessionIds = aiConversationMapper.selectUserSessionIds(userId);

            for (String sessionId : sessionIds) {
                // 获取每个会话的最新消息
                List<AiConversation> messages = aiConversationMapper.selectBySessionId(sessionId);
                if (!messages.isEmpty()) {
                    // 排序获取最新消息
                    messages.sort((a, b) -> b.getConversationTime().compareTo(a.getConversationTime()));
                    AiConversation lastMsg = messages.get(0);

                    // 查找第一个用户消息作为标题
                    String title = "AI对话";
                    for (AiConversation msg : messages) {
                        if ("0".equals(msg.getMessageType())) {
                            String content = msg.getContent();
                            if (content.length() > 20) {
                                title = content.substring(0, 20) + "...";
                            } else {
                                title = content;
                            }
                            break;
                        }
                    }

                    Map<String, Object> session = new HashMap<>();
                    session.put("sessionId", sessionId);
                    session.put("title", title);
                    session.put("lastTime", lastMsg.getConversationTime());
                    session.put("messageCount", messages.size());

                    sessions.add(session);
                }
            }

            // 如果没有会话，创建一个默认会话
            if (sessions.isEmpty()) {
                Map<String, Object> defaultSession = createDefaultSession(userId);
                sessions.add(defaultSession);
            }

        } catch (Exception e) {
            log.error("获取用户会话列表失败：", e);
            // 即使出错也返回一个默认会话
            sessions.add(createDefaultSession(userId));
        }

        return sessions;
    }

    @Override
    @Transactional
    public Map<String, Object> startNewConversation(Long userId, String userName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 生成唯一会话ID
            int sessionNum = userSessionCounter.computeIfAbsent(userId, k -> new AtomicInteger(0)).incrementAndGet();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String random = UUID.randomUUID().toString().substring(0, 8);
            String sessionId = String.format("session-%d-%s-%s", sessionNum, timestamp, random);

            // 添加欢迎消息
            String welcomeMessage = "您好！我是AI心理助手，很高兴为您服务。我可以为您提供情绪疏导、压力管理、心理健康知识等方面的支持。有什么我可以帮助您的吗？";

            AiConversation welcomeMsg = new AiConversation();
            welcomeMsg.setSessionId(sessionId);
            welcomeMsg.setUserId(userId);
            welcomeMsg.setUserName("AI助手");
            welcomeMsg.setMessageType("1");
            welcomeMsg.setContent(welcomeMessage);
            welcomeMsg.setTokenUsage(0);
            welcomeMsg.setModelName("deepseek-chat");
            welcomeMsg.setConversationTime(new Date());
            welcomeMsg.setIsDeleted("0");

            aiConversationMapper.insertAiConversation(welcomeMsg);

            result.put("sessionId", sessionId);
            result.put("title", "新对话");
            result.put("createTime", new Date());
            result.put("welcomeMessage", welcomeMessage);

            log.info("创建新会话成功：{}", sessionId);

        } catch (Exception e) {
            log.error("创建新会话失败：", e);
            result.put("error", "创建会话失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> sendMessage(String sessionId, String message, Long userId, String userName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证API密钥
            if (!isApiKeyValid()) {
                String errorMsg = "DeepSeek API密钥未配置或无效，请检查配置";
                log.error(errorMsg);
                result.put("success", false);
                result.put("error", errorMsg);
                result.put("content", "系统配置错误，请联系管理员配置DeepSeek API密钥。");
                return result;
            }

            // 2. 保存用户消息
            AiConversation userMsg = new AiConversation();
            userMsg.setSessionId(sessionId);
            userMsg.setUserId(userId);
            userMsg.setUserName(userName);
            userMsg.setMessageType("0");
            userMsg.setContent(message);
            userMsg.setConversationTime(new Date());
            userMsg.setTokenUsage(0);
            userMsg.setModelName(deepSeekConfig.getModel());
            userMsg.setIsDeleted("0");

            int userResult = aiConversationMapper.insertAiConversation(userMsg);
            if (userResult <= 0) {
                throw new RuntimeException("保存用户消息失败");
            }

            log.info("用户消息保存成功，会话ID：{}，用户：{}", sessionId, userName);

            // 3. 获取会话历史（用于上下文）
            List<AiConversation> conversations = aiConversationMapper.selectBySessionId(sessionId);
            List<Map<String, Object>> historyList = buildHistoryForAI(conversations);

            log.info("构建历史消息完成，历史记录数：{}", historyList.size());

            // 4. 调用DeepSeek API获取回复
            Map<String, Object> aiResponse = deepSeekService.sendMessage(message, sessionId, historyList);

            // 5. 检查API调用结果
            if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.get("success"))) {
                String error = aiResponse != null ? (String) aiResponse.get("error") : "API调用失败";
                log.error("DeepSeek API调用失败：{}", error);

                result.put("success", false);
                result.put("error", error);
                result.put("content", "抱歉，AI服务暂时不可用，请稍后重试。");
                return result;
            }

            // 6. 获取AI回复内容
            String aiContent = (String) aiResponse.get("content");
            Integer totalTokens = (Integer) aiResponse.getOrDefault("totalTokens", 0);
            Integer promptTokens = (Integer) aiResponse.getOrDefault("promptTokens", 0);
            Integer completionTokens = (Integer) aiResponse.getOrDefault("completionTokens", 0);

            log.info("DeepSeek API调用成功，Prompt Tokens：{}，Completion Tokens：{}，Total Tokens：{}",
                    promptTokens, completionTokens, totalTokens);

            // 7. 保存AI回复到数据库
            AiConversation aiMsg = new AiConversation();
            aiMsg.setSessionId(sessionId);
            aiMsg.setUserId(userId);
            aiMsg.setUserName("AI助手");
            aiMsg.setMessageType("1");
            aiMsg.setContent(aiContent);
            aiMsg.setTokenUsage(totalTokens);
            aiMsg.setModelName((String) aiResponse.getOrDefault("model", deepSeekConfig.getModel()));
            aiMsg.setConversationTime(new Date());
            aiMsg.setIsDeleted("0");

            int aiResult = aiConversationMapper.insertAiConversation(aiMsg);
            if (aiResult <= 0) {
                log.warn("保存AI回复到数据库失败，但用户消息已保存");
            }

            // 8. 构建返回结果
            result.put("success", true);
            result.put("content", aiContent);
            result.put("tokenUsage", totalTokens);
            result.put("promptTokens", promptTokens);
            result.put("completionTokens", completionTokens);
            result.put("sessionId", sessionId);
            result.put("messageId", aiMsg.getId());
            result.put("model", aiMsg.getModelName());
            result.put("apiSuccess", true);
            result.put("timestamp", new Date());

            log.info("消息处理完成，会话ID：{}，Token使用：{}", sessionId, totalTokens);

        } catch (Exception e) {
            log.error("发送消息失败：", e);

            result.put("success", false);
            result.put("error", "消息处理失败：" + e.getMessage());
            result.put("content", "抱歉，处理您的请求时出现错误，请稍后重试。");
        }

        return result;
    }

    /**
     * 构建AI所需的历史消息格式
     */
    private List<Map<String, Object>> buildHistoryForAI(List<AiConversation> conversations) {
        List<Map<String, Object>> historyList = new ArrayList<>();

        if (conversations == null || conversations.isEmpty()) {
            return historyList;
        }

        // 按时间排序
        conversations.sort((a, b) -> a.getConversationTime().compareTo(b.getConversationTime()));

        // 只保留最近的10条消息（避免token超限）
        int startIndex = Math.max(0, conversations.size() - 10);
        for (int i = startIndex; i < conversations.size(); i++) {
            AiConversation conv = conversations.get(i);

            // 只包含未被删除的消息
            if ("0".equals(conv.getIsDeleted())) {
                Map<String, Object> historyMsg = new HashMap<>();

                // 根据message_type设置role
                String role = "0".equals(conv.getMessageType()) ? "user" : "assistant";
                historyMsg.put("role", role);
                historyMsg.put("content", conv.getContent());

                historyList.add(historyMsg);
            }
        }

        return historyList;
    }

    /**
     * 检查API密钥是否有效
     */
    private boolean isApiKeyValid() {
        if (deepSeekConfig == null || deepSeekConfig.getKey() == null) {
            return false;
        }

        String key = deepSeekConfig.getKey().trim();

        // 空密钥无效
        if (key.isEmpty()) {
            return false;
        }

        // 检查是否为测试密钥
        if (key.startsWith("sk-test") || key.contains("test-key")) {
            log.error("检测到测试密钥，无法使用DeepSeek API");
            return false;
        }

        // 检查密钥长度和格式
        if (key.length() < 30 || !key.startsWith("sk-")) {
            log.error("API密钥格式不正确");
            return false;
        }

        return true;
    }

    @Override
    public List<Map<String, Object>> getUserConversationHistory(String sessionId, Long userId) {
        List<Map<String, Object>> messages = new ArrayList<>();

        try {
            List<AiConversation> conversations = aiConversationMapper.selectBySessionId(sessionId);

            // 过滤出当前用户的会话（安全检查）
            List<AiConversation> userConversations = new ArrayList<>();
            for (AiConversation conversation : conversations) {
                if (conversation.getUserId().equals(userId) && "0".equals(conversation.getIsDeleted())) {
                    userConversations.add(conversation);
                }
            }

            // 按时间排序
            userConversations.sort((a, b) -> a.getConversationTime().compareTo(b.getConversationTime()));

            // 转换为前端需要的格式
            for (AiConversation conversation : userConversations) {
                Map<String, Object> message = new HashMap<>();
                message.put("type", "0".equals(conversation.getMessageType()) ? "user" : "ai");
                message.put("content", conversation.getContent());
                message.put("time", conversation.getConversationTime());
                if ("1".equals(conversation.getMessageType())) {
                    message.put("tokenUsage", conversation.getTokenUsage());
                    message.put("model", conversation.getModelName());
                }
                messages.add(message);
            }

        } catch (Exception e) {
            log.error("获取会话历史失败：", e);
        }

        return messages;
    }

    @Override
    @Transactional
    public boolean deleteUserSession(String sessionId, Long userId) {
        try {
            log.info("开始删除会话，会话ID：{}，用户ID：{}", sessionId, userId);

            // 1. 首先检查会话是否存在且属于该用户
            List<AiConversation> conversations = aiConversationMapper.selectBySessionId(sessionId);

            if (conversations.isEmpty()) {
                log.warn("会话不存在，会话ID：{}", sessionId);
                return true; // 会话不存在，视为删除成功
            }

            // 2. 验证会话是否属于当前用户
            boolean belongsToUser = conversations.stream()
                    .anyMatch(conv -> conv.getUserId().equals(userId));

            if (!belongsToUser) {
                log.error("会话不属于当前用户，会话ID：{}，用户ID：{}", sessionId, userId);
                throw new SecurityException("无权删除此会话");
            }

            // 3. 执行逻辑删除 - 注意：这里传递两个参数
            int affectedRows = aiConversationMapper.deleteBySessionId(sessionId, userId);

            log.info("删除会话完成，影响行数：{}，会话ID：{}", affectedRows, sessionId);

            return affectedRows > 0;

        } catch (Exception e) {
            log.error("删除会话失败：", e);
            throw new RuntimeException("删除会话失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getConversationStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取会话数量
            List<String> sessionIds = aiConversationMapper.selectUserSessionIds(userId);
            int sessionCount = sessionIds.size();

            // 获取总消息数
            int totalMessages = 0;
            int totalTokens = 0;
            for (String sessionId : sessionIds) {
                List<AiConversation> messages = aiConversationMapper.selectBySessionId(sessionId);
                totalMessages += messages.size();
                for (AiConversation message : messages) {
                    if (message.getTokenUsage() != null) {
                        totalTokens += message.getTokenUsage();
                    }
                }
            }

            // 获取最近活跃时间
            Date lastActiveTime = null;
            if (!sessionIds.isEmpty()) {
                for (String sessionId : sessionIds) {
                    List<AiConversation> messages = aiConversationMapper.selectBySessionId(sessionId);
                    if (!messages.isEmpty()) {
                        messages.sort((a, b) -> b.getConversationTime().compareTo(a.getConversationTime()));
                        Date lastTime = messages.get(0).getConversationTime();
                        if (lastActiveTime == null || lastTime.after(lastActiveTime)) {
                            lastActiveTime = lastTime;
                        }
                    }
                }
            }

            stats.put("sessionCount", sessionCount);
            stats.put("totalMessages", totalMessages);
            stats.put("totalTokens", totalTokens);
            stats.put("lastActiveTime", lastActiveTime);

        } catch (Exception e) {
            log.error("获取对话统计失败：", e);
            stats.put("sessionCount", 0);
            stats.put("totalMessages", 0);
            stats.put("totalTokens", 0);
            stats.put("lastActiveTime", null);
        }

        return stats;
    }

    // 辅助方法
    private Map<String, Object> createDefaultSession(Long userId) {
        Map<String, Object> session = new HashMap<>();
        String defaultSessionId = "default-session-" + userId;

        session.put("sessionId", defaultSessionId);
        session.put("title", "欢迎使用AI心理疏导");
        session.put("lastTime", new Date());
        session.put("messageCount", 0);

        return session;
    }
}