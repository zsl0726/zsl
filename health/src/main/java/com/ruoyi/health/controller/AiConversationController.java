// src/main/java/com/ruoyi/health/controller/AiConversationController.java
package com.ruoyi.health.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.health.domain.AiConversation;
import com.ruoyi.health.service.IAiConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * AI对话记录Controller
 */
@RestController
@RequestMapping("/mental/conversation")
public class AiConversationController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AiConversationController.class);

    @Autowired
    private IAiConversationService aiConversationService;

    // ==================== 用户端接口 ====================

    /**
     * 用户端：获取我的会话列表
     */
    @GetMapping("/my/sessions")
    public AjaxResult getMySessions() {
        try {
            Long userId = getUserId();
            log.info("获取用户 {} 的会话列表", userId);

            List<Map<String, Object>> sessions = aiConversationService.getUserSessionList(userId);

            return success(sessions);
        } catch (Exception e) {
            log.error("获取会话列表失败：", e);
            return error("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 用户端：开始新的AI对话
     */
    @PostMapping("/my/start")
    public AjaxResult startNewConversation() {
        try {
            Long userId = getUserId();
            String username = getUsername();

            log.info("用户 {} 开始新对话", username);

            Map<String, Object> result = aiConversationService.startNewConversation(userId, username);

            return success(result);
        } catch (Exception e) {
            log.error("开始新对话失败：", e);
            return error("开始新对话失败：" + e.getMessage());
        }
    }

    /**
     * 用户端：发送消息给AI
     */
    @PostMapping("/my/send")
    public AjaxResult sendMessage(@RequestBody Map<String, String> params) {
        try {
            String sessionId = params.get("sessionId");
            String content = params.get("content");

            if (sessionId == null || content == null || content.trim().isEmpty()) {
                return error("参数错误：sessionId和content不能为空");
            }

            Long userId = getUserId();
            String username = getUsername();

            log.info("用户 {} 发送消息，会话ID：{}", username, sessionId);

            Map<String, Object> result = aiConversationService.sendMessage(sessionId, content, userId, username);

            if (result.get("success") != null && !(Boolean) result.get("success")) {
                return error(result.get("error") != null ? result.get("error").toString() : "消息发送失败");
            }

            return success(result);
        } catch (Exception e) {
            log.error("发送消息失败：", e);
            return error("发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 用户端：获取会话历史
     */
    @GetMapping("/my/history/{sessionId}")
    public AjaxResult getConversationHistory(@PathVariable("sessionId") String sessionId) {
        try {
            Long userId = getUserId();

            log.info("获取用户 {} 的会话历史，会话ID：{}", userId, sessionId);

            List<Map<String, Object>> messages = aiConversationService.getUserConversationHistory(sessionId, userId);

            return success(messages);
        } catch (Exception e) {
            log.error("获取会话历史失败：", e);
            return error("获取会话历史失败：" + e.getMessage());
        }
    }

    /**
     * 用户端：删除我的会话
     */
    @DeleteMapping("/my/session/{sessionId}")
    public AjaxResult deleteMySession(@PathVariable("sessionId") String sessionId) {
        try {
            Long userId = getUserId();

            log.info("用户 {} 删除会话：{}", userId, sessionId);

            boolean result = aiConversationService.deleteUserSession(sessionId, userId);

            if (result) {
                return success("会话删除成功");
            } else {
                return error("会话删除失败");
            }
        } catch (Exception e) {
            log.error("删除会话失败：", e);
            return error("删除会话失败：" + e.getMessage());
        }
    }

    /**
     * 用户端：获取对话统计
     */
    @GetMapping("/my/stats")
    public AjaxResult getConversationStats() {
        try {
            Long userId = getUserId();

            Map<String, Object> stats = aiConversationService.getConversationStats(userId);

            return success(stats);
        } catch (Exception e) {
            log.error("获取对话统计失败：", e);
            return error("获取对话统计失败：" + e.getMessage());
        }
    }

    // ==================== 管理端接口 ====================

    @PreAuthorize("@ss.hasPermi('mental:conversation:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiConversation aiConversation) {
        startPage();
        List<AiConversation> list = aiConversationService.selectAiConversationList(aiConversation);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mental:conversation:export')")
    @Log(title = "AI对话记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiConversation aiConversation) {
        List<AiConversation> list = aiConversationService.selectAiConversationList(aiConversation);
        ExcelUtil<AiConversation> util = new ExcelUtil<>(AiConversation.class);
        util.exportExcel(response, list, "AI对话记录数据");
    }

    @PreAuthorize("@ss.hasPermi('mental:conversation:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(aiConversationService.selectAiConversationById(id));
    }

    @PreAuthorize("@ss.hasPermi('mental:conversation:add')")
    @Log(title = "AI对话记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AiConversation aiConversation) {
        return toAjax(aiConversationService.insertAiConversation(aiConversation));
    }

    @PreAuthorize("@ss.hasPermi('mental:conversation:edit')")
    @Log(title = "AI对话记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AiConversation aiConversation) {
        return toAjax(aiConversationService.updateAiConversation(aiConversation));
    }

    @PreAuthorize("@ss.hasPermi('mental:conversation:remove')")
    @Log(title = "AI对话记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(aiConversationService.deleteAiConversationByIds(ids));
    }


}