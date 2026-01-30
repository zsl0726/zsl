package com.ruoyi.health.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.AppointmentRating;
import com.ruoyi.health.domain.ConsultationAppointment;
import com.ruoyi.health.service.IAppointmentRatingService;
import com.ruoyi.health.service.IConsultationAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/health/consultation/appointment")
public class ConsultationAppointmentController extends BaseController {

    @Autowired
    private IConsultationAppointmentService consultationAppointmentService;

    @Autowired
    private IAppointmentRatingService appointmentRatingService;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    // ==================== 用户端接口 ====================

    @PreAuthorize("@ss.hasPermi('consultation:appointment:user')")
    @PostMapping
    @Log(title = "心理咨询预约", businessType = BusinessType.INSERT)
    public AjaxResult createAppointment(@Validated @RequestBody ConsultationAppointment consultationAppointment) {
        // 验证预约日期不能是过去（允许今天）
        if (consultationAppointment.getAppointmentDate() != null) {
            Date appointmentDate = consultationAppointment.getAppointmentDate();
            Date now = new Date();

            // 转换为yyyy-MM-dd格式进行比较
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String appointmentDateStr = sdf.format(appointmentDate);
            String nowDateStr = sdf.format(now);

            try {
                Date appointmentDateOnly = sdf.parse(appointmentDateStr);
                Date nowDateOnly = sdf.parse(nowDateStr);

                // 如果预约日期在今天之前，返回错误
                if (appointmentDateOnly.before(nowDateOnly)) {
                    return AjaxResult.error("预约日期不能是过去的时间");
                }

                // 如果是今天，检查时间段是否合理
                if (appointmentDateOnly.equals(nowDateOnly)) {
                    String timeSlot = consultationAppointment.getTimeSlot();
                    if (timeSlot != null && !timeSlot.isEmpty()) {
                        // 获取当前时间
                        Calendar nowCal = Calendar.getInstance();
                        int currentHour = nowCal.get(Calendar.HOUR_OF_DAY);

                        // 根据时间段判断是否已过去
                        Map<String, Integer> timeSlotHourMap = new HashMap<>();
                        timeSlotHourMap.put("morning_1", 9);
                        timeSlotHourMap.put("morning_2", 10);
                        timeSlotHourMap.put("morning_3", 11);
                        timeSlotHourMap.put("afternoon_1", 14);
                        timeSlotHourMap.put("afternoon_2", 15);
                        timeSlotHourMap.put("afternoon_3", 16);
                        timeSlotHourMap.put("evening_1", 18);
                        timeSlotHourMap.put("evening_2", 19);

                        Integer slotHour = timeSlotHourMap.get(timeSlot);
                        if (slotHour != null && slotHour <= currentHour) {
                            return AjaxResult.error("不能选择今天已经过去的时间段");
                        }
                    }
                }

            } catch (ParseException e) {
                logger.error("日期格式化失败", e);
                return AjaxResult.error("日期格式错误");
            }
        }

        try {
            int result = consultationAppointmentService.createAppointment(consultationAppointment);
            return result > 0 ? AjaxResult.success("预约成功") : AjaxResult.error("预约失败");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:my')")
    @GetMapping("/my")
    public TableDataInfo getMyAppointments(@RequestParam(value = "status", required = false) String status) {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<ConsultationAppointment> list = consultationAppointmentService.selectUserAppointments(userId, status);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:user')")
    @PutMapping("/{id}/cancel")
    @Log(title = "取消预约", businessType = BusinessType.UPDATE)
    public AjaxResult cancelAppointment(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        int result = consultationAppointmentService.cancelAppointment(id, userId);
        return result > 0 ? AjaxResult.success("取消成功") : AjaxResult.error("取消失败");
    }

    // ==================== 咨询师端接口 ====================

    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @GetMapping("/psychologist")
    public TableDataInfo getPsychologistAppointments(@RequestParam(value = "status", required = false) String status) {
        Long psychologistId = SecurityUtils.getUserId();
        startPage();
        List<ConsultationAppointment> list = consultationAppointmentService.selectPsychologistAppointments(psychologistId, status);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @PutMapping("/{id}/accept")
    @Log(title = "接受预约", businessType = BusinessType.UPDATE)
    public AjaxResult acceptAppointment(@PathVariable Long id) {
        int result = consultationAppointmentService.acceptAppointment(id);
        return result > 0 ? AjaxResult.success("接受成功") : AjaxResult.error("接受失败");
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @PutMapping("/{id}/reject")
    @Log(title = "拒绝预约", businessType = BusinessType.UPDATE)
    public AjaxResult rejectAppointment(@PathVariable Long id) {
        int result = consultationAppointmentService.rejectAppointment(id);
        return result > 0 ? AjaxResult.success("已拒绝") : AjaxResult.error("操作失败");
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @PutMapping("/{id}/complete")
    @Log(title = "完成预约", businessType = BusinessType.UPDATE)
    public AjaxResult completeAppointment(@PathVariable Long id, @RequestParam(value = "counselingNotes", required = false) String counselingNotes) {
        int result = consultationAppointmentService.completeAppointment(id, counselingNotes);
        return result > 0 ? AjaxResult.success("已完成") : AjaxResult.error("操作失败");
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @PutMapping("/{id}/notes")
    @Log(title = "更新咨询笔记", businessType = BusinessType.UPDATE)
    public AjaxResult updateNotes(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String counselingNotes = request.get("counselingNotes");
        ConsultationAppointment appointment = new ConsultationAppointment();
        appointment.setId(id);
        appointment.setCounselingNotes(counselingNotes);
        int result = consultationAppointmentService.updateConsultationAppointment(appointment);
        return result > 0 ? AjaxResult.success("笔记更新成功") : AjaxResult.error("更新失败");
    }

    // ==================== 统计接口 ====================

    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Long psychologistId = SecurityUtils.getUserId();
        Map<String, Object> statistics = consultationAppointmentService.getAppointmentStatistics(psychologistId);
        return AjaxResult.success(statistics);
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @GetMapping("/today")
    public AjaxResult getTodayAppointments() {
        Long psychologistId = SecurityUtils.getUserId();
        List<ConsultationAppointment> todayAppointments = consultationAppointmentService.getTodayAppointments(psychologistId);
        return AjaxResult.success(todayAppointments);
    }

    // ==================== 咨询师首页专用接口 ====================

    /**
     * 获取咨询师今日预约（咨询师首页专用）
     * 返回今日所有预约，支持分页
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @GetMapping("/psychologist/today")
    public TableDataInfo getTodayAppointmentsForPsychologist(@RequestParam(required = false) String date) {
        Long psychologistId = SecurityUtils.getUserId();

        // 如果没传日期，使用当前日期
        if (date == null || date.isEmpty()) {
            date = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, new Date());
        }

        startPage();
        List<ConsultationAppointment> list = consultationAppointmentService.selectTodayAppointmentsByPsychologistId(psychologistId, date);
        return getDataTable(list);
    }

    /**
     * 获取咨询师待处理预约
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @GetMapping("/psychologist/pending")
    public TableDataInfo getPendingAppointmentsForPsychologist() {
        Long psychologistId = SecurityUtils.getUserId();
        startPage();
        List<ConsultationAppointment> list = consultationAppointmentService.selectPendingAppointmentsByPsychologistId(psychologistId);
        return getDataTable(list);
    }

    /**
     * 获取咨询师首页统计信息
     * 包含：今日预约数、待处理数、已完成数、平均评分
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @GetMapping("/psychologist/dashboard")
    public AjaxResult getPsychologistDashboard() {
        Long psychologistId = SecurityUtils.getUserId();
        Map<String, Object> dashboard = new HashMap<>();

        // 今日预约数量
        int todayCount = consultationAppointmentService.countTodayAppointments(psychologistId);
        dashboard.put("todayAppointments", todayCount);

        // 待处理预约数量
        int pendingCount = consultationAppointmentService.countPendingAppointments(psychologistId);
        dashboard.put("pendingAppointments", pendingCount);

        // 已完成咨询数量
        int completedCount = consultationAppointmentService.countCompletedAppointments(psychologistId);
        dashboard.put("completedSessions", completedCount);

        // 平均评分 - 从评价服务获取真实数据
        try {
            Map<String, Object> ratingInfo = appointmentRatingService.getPsychologistRatingInfo(psychologistId);
            dashboard.put("averageRating", ratingInfo.get("averageRating"));
            dashboard.put("totalRatings", ratingInfo.get("totalRatings"));
        } catch (Exception e) {
            logger.warn("获取评分信息失败: ", e);
            dashboard.put("averageRating", 0.0);
            dashboard.put("totalRatings", 0);
        }

        // 今日预约详细列表（前5条）
        List<ConsultationAppointment> todayList = consultationAppointmentService.getTodayAppointments(psychologistId);
        dashboard.put("todayAppointmentsList", todayList);

        return AjaxResult.success(dashboard);
    }

    // ==================== 咨询评价接口 ====================

    /**
     * 提交评价（用户端）
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:user')")
    @Log(title = "提交评价", businessType = BusinessType.INSERT)
    @PostMapping("/rating/submit")
    public AjaxResult submitRating(@RequestBody AppointmentRating rating) {
        return appointmentRatingService.submitRating(rating);
    }

    /**
     * 获取咨询师评分信息（用于咨询师首页）
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:psychologist')")
    @GetMapping("/psychologist/rating")
    public AjaxResult getPsychologistRating() {
        Long psychologistId = SecurityUtils.getUserId();
        Map<String, Object> ratingInfo = appointmentRatingService.getPsychologistRatingInfo(psychologistId);
        return AjaxResult.success(ratingInfo);
    }

    /**
     * 获取特定咨询师的评分信息（用户端查看咨询师评分）
     */
    @GetMapping("/psychologist/rating/{psychologistId}")
    public AjaxResult getPsychologistRatingById(@PathVariable Long psychologistId) {
        Map<String, Object> ratingInfo = appointmentRatingService.getPsychologistRatingInfo(psychologistId);
        return AjaxResult.success(ratingInfo);
    }

    /**
     * 根据预约ID查询评价详情
     */
    @GetMapping("/rating/appointment/{appointmentId}")
    public AjaxResult getRatingByAppointmentId(@PathVariable Long appointmentId) {
        AppointmentRating rating = appointmentRatingService.selectByAppointmentId(appointmentId);
        return AjaxResult.success(rating);
    }

    /**
     * 获取咨询师的所有评价列表
     */
    @GetMapping("/psychologist/ratings/{psychologistId}")
    public TableDataInfo getPsychologistRatings(@PathVariable Long psychologistId) {
        startPage();
        List<AppointmentRating> ratings = appointmentRatingService.selectByPsychologistId(psychologistId);
        return getDataTable(ratings);
    }

    // ==================== 通用统计接口 ====================

    @GetMapping("/count")
    public AjaxResult getAppointmentCount() {
        Long userId = SecurityUtils.getUserId();
        Long psychologistId = null;

        // 根据用户ID判断是否为咨询师
        if (userId >= 100 && userId <= 199) {
            psychologistId = userId;
            userId = null;
        }

        Map<String, Long> statusCount = consultationAppointmentService.getAppointmentCountByStatus(userId, psychologistId);
        return AjaxResult.success(statusCount);
    }

    // ==================== 管理员接口 ====================

    @PreAuthorize("@ss.hasPermi('consultation:appointment:list')")
    @GetMapping("/list")
    public TableDataInfo list(ConsultationAppointment consultationAppointment) {
        startPage();
        List<ConsultationAppointment> list = consultationAppointmentService.selectConsultationAppointmentList(consultationAppointment);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(consultationAppointmentService.selectConsultationAppointmentById(id));
    }

    @PreAuthorize("@ss.hasPermi('consultation:appointment:remove')")
    @DeleteMapping("/{ids}")
    @Log(title = "删除预约", businessType = BusinessType.DELETE)
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(consultationAppointmentService.deleteConsultationAppointmentByIds(ids));
    }

    // ==================== 咨询师查询接口 ====================

    /**
     * 获取咨询师列表
     * 权限：任何已登录用户都可以查看咨询师列表
     */
    @GetMapping("/psychologists")
    public AjaxResult getPsychologists() {
        try {
            // 从数据库查询咨询师数据
            List<Map<String, Object>> psychologists = queryPsychologistsFromDatabase();

            // 为每个咨询师添加评分信息
            for (Map<String, Object> psychologist : psychologists) {
                Long psychologistId = (Long) psychologist.get("userId");
                try {
                    Map<String, Object> ratingInfo = appointmentRatingService.getPsychologistRatingInfo(psychologistId);
                    psychologist.put("averageRating", ratingInfo.get("averageRating"));
                    psychologist.put("totalRatings", ratingInfo.get("totalRatings"));
                } catch (Exception e) {
                    logger.warn("获取咨询师评分失败: ", e);
                    psychologist.put("averageRating", 0.0);
                    psychologist.put("totalRatings", 0);
                }
            }

            return AjaxResult.success(psychologists);

        } catch (Exception e) {
            logger.error("获取咨询师列表失败", e);
            return AjaxResult.error("获取咨询师列表失败：" + e.getMessage());
        }
    }

    /**
     * 从数据库查询咨询师数据
     */
    private List<Map<String, Object>> queryPsychologistsFromDatabase() {
        List<Map<String, Object>> psychologists = new ArrayList<>();

        try {
            // 检查是否有JdbcTemplate可用
            if (jdbcTemplate == null) {
                logger.warn("JdbcTemplate未注入，无法从数据库查询咨询师数据");
                return psychologists;
            }

            // 查询咨询师列表的SQL
            String sql = "SELECT " +
                    "u.user_id, " +
                    "u.user_name, " +
                    "u.nick_name, " +
                    "u.phonenumber, " +
                    "u.email, " +
                    "u.avatar, " +
                    "u.sex, " +
                    "u.status, " +
                    "u.create_time, " +
                    "r.role_name, " +
                    "r.role_key " +
                    "FROM sys_user u " +
                    "INNER JOIN sys_user_role ur ON u.user_id = ur.user_id " +
                    "INNER JOIN sys_role r ON ur.role_id = r.role_id " +
                    "WHERE u.status = '0' " +      // 用户状态为正常
                    "AND u.del_flag = '0' " +      // 未删除
                    "AND r.role_key = 'psychologist' " +  // 角色为咨询师
                    "ORDER BY u.user_id";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 格式化返回数据
            for (Map<String, Object> row : results) {
                Map<String, Object> psychologist = new HashMap<>();

                // 基本信息
                psychologist.put("userId", row.get("user_id"));
                psychologist.put("userName", row.get("user_name"));
                psychologist.put("nickName", row.get("nick_name"));
                psychologist.put("phone", row.get("phonenumber"));
                psychologist.put("email", row.get("email"));
                psychologist.put("avatar", row.get("avatar"));

                // 性别处理
                Object sex = row.get("sex");
                if (sex != null) {
                    psychologist.put("sex", sex.toString().equals("0") ? "男" : "女");
                } else {
                    psychologist.put("sex", "未知");
                }

                // 角色信息
                psychologist.put("roleName", row.get("role_name"));

                // 获取专业领域（如果数据库中有相关表）
                String specialty = getPsychologistSpecialtyFromDB((Long) row.get("user_id"));
                psychologist.put("specialty", specialty);

                // 获取咨询次数统计
                int consultationCount = getConsultationCount((Long) row.get("user_id"));
                psychologist.put("consultationCount", consultationCount);
                psychologist.put("experience", getExperienceLevel(consultationCount));

                // 添加到结果列表
                psychologists.add(psychologist);
            }

            logger.info("成功从数据库查询到 {} 位咨询师", psychologists.size());

        } catch (Exception e) {
            logger.error("查询咨询师数据失败", e);
            // 可以在这里添加降级逻辑，但根据要求，不返回示例数据
        }

        return psychologists;
    }

    /**
     * 从数据库获取咨询师的专业领域
     */
    private String getPsychologistSpecialtyFromDB(Long userId) {
        try {
            // 首先检查是否有专门的心理咨询师信息表
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'psychologist_profile'";
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);

            if (tableCount != null && tableCount > 0) {
                // 如果有专门的表，从中查询专业领域
                String specialtySql = "SELECT specialty FROM psychologist_profile WHERE user_id = ?";
                List<String> specialties = jdbcTemplate.queryForList(specialtySql, String.class, userId);
                if (!specialties.isEmpty()) {
                    return specialties.get(0);
                }
            }

            // 如果没有专门表，尝试从sys_user的remark字段获取
            String remarkSql = "SELECT remark FROM sys_user WHERE user_id = ?";
            List<String> remarks = jdbcTemplate.queryForList(remarkSql, String.class, userId);
            if (!remarks.isEmpty() && remarks.get(0) != null && !remarks.get(0).isEmpty()) {
                return remarks.get(0);
            }

        } catch (Exception e) {
            logger.warn("获取咨询师专业领域失败，用户ID: {}", userId, e);
        }

        // 默认返回通用的专业领域
        return "心理咨询";
    }

    /**
     * 获取咨询师的咨询次数
     */
    private int getConsultationCount(Long psychologistId) {
        try {
            String countSql = "SELECT COUNT(*) FROM consultation_appointment WHERE psychologist_id = ? AND status = 'completed' AND is_deleted = 0";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, psychologistId);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.warn("获取咨询次数失败，咨询师ID: {}", psychologistId, e);
            return 0;
        }
    }

    /**
     * 根据咨询次数确定经验级别
     */
    private String getExperienceLevel(int consultationCount) {
        if (consultationCount > 500) {
            return "资深专家";
        } else if (consultationCount > 200) {
            return "资深咨询师";
        } else if (consultationCount > 50) {
            return "经验丰富";
        } else if (consultationCount > 10) {
            return "初级咨询师";
        } else {
            return "新手咨询师";
        }
    }

    /**
     * 获取指定咨询师的详情
     */
    @GetMapping("/psychologist/{psychologistId}")
    public AjaxResult getPsychologistInfo(@PathVariable Long psychologistId) {
        try {
            // 从数据库查询咨询师详情
            Map<String, Object> psychologist = queryPsychologistDetailsFromDatabase(psychologistId);

            // 添加评分信息
            try {
                Map<String, Object> ratingInfo = appointmentRatingService.getPsychologistRatingInfo(psychologistId);
                psychologist.putAll(ratingInfo);
            } catch (Exception e) {
                logger.warn("获取咨询师评分信息失败: ", e);
                psychologist.put("averageRating", 0.0);
                psychologist.put("totalRatings", 0);
            }

            return AjaxResult.success(psychologist);

        } catch (Exception e) {
            logger.error("获取咨询师详情失败", e);
            return AjaxResult.error("获取咨询师详情失败");
        }
    }

    /**
     * 从数据库查询咨询师详情
     */
    private Map<String, Object> queryPsychologistDetailsFromDatabase(Long psychologistId) {
        Map<String, Object> psychologist = new HashMap<>();

        try {
            if (jdbcTemplate == null) {
                throw new Exception("JdbcTemplate未注入");
            }

            // 查询咨询师基本信息
            String sql = "SELECT " +
                    "u.user_id, " +
                    "u.user_name, " +
                    "u.nick_name, " +
                    "u.phonenumber, " +
                    "u.email, " +
                    "u.avatar, " +
                    "u.sex, " +
                    "u.status, " +
                    "u.create_time, " +
                    "u.remark " +
                    "FROM sys_user u " +
                    "INNER JOIN sys_user_role ur ON u.user_id = ur.user_id " +
                    "INNER JOIN sys_role r ON ur.role_id = r.role_id " +
                    "WHERE u.user_id = ? " +
                    "AND u.status = '0' " +
                    "AND u.del_flag = '0' " +
                    "AND r.role_key = 'psychologist'";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, psychologistId);

            if (results.isEmpty()) {
                psychologist.put("error", "未找到咨询师信息");
                return psychologist;
            }

            Map<String, Object> userInfo = results.get(0);

            // 基本信息
            psychologist.put("userId", userInfo.get("user_id"));
            psychologist.put("userName", userInfo.get("user_name"));
            psychologist.put("nickName", userInfo.get("nick_name"));
            psychologist.put("phone", userInfo.get("phonenumber"));
            psychologist.put("email", userInfo.get("email"));
            psychologist.put("avatar", userInfo.get("avatar"));

            // 性别处理
            Object sex = userInfo.get("sex");
            psychologist.put("sex", sex != null ? (sex.toString().equals("0") ? "男" : "女") : "未知");

            // 注册时间
            psychologist.put("joinDate", userInfo.get("create_time"));

            // 专业领域
            String specialty = getPsychologistSpecialtyFromDB(psychologistId);
            psychologist.put("specialty", specialty);

            // 咨询统计
            int totalConsultations = getConsultationCount(psychologistId);
            psychologist.put("consultationCount", totalConsultations);

            // 成功咨询次数
            String successCountSql = "SELECT COUNT(*) FROM consultation_appointment WHERE psychologist_id = ? AND status = 'completed' AND is_deleted = 0";
            Integer successCount = jdbcTemplate.queryForObject(successCountSql, Integer.class, psychologistId);
            psychologist.put("successCount", successCount != null ? successCount : 0);

            // 经验级别
            psychologist.put("experienceLevel", getExperienceLevel(totalConsultations));

            // 备注信息
            psychologist.put("description", userInfo.get("remark") != null ? userInfo.get("remark") : "专业心理咨询师");

            logger.info("成功查询咨询师详情，ID: {}", psychologistId);

        } catch (Exception e) {
            logger.error("查询咨询师详情失败，ID: {}", psychologistId, e);
            psychologist.put("error", "查询咨询师详情失败: " + e.getMessage());
        }

        return psychologist;
    }
}