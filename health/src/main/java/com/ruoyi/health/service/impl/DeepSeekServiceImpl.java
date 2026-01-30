package com.ruoyi.health.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.health.config.DeepSeekConfig;
import com.ruoyi.health.service.DeepSeekService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekServiceImpl.class);

    private final DeepSeekConfig deepSeekConfig;
    private OkHttpClient httpClient;

    // 存储会话历史（内存中）
    private final Map<String, List<Map<String, String>>> sessionHistories = new ConcurrentHashMap<>();

    // 使用构造函数注入
    @Autowired
    public DeepSeekServiceImpl(DeepSeekConfig deepSeekConfig) {
        this.deepSeekConfig = deepSeekConfig;
        log.info("DeepSeekServiceImpl初始化，配置状态：{}",
                deepSeekConfig != null ? "成功" : "失败");
    }

    @PostConstruct
    public void init() {
        // 确保配置已加载
        if (deepSeekConfig == null) {
            log.error("DeepSeekConfig注入失败！");
            throw new IllegalStateException("DeepSeekConfig注入失败");
        }

        log.info("初始化DeepSeek服务配置：");
        log.info("- Base URL: {}", deepSeekConfig.getBaseUrl());
        log.info("- Model: {}", deepSeekConfig.getModel());
        log.info("- Timeout: {}ms", deepSeekConfig.getTimeout());
        log.info("- Max Tokens: {}", deepSeekConfig.getMaxTokens());

        // 安全地显示API密钥状态
        String apiKey = deepSeekConfig.getKey();
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("- API Key: 未设置，将使用模拟回复模式");
        } else if (apiKey.startsWith("sk-test") || apiKey.contains("test-key") || apiKey.length() < 20) {
            log.warn("- API Key: 检测到测试密钥，将使用模拟回复模式");
        } else {
            log.info("- API Key: 已配置（长度: {}）", apiKey.length());
        }

        // 初始化HttpClient
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(deepSeekConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(deepSeekConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(deepSeekConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .build();

        log.info("DeepSeek服务初始化完成");
    }

    @Override
    public Map<String, Object> sendMessage(String userMessage, String sessionId, List<Map<String, Object>> history) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证配置和API密钥
            if (!isApiKeyValid()) {
                log.warn("API密钥无效，使用模拟回复，会话ID：{}", sessionId);
                return getMockResponse(userMessage, sessionId);
            }

            // 2. 构建消息数组
            List<Map<String, String>> messages = buildMessages(userMessage, sessionId, history);

            // 3. 构建请求体
            JSONObject requestBody = buildRequestBody(messages);

            // 4. 发送请求
            JSONObject apiResponse = callDeepSeekAPI(requestBody, deepSeekConfig.getKey());

            // 5. 处理响应
            if (apiResponse != null) {
                processApiResponse(apiResponse, result, sessionId, userMessage);
            } else {
                // API调用失败，返回模拟回复
                return getMockResponse(userMessage, sessionId);
            }

        } catch (Exception e) {
            log.error("调用DeepSeek API时发生异常，会话ID：{}", sessionId, e);
            result.put("success", false);
            result.put("error", "服务暂时不可用：" + e.getMessage());
            result.put("content", getFallbackResponse(userMessage));
            result.put("totalTokens", 0);
        }

        return result;
    }

    @Override
    public String analyzeEmotion(String message) {
        message = message.toLowerCase();

        if (message.contains("开心") || message.contains("高兴") || message.contains("愉快")) {
            return "积极";
        } else if (message.contains("压力") || message.contains("紧张") || message.contains("焦虑")) {
            return "焦虑";
        } else if (message.contains("难过") || message.contains("伤心") || message.contains("抑郁")) {
            return "低落";
        } else if (message.contains("生气") || message.contains("愤怒") || message.contains("烦躁")) {
            return "愤怒";
        } else {
            return "正常";
        }
    }

    @Override
    public void clearSessionHistory(String sessionId) {
        sessionHistories.remove(sessionId);
        log.info("清除会话历史，会话ID：{}", sessionId);
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

        // 测试密钥无效（用于模拟模式）
        if (key.startsWith("sk-test") || key.contains("test-key") || key.length() < 20) {
            return false;
        }

        return true;
    }

    /**
     * 构建消息数组
     */
    private List<Map<String, String>> buildMessages(String userMessage, String sessionId, List<Map<String, Object>> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统提示
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt());
        messages.add(systemMsg);

        // 从数据库历史构建消息（最多10条）
        if (history != null && !history.isEmpty()) {
            int startIndex = Math.max(0, history.size() - 10);
            for (int i = startIndex; i < history.size(); i++) {
                Map<String, Object> msg = history.get(i);
                Map<String, String> historyMsg = new HashMap<>();
                // 注意：数据库中的message_type：0为用户，1为AI
                String role = "0".equals(String.valueOf(msg.get("message_type"))) ? "user" : "assistant";
                historyMsg.put("role", role);
                historyMsg.put("content", String.valueOf(msg.get("content")));
                messages.add(historyMsg);
            }
        }

        // 添加当前用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        return messages;
    }

    /**
     * 构建请求体
     */
    private JSONObject buildRequestBody(List<Map<String, String>> messages) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", deepSeekConfig.getModel());
        requestBody.put("messages", convertToJSONArray(messages));
        requestBody.put("max_tokens", deepSeekConfig.getMaxTokens());
        requestBody.put("temperature", deepSeekConfig.getTemperature());
        requestBody.put("stream", false);

        return requestBody;
    }

    /**
     * 转换为JSONArray
     */
    private JSONArray convertToJSONArray(List<Map<String, String>> messages) {
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> msg : messages) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("role", msg.get("role"));
            jsonMsg.put("content", msg.get("content"));
            jsonArray.add(jsonMsg);
        }
        return jsonArray;
    }

    /**
     * 调用DeepSeek API
     */
    private JSONObject callDeepSeekAPI(JSONObject requestBody, String apiKey) throws IOException {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                requestBody.toJSONString()
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        log.info("调用DeepSeek API，模型：{}", deepSeekConfig.getModel());

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                log.error("DeepSeek API调用失败，状态码：{}，错误：{}", response.code(), errorBody);
                return null;
            }

            String responseBody = response.body().string();
            return JSON.parseObject(responseBody);
        }
    }

    /**
     * 处理API响应
     */
    private void processApiResponse(JSONObject apiResponse, Map<String, Object> result,
                                    String sessionId, String userMessage) {
        try {
            if (apiResponse.containsKey("choices") && !apiResponse.getJSONArray("choices").isEmpty()) {
                JSONObject choice = apiResponse.getJSONArray("choices").getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");

                // 获取token使用情况
                JSONObject usage = apiResponse.getJSONObject("usage");
                int promptTokens = usage.getIntValue("prompt_tokens");
                int completionTokens = usage.getIntValue("completion_tokens");
                int totalTokens = usage.getIntValue("total_tokens");

                log.info("API调用成功，Prompt Tokens：{}，Completion Tokens：{}，Total Tokens：{}",
                        promptTokens, completionTokens, totalTokens);

                result.put("success", true);
                result.put("content", content);
                result.put("promptTokens", promptTokens);
                result.put("completionTokens", completionTokens);
                result.put("totalTokens", totalTokens);
                result.put("model", deepSeekConfig.getModel());

                // 保存到会话历史
                saveToSessionHistory(sessionId, userMessage, content);

            } else {
                log.error("API响应格式错误，缺少choices字段");
                result.put("success", false);
                result.put("error", "API响应格式错误");
                result.put("content", getFallbackResponse(userMessage));
            }
        } catch (Exception e) {
            log.error("解析API响应失败：", e);
            result.put("success", false);
            result.put("error", "解析响应失败：" + e.getMessage());
            result.put("content", getFallbackResponse(userMessage));
        }
    }

    /**
     * 保存到会话历史
     */
    private void saveToSessionHistory(String sessionId, String userMessage, String aiResponse) {
        sessionHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // 保存用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        sessionHistories.get(sessionId).add(userMsg);

        // 保存AI回复
        Map<String, String> aiMsg = new HashMap<>();
        aiMsg.put("role", "assistant");
        aiMsg.put("content", aiResponse);
        sessionHistories.get(sessionId).add(aiMsg);

        // 限制历史长度（最多保留20条消息）
        if (sessionHistories.get(sessionId).size() > 20) {
            sessionHistories.put(sessionId,
                    new ArrayList<>(sessionHistories.get(sessionId).subList(
                            sessionHistories.get(sessionId).size() - 10,
                            sessionHistories.get(sessionId).size()
                    )));
        }
    }

    /**
     * 模拟回复（用于测试或API不可用时）
     */
    private Map<String, Object> getMockResponse(String userMessage, String sessionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("content", getFallbackResponse(userMessage));
        result.put("promptTokens", estimateTokenUsage(userMessage));
        result.put("completionTokens", 100);
        result.put("totalTokens", estimateTokenUsage(userMessage) + 100);
        result.put("model", "deepseek-chat-mock");
        result.put("isMock", true);

        log.info("使用模拟回复，会话ID：{}", sessionId);
        return result;
    }

    /**
     * 备选回复
     */
    private String getFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase();

        if (message.contains("开心") || message.contains("高兴") || message.contains("快乐")) {
            return "听到您感到开心，我也为您感到高兴！😊 积极情绪对我们的心理健康非常重要。\n\n" +
                    "您可以尝试：\n" +
                    "1. **记录美好时刻**：把开心的事情写下来，将来回顾\n" +
                    "2. **分享喜悦**：和信任的人分享您的快乐\n" +
                    "3. **感恩练习**：每天想三件值得感恩的事\n\n" +
                    "您愿意分享是什么让您感到开心的吗？";

        } else if (message.contains("压力") || message.contains("紧张") || message.contains("焦虑")) {
            return "感受到您的压力，压力是很多人都会经历的挑战。💪\n\n" +
                    "以下是一些压力管理建议：\n" +
                    "1. **深呼吸练习**：尝试4-7-8呼吸法\n" +
                    "2. **时间管理**：优先处理重要且紧急的事情\n" +
                    "3. **身体活动**：适量运动帮助释放压力荷尔蒙\n" +
                    "4. **放松技巧**：渐进式肌肉放松\n\n" +
                    "您可以具体说说最近的压力来源吗？";

        } else if (message.contains("难过") || message.contains("悲伤") || message.contains("伤心")) {
            return "感受到您的难过，情绪低落时，允许自己感受这些情绪是很重要的。🤗\n\n" +
                    "您可以尝试：\n" +
                    "1. **情绪表达**：写日记、画画表达情感\n" +
                    "2. **自我关怀**：像对待朋友那样对待自己\n" +
                    "3. **寻找支持**：寻求朋友或专业人士的帮助\n\n" +
                    "如果您愿意，可以多聊聊您的感受，我会认真倾听。";

        } else {
            return "感谢您的分享。作为心理健康助手，我在这里为您提供支持。\n\n" +
                    "心理健康是整体健康的重要组成部分，我们可以一起探讨：\n" +
                    "• 情绪管理和调节技巧\n" +
                    "• 压力应对策略\n" +
                    "• 自我关怀和自我接纳\n\n" +
                    "如果您愿意分享更多，我可以提供更针对性的建议。";
        }
    }

    /**
     * 估算token使用量
     */
    private int estimateTokenUsage(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 简单估算：中文字符约2个token
        return (int) (text.length() * 1.5);
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() { return "你是一名专业的心理咨询师AI助手，正在为心理健康评估系统的用户提供心理疏导服务。请遵循以下指导原则：\n" + "1. 提供温暖、共情、专业的心理支持，语气亲切自然\n" + "2. 避免给出医学诊断或治疗建议，如有严重心理问题建议寻求专业帮助\n" + "3. 鼓励积极思维和健康的生活方式\n" + "4. 提供实用的心理健康建议和技巧\n" + "5. 根据用户情绪调整回应方式\n" + "6. 保持对话的连贯性和一致性\n" + "7. 尊重用户的隐私和感受\n" + "8. 在适当时机提供情绪调节方法\n" + "9. 使用中文回复，语言简洁明了\n" + "10. 如果用户提到自杀、自伤等危险行为，必须明确建议立即寻求专业帮助\n\n" + "你可以帮助用户处理以下问题：\n" + "- 情绪疏导：焦虑、抑郁、压力、愤怒等情绪管理\n" + "- 压力管理：工作压力、学习压力、生活压力\n" + "- 人际关系：家庭关系、朋友关系、职场关系\n" + "- 自我成长：自信心建立、目标设定、习惯养成\n" + "- 睡眠问题：失眠、睡眠质量差\n" + "- 其他心理健康相关问题\n\n" + "请根据用户的具体问题提供针对性的建议和支持。"; }

    /**
     * 获取指定会话的历史记录
     */
    public List<Map<String, String>> getSessionHistory(String sessionId) {
        return sessionHistories.getOrDefault(sessionId, new ArrayList<>());
    }
}