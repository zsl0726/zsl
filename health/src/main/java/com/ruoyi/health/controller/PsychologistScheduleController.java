package com.ruoyi.health.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.PsychologistSchedule;
import com.ruoyi.health.service.IPsychologistScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 心理咨询师排班管理
 */
@RestController
@RequestMapping("/health/consultation/schedule")
public class PsychologistScheduleController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(PsychologistScheduleController.class);

    @Autowired
    private IPsychologistScheduleService psychologistScheduleService;

    // ==================== 排班管理接口 ====================

    /**
     * 查询心理咨询师排班列表
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @GetMapping("/list")
    public TableDataInfo list(PsychologistSchedule psychologistSchedule) {
        // 默认查询当前咨询师的排班
        Long psychologistId = SecurityUtils.getUserId();
        psychologistSchedule.setPsychologistId(psychologistId);

        startPage();
        List<PsychologistSchedule> list = psychologistScheduleService.selectPsychologistScheduleList(psychologistSchedule);
        return getDataTable(list);
    }

    /**
     * 获取排班详情
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(psychologistScheduleService.selectPsychologistScheduleById(id));
    }

    /**
     * 查询咨询师指定日期的排班
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @GetMapping("/by-date")
    public AjaxResult getScheduleByDate(@RequestParam String date) {
        Long psychologistId = SecurityUtils.getUserId();
        PsychologistSchedule schedule = psychologistScheduleService.selectScheduleByDate(
                psychologistId, DateUtils.parseDate(date));

        if (schedule == null) {
            schedule = new PsychologistSchedule();
            schedule.setPsychologistId(psychologistId);
            schedule.setScheduleDate(DateUtils.parseDate(date));
            schedule.setTimeSlots("");
            schedule.setMaxAppointments(5);
            schedule.setIsAvailable("1");
        }

        return AjaxResult.success(schedule);
    }

    /**
     * 获取可用时间段（公开接口，用户预约时使用）
     */
    @GetMapping("/available-slots")
    public AjaxResult getAvailableTimeSlots(
            @RequestParam Long psychologistId,
            @RequestParam String date) {
        try {
            logger.info("查询可用时间段，咨询师ID: {}, 日期: {}", psychologistId, date);

            PsychologistSchedule schedule = psychologistScheduleService.selectScheduleByPsychologistAndDate(
                    psychologistId, DateUtils.parseDate(date));

            List<String> availableSlots = new ArrayList<>();

            if (schedule != null && "1".equals(schedule.getIsAvailable())) {
                String timeSlots = schedule.getTimeSlots();
                if (timeSlots != null && !timeSlots.trim().isEmpty()) {
                    String[] slots = timeSlots.split(",");
                    for (String slot : slots) {
                        slot = slot.trim();
                        if (!slot.isEmpty()) {
                            availableSlots.add(slot);
                        }
                    }
                }
                logger.info("查询到排班记录，可用时间段: {}", availableSlots);
            } else {
                logger.warn("未找到咨询师ID: {} 在日期: {} 的排班记录或排班不可用", psychologistId, date);
            }

            return AjaxResult.success(availableSlots);

        } catch (Exception e) {
            logger.error("获取可用时间段失败", e);
            return AjaxResult.error("获取可用时间段失败");
        }
    }

    /**
     * 获取排班日历数据
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @GetMapping("/calendar")
    public AjaxResult getScheduleCalendar(@RequestParam String month) {
        Long psychologistId = SecurityUtils.getUserId();
        Map<String, Object> calendarData = psychologistScheduleService.getScheduleCalendar(psychologistId, month);
        return AjaxResult.success(calendarData);
    }

    // ==================== 操作接口 ====================

    /**
     * 新增排班
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @PostMapping
    @Log(title = "心理咨询师排班", businessType = BusinessType.INSERT)
    public AjaxResult add(@Validated @RequestBody PsychologistSchedule psychologistSchedule) {
        try {
            // 设置当前咨询师
            Long psychologistId = SecurityUtils.getUserId();
            String psychologistName = SecurityUtils.getUsername();
            psychologistSchedule.setPsychologistId(psychologistId);
            psychologistSchedule.setPsychologistName(psychologistName);

            // 验证必填字段
            if (psychologistSchedule.getScheduleDate() == null) {
                return AjaxResult.error("请选择排班日期");
            }

            // 如果时间段为空，设置为空字符串
            if (psychologistSchedule.getTimeSlots() == null) {
                psychologistSchedule.setTimeSlots("");
            }

            // 设置默认值
            if (psychologistSchedule.getMaxAppointments() == null) {
                psychologistSchedule.setMaxAppointments(5);
            }
            if (psychologistSchedule.getIsAvailable() == null) {
                psychologistSchedule.setIsAvailable("1");
            }

            int result = psychologistScheduleService.insertPsychologistSchedule(psychologistSchedule);
            return result > 0 ? AjaxResult.success("新增成功") : AjaxResult.error("新增失败");
        } catch (Exception e) {
            logger.error("新增排班失败", e);
            return AjaxResult.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改排班
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @PutMapping
    @Log(title = "心理咨询师排班", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@Validated @RequestBody PsychologistSchedule psychologistSchedule) {
        try {
            if (psychologistSchedule.getId() == null) {
                return AjaxResult.error("排班ID不能为空");
            }

            // 更新操作人信息
            psychologistSchedule.setUpdateBy(SecurityUtils.getUsername());

            int result = psychologistScheduleService.updatePsychologistSchedule(psychologistSchedule);
            return result > 0 ? AjaxResult.success("修改成功") : AjaxResult.error("修改失败");
        } catch (Exception e) {
            logger.error("修改排班失败", e);
            return AjaxResult.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 保存排班（新增或修改）
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @PostMapping("/save")
    @Log(title = "保存心理咨询师排班", businessType = BusinessType.INSERT)
    public AjaxResult saveSchedule(@Validated @RequestBody PsychologistSchedule psychologistSchedule) {
        try {
            // 设置当前咨询师
            Long psychologistId = SecurityUtils.getUserId();
            String psychologistName = SecurityUtils.getUsername();
            psychologistSchedule.setPsychologistId(psychologistId);
            psychologistSchedule.setPsychologistName(psychologistName);

            // 验证必填字段
            if (psychologistSchedule.getScheduleDate() == null) {
                return AjaxResult.error("请选择排班日期");
            }

            // 设置默认值
            if (psychologistSchedule.getTimeSlots() == null) {
                psychologistSchedule.setTimeSlots("");
            }
            if (psychologistSchedule.getMaxAppointments() == null) {
                psychologistSchedule.setMaxAppointments(5);
            }
            if (psychologistSchedule.getIsAvailable() == null) {
                psychologistSchedule.setIsAvailable("1");
            }

            int result = psychologistScheduleService.saveSchedule(psychologistSchedule);
            return result > 0 ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
        } catch (Exception e) {
            logger.error("保存排班失败", e);
            return AjaxResult.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 批量保存排班
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @PostMapping("/batch")
    @Log(title = "批量保存心理咨询师排班", businessType = BusinessType.INSERT)
    public AjaxResult batchSaveSchedule(@RequestBody List<PsychologistSchedule> scheduleList) {
        try {
            if (scheduleList == null || scheduleList.isEmpty()) {
                return AjaxResult.error("排班数据不能为空");
            }

            // 设置咨询师信息
            Long psychologistId = SecurityUtils.getUserId();
            String psychologistName = SecurityUtils.getUsername();

            for (PsychologistSchedule schedule : scheduleList) {
                schedule.setPsychologistId(psychologistId);
                schedule.setPsychologistName(psychologistName);

                // 设置默认值
                if (schedule.getTimeSlots() == null) {
                    schedule.setTimeSlots("");
                }
                if (schedule.getMaxAppointments() == null) {
                    schedule.setMaxAppointments(5);
                }
                if (schedule.getIsAvailable() == null) {
                    schedule.setIsAvailable("1");
                }
            }

            int result = psychologistScheduleService.batchSaveSchedule(scheduleList);
            return result > 0 ? AjaxResult.success("批量保存成功") : AjaxResult.error("批量保存失败");
        } catch (Exception e) {
            logger.error("批量保存排班失败", e);
            return AjaxResult.error("批量保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除排班
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @DeleteMapping("/{ids}")
    @Log(title = "删除心理咨询师排班", businessType = BusinessType.DELETE)
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            return toAjax(psychologistScheduleService.deletePsychologistScheduleByIds(ids));
        } catch (Exception e) {
            logger.error("删除排班失败", e);
            return AjaxResult.error("删除失败：" + e.getMessage());
        }
    }

    // ==================== 工具接口 ====================

    /**
     * 获取默认时间段选项
     */
    @GetMapping("/time-slots/options")
    public AjaxResult getTimeSlotOptions() {
        List<Map<String, Object>> options = new ArrayList<>();

        String[] slots = {
                "9:00-10:00",
                "10:00-11:00",
                "11:00-12:00",
                "14:00-15:00",
                "15:00-16:00",
                "16:00-17:00",
                "17:00-18:00"
        };

        for (String slot : slots) {
            Map<String, Object> option = new HashMap<>();
            option.put("label", slot);
            option.put("value", slot);
            options.add(option);
        }

        return AjaxResult.success(options);
    }

    /**
     * 更新排班状态
     */
    @PreAuthorize("@ss.hasPermi('consultation:appointment:schedule')")
    @PutMapping("/{id}/status")
    @Log(title = "更新排班状态", businessType = BusinessType.UPDATE)
    public AjaxResult updateStatus(@PathVariable Long id, @RequestParam String isAvailable) {
        try {
            if (!"0".equals(isAvailable) && !"1".equals(isAvailable)) {
                return AjaxResult.error("状态值不正确，应为0或1");
            }

            int result = psychologistScheduleService.updateScheduleStatus(id, isAvailable);
            return result > 0 ? AjaxResult.success("状态更新成功") : AjaxResult.error("状态更新失败");
        } catch (Exception e) {
            logger.error("更新排班状态失败", e);
            return AjaxResult.error("更新状态失败：" + e.getMessage());
        }
    }
}