package com.ruoyi.system.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.health.domain.AssessmentRecord;
import com.ruoyi.health.service.IAssessmentRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.stereotype.Component;

/**
 * 评估记录Controller
 *
 * @author GGbond
 * @date 2025-12-17
 */
@Component
@RestController
@RequestMapping("/health/record")
public class AssessmentRecordController extends BaseController
{
    public AssessmentRecordController() {
        System.out.println("=== AssessmentRecordController 被 Spring 创建 ===");
        System.out.println("=== 控制器路径: /health/record ===");
    }

    @Autowired
    private IAssessmentRecordService assessmentRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public AjaxResult test() {
        System.out.println("=== /health/record/test 被调用 ===");
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "接口正常工作");
        result.put("controller", "AssessmentRecordController");
        result.put("timestamp", new Date());
        return AjaxResult.success(result);
    }

    /**
     * 检查认证状态
     */
    @GetMapping("/checkAuth")
    public AjaxResult checkAuth() {
        System.out.println("=== /health/record/checkAuth 被调用 ===");

        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error(401, "未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("userId", loginUser.getUserId());
        result.put("userName", loginUser.getUsername());
        result.put("isAdmin", loginUser.getUser().isAdmin());
        result.put("timestamp", new Date());

        return AjaxResult.success(result);
    }

    /**
     * 用户提交心理评估
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody AssessmentRecord assessmentRecord)
    {
        try {
            System.out.println("=== /health/record/submit 被调用 ===");
            System.out.println("接收到测评提交请求: " + assessmentRecord);

            LoginUser loginUser = getLoginUser();
            if (loginUser == null) {
                return AjaxResult.error(401, "用户未登录");
            }

            // 设置用户信息
            assessmentRecord.setUserId(loginUser.getUserId());
            assessmentRecord.setUserName(loginUser.getUsername());

            // 计算评估结果
            calculateAssessmentResult(assessmentRecord);

            System.out.println("计算后的结果: " + assessmentRecord);

            int result = assessmentRecordService.insertAssessmentRecord(assessmentRecord);
            if (result > 0) {
                // 返回保存的记录
                AssessmentRecord savedRecord = assessmentRecordService.selectAssessmentRecordById(assessmentRecord.getId());
                return AjaxResult.success("测评提交成功", savedRecord);
            } else {
                return AjaxResult.error("测评提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 用户获取自己的评估记录
     */
    @GetMapping("/my/list")
    public AjaxResult myList(AssessmentRecord assessmentRecord)
    {
        try {
            System.out.println("=== /health/record/my/list 被调用 ===");

            LoginUser loginUser = getLoginUser();
            if (loginUser == null) {
                System.out.println("用户未登录");
                return AjaxResult.error(401, "请先登录");
            }

            System.out.println("用户ID: " + loginUser.getUserId());
            assessmentRecord.setUserId(loginUser.getUserId());

            startPage();
            List<AssessmentRecord> list = assessmentRecordService.selectAssessmentRecordList(assessmentRecord);
            System.out.println("查询到 " + (list != null ? list.size() : 0) + " 条记录");

            return AjaxResult.success(getDataTable(list));
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取记录失败: " + e.getMessage());
        }
    }

    /**
     * 用户获取自己的评估记录详情
     */
    @GetMapping("/my/{id}")
    public AjaxResult getMyInfo(@PathVariable("id") Long id)
    {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error(401, "用户未登录");
        }

        AssessmentRecord record = assessmentRecordService.selectAssessmentRecordById(id);
        if (record == null) {
            return AjaxResult.error("记录不存在");
        }
        // 只能查看自己的记录
        if (!record.getUserId().equals(loginUser.getUserId())) {
            return AjaxResult.error("无权查看此记录");
        }
        return AjaxResult.success(record);
    }

    /**
     * 用户删除自己的评估记录
     */
    @DeleteMapping("/my/{id}")
    public AjaxResult removeMy(@PathVariable Long id)
    {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error(401, "用户未登录");
        }

        AssessmentRecord record = assessmentRecordService.selectAssessmentRecordById(id);
        if (record == null) {
            return AjaxResult.error("记录不存在");
        }
        // 只能删除自己的记录
        if (!record.getUserId().equals(loginUser.getUserId())) {
            return AjaxResult.error("无权删除此记录");
        }
        return toAjax(assessmentRecordService.deleteAssessmentRecordById(id));
    }

    /**
     * 用户导出单个报告
     */
    @GetMapping("/export/{id}")
    public void exportReport(@PathVariable("id") Long id, HttpServletResponse response)
    {
        try {
            System.out.println("=== /health/record/export/{id} 被调用，ID: " + id + " ===");

            LoginUser loginUser = getLoginUser();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }

            AssessmentRecord record = assessmentRecordService.selectAssessmentRecordById(id);
            if (record == null) {
                throw new RuntimeException("记录不存在");
            }

            // 只能导出自己的记录
            if (!record.getUserId().equals(loginUser.getUserId())) {
                throw new RuntimeException("无权导出此记录");
            }

            // 将单条记录放入列表
            List<AssessmentRecord> list = new ArrayList<>();
            list.add(record);

            // 创建ExcelUtil实例
            ExcelUtil<AssessmentRecord> util = new ExcelUtil<>(AssessmentRecord.class);

            // 设置文件名
            String fileName = "心理评估报告_" + record.getUserName() + "_" +
                    DateUtils.dateTimeNow("yyyyMMddHHmmss") + ".xlsx";

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 导出 Excel
            util.exportExcel(response, list, "心理评估报告");

            System.out.println("=== 单个报告导出成功 ===");

        } catch (Exception e) {
            System.err.println("导出报告失败: " + e.getMessage());
            e.printStackTrace();
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 用户批量导出报告
     */
    @GetMapping("/my/export")
    public void exportMyReports(HttpServletResponse response, AssessmentRecord assessmentRecord)
    {
        try {
            System.out.println("=== /health/record/my/export 被调用 ===");

            LoginUser loginUser = getLoginUser();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }

            // 只能导出自己的记录
            assessmentRecord.setUserId(loginUser.getUserId());

            // 获取用户的评估记录
            List<AssessmentRecord> list = assessmentRecordService.selectAssessmentRecordList(assessmentRecord);

            if (list == null || list.isEmpty()) {
                throw new RuntimeException("没有可导出的记录");
            }

            System.out.println("=== 导出 " + list.size() + " 条记录 ===");

            // 使用若依的 ExcelUtil 工具类
            ExcelUtil<AssessmentRecord> util = new ExcelUtil<>(AssessmentRecord.class);

            // 设置文件名
            String fileName = "我的心理评估报告_" + loginUser.getUsername() + "_" +
                    DateUtils.dateTimeNow("yyyyMMddHHmmss") + ".xlsx";

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 导出 Excel
            util.exportExcel(response, list, "我的心理评估报告");

            System.out.println("=== 批量报告导出成功 ===");

        } catch (Exception e) {
            System.err.println("导出报告失败: " + e.getMessage());
            e.printStackTrace();
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 查询评估记录列表（管理员）
     */
    @PreAuthorize("@ss.hasPermi('health:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessmentRecord assessmentRecord)
    {
        // 普通用户只能查看自己的记录
        LoginUser loginUser = getLoginUser();
        if (loginUser != null && !loginUser.getUser().isAdmin()) {
            assessmentRecord.setUserId(loginUser.getUserId());
        }
        startPage();
        List<AssessmentRecord> list = assessmentRecordService.selectAssessmentRecordList(assessmentRecord);
        return getDataTable(list);
    }

    /**
     * 导出评估记录列表（管理员用）
     */
    @PreAuthorize("@ss.hasPermi('health:record:export')")
    @Log(title = "评估记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssessmentRecord assessmentRecord)
    {
        List<AssessmentRecord> list = assessmentRecordService.selectAssessmentRecordList(assessmentRecord);
        ExcelUtil<AssessmentRecord> util = new ExcelUtil<AssessmentRecord>(AssessmentRecord.class);

        // 设置文件名
        String fileName = "评估记录数据_" + DateUtils.dateTimeNow("yyyyMMddHHmmss") + ".xlsx";

        // 设置响应头
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        util.exportExcel(response, list, "评估记录数据");
    }

    /**
     * 获取评估记录详细信息（管理员用）
     */
    @PreAuthorize("@ss.hasPermi('health:record:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(assessmentRecordService.selectAssessmentRecordById(id));
    }

    /**
     * 新增评估记录（管理员用）
     */
    @PreAuthorize("@ss.hasPermi('health:record:add')")
    @Log(title = "评估记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssessmentRecord assessmentRecord)
    {
        return toAjax(assessmentRecordService.insertAssessmentRecord(assessmentRecord));
    }

    /**
     * 修改评估记录（管理员用）
     */
    @PreAuthorize("@ss.hasPermi('health:record:edit')")
    @Log(title = "评估记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssessmentRecord assessmentRecord)
    {
        return toAjax(assessmentRecordService.updateAssessmentRecord(assessmentRecord));
    }

    /**
     * 删除评估记录（管理员用）
     */
    @PreAuthorize("@ss.hasPermi('health:record:remove')")
    @Log(title = "评估记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(assessmentRecordService.deleteAssessmentRecordByIds(ids));
    }

    /**
     * 计算评估结果（核心逻辑）
     */
    private void calculateAssessmentResult(AssessmentRecord record) {
        try {
            System.out.println("开始计算评估结果");

            // 计算总分
            Long totalScore = calculateTotalScore(record.getAnswersJson());
            record.setTotalScore(totalScore);

            // 计算标准分和评估结果
            double standardScore = 0;
            String riskLevel = "低风险"; // 默认风险等级

            if ("SDS".equals(record.getScaleCode())) {
                standardScore = totalScore * 1.25;
                record.setStandardScore(standardScore);

                if (standardScore < 53) {
                    record.setResultLevel("正常");
                    riskLevel = "低风险";
                } else if (standardScore < 63) {
                    record.setResultLevel("轻度抑郁");
                    riskLevel = "中低风险";
                } else if (standardScore < 73) {
                    record.setResultLevel("中度抑郁");
                    riskLevel = "中高风险";
                } else {
                    record.setResultLevel("重度抑郁");
                    riskLevel = "高风险";
                }

            } else if ("SAS".equals(record.getScaleCode())) {
                standardScore = totalScore * 1.25;
                record.setStandardScore(standardScore);

                if (standardScore < 50) {
                    record.setResultLevel("正常");
                    riskLevel = "低风险";
                } else if (standardScore < 60) {
                    record.setResultLevel("轻度焦虑");
                    riskLevel = "中低风险";
                } else if (standardScore < 70) {
                    record.setResultLevel("中度焦虑");
                    riskLevel = "中高风险";
                } else {
                    record.setResultLevel("重度焦虑");
                    riskLevel = "高风险";
                }
            } else {
                record.setResultLevel("未知");
                riskLevel = "未知";
            }

            // 设置风险等级
            record.setRiskLevel(riskLevel);

            // 生成建议
            String suggestion = generateSuggestion(record.getResultLevel());
            record.setSuggestion(suggestion);

            System.out.println("最终结果 - totalScore: " + record.getTotalScore());
            System.out.println("最终结果 - resultLevel: " + record.getResultLevel());
            System.out.println("最终结果 - riskLevel: " + record.getRiskLevel());
            System.out.println("最终结果 - suggestion: " + record.getSuggestion());

        } catch (Exception e) {
            System.err.println("计算评估结果失败: " + e.getMessage());
            e.printStackTrace();
            record.setTotalScore(0L);
            record.setResultLevel("计算错误");
            record.setRiskLevel("未知");
            record.setSuggestion("评估结果计算异常，请联系管理员");
        }
    }

    /**
     * 计算总分
     */
    private Long calculateTotalScore(String answersJson) {
        try {
            if (answersJson == null || answersJson.isEmpty()) {
                System.out.println("答案JSON为空");
                return 0L;
            }

            System.out.println("解析答案JSON: " + answersJson);

            // 使用 ObjectMapper 解析 JSON
            JsonNode rootNode = objectMapper.readTree(answersJson);
            long totalScore = 0;

            if (rootNode.isArray()) {
                for (JsonNode answerNode : rootNode) {
                    JsonNode answerValue = answerNode.get("answer");
                    if (answerValue != null && answerValue.isNumber()) {
                        int score = answerValue.asInt();
                        // 确保分数在有效范围内（1-4）
                        if (score >= 1 && score <= 4) {
                            totalScore += score;
                        } else if (score == 0) {
                            // 如果是0分（未回答），按1分计算（最低分）
                            totalScore += 1;
                        }
                        System.out.println("题目答案: " + score);
                    }
                }
            }

            System.out.println("计算的总分: " + totalScore);
            return totalScore;

        } catch (Exception e) {
            System.err.println("计算总分失败: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 生成建议
     */
    private String generateSuggestion(String resultLevel) {
        if (resultLevel == null) return "请关注心理健康";

        if (resultLevel.contains("正常")) {
            return "您的心理状态良好，请继续保持健康的生活方式。";
        } else if (resultLevel.contains("轻度")) {
            return "建议进行适当的自我调节，如运动、冥想、与朋友交流等。";
        } else if (resultLevel.contains("中度")) {
            return "建议寻求心理咨询师的帮助，进行专业的心理疏导。";
        } else if (resultLevel.contains("重度")) {
            return "建议立即寻求专业心理治疗，必要时请及时就医。";
        }
        return "请关注您的心理健康状态。";
    }
}