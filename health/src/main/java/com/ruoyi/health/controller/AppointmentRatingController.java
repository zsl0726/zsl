package com.ruoyi.health.controller;

import java.util.List;

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

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.health.domain.AppointmentRating;
import com.ruoyi.health.service.IAppointmentRatingService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 咨询评价Controller
 */
@RestController
@RequestMapping("/health/consultation/rating")
public class AppointmentRatingController extends BaseController {

    @Autowired
    private IAppointmentRatingService appointmentRatingService;

    /**
     * 查询咨询评价列表
     */
    @PreAuthorize("@ss.hasPermi('health:rating:list')")
    @GetMapping("/list")
    public TableDataInfo list(AppointmentRating appointmentRating) {
        startPage();
        List<AppointmentRating> list = appointmentRatingService.selectAppointmentRatingList(appointmentRating);
        return getDataTable(list);
    }

    /**
     * 导出咨询评价列表
     */
    @PreAuthorize("@ss.hasPermi('health:rating:export')")
    @Log(title = "咨询评价", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(AppointmentRating appointmentRating) {
        List<AppointmentRating> list = appointmentRatingService.selectAppointmentRatingList(appointmentRating);
        ExcelUtil<AppointmentRating> util = new ExcelUtil<>(AppointmentRating.class);
        return util.exportExcel(list, "咨询评价数据");
    }

    /**
     * 获取咨询评价详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:rating:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(appointmentRatingService.selectAppointmentRatingById(id));
    }

    /**
     * 新增咨询评价
     */
    @PreAuthorize("@ss.hasPermi('health:rating:add')")
    @Log(title = "咨询评价", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AppointmentRating appointmentRating) {
        return toAjax(appointmentRatingService.insertAppointmentRating(appointmentRating));
    }

    /**
     * 修改咨询评价
     */
    @PreAuthorize("@ss.hasPermi('health:rating:edit')")
    @Log(title = "咨询评价", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AppointmentRating appointmentRating) {
        return toAjax(appointmentRatingService.updateAppointmentRating(appointmentRating));
    }

    /**
     * 删除咨询评价
     */
    @PreAuthorize("@ss.hasPermi('health:rating:remove')")
    @Log(title = "咨询评价", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(appointmentRatingService.deleteAppointmentRatingByIds(ids));
    }

    /**
     * 提交评价（用户端接口）
     */
    @Log(title = "提交评价", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submitRating(@RequestBody AppointmentRating rating) {
        return appointmentRatingService.submitRating(rating);
    }

    /**
     * 根据预约ID查询评价
     */
    @GetMapping("/appointment/{appointmentId}")
    public AjaxResult getByAppointmentId(@PathVariable("appointmentId") Long appointmentId) {
        AppointmentRating rating = appointmentRatingService.selectByAppointmentId(appointmentId);
        return AjaxResult.success(rating);
    }

    /**
     * 获取咨询师评分统计信息
     */
    @GetMapping("/psychologist/stats/{psychologistId}")
    public AjaxResult getPsychologistStats(@PathVariable("psychologistId") Long psychologistId) {
        return AjaxResult.success(appointmentRatingService.getPsychologistRatingInfo(psychologistId));
    }

    /**
     * 获取当前咨询师的评分统计信息
     */
    @GetMapping("/psychologist/myStats")
    public AjaxResult getMyStats() {
        Long psychologistId = getUserId();
        return AjaxResult.success(appointmentRatingService.getPsychologistRatingInfo(psychologistId));
    }

    /**
     * 获取咨询师的所有评价列表
     */
    @GetMapping("/psychologist/list/{psychologistId}")
    public TableDataInfo getByPsychologistId(@PathVariable("psychologistId") Long psychologistId) {
        startPage();
        List<AppointmentRating> list = appointmentRatingService.selectByPsychologistId(psychologistId);
        return getDataTable(list);
    }
}