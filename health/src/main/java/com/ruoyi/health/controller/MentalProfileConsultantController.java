package com.ruoyi.health.controller;

import java.util.List;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.health.domain.MentalProfile;
import com.ruoyi.health.domain.ProfileConsultantNote;
import com.ruoyi.health.service.IMentalProfileService;
import com.ruoyi.health.service.IProfileConsultantNoteService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 咨询师心理档案管理Controller
 */
@RestController
@RequestMapping("/health/profile")
public class MentalProfileConsultantController extends BaseController {

    @Autowired
    private IMentalProfileService mentalProfileService;

    @Autowired
    private IProfileConsultantNoteService profileConsultantNoteService;

    /**
     * 查询咨询师可见的心理档案列表
     */
    @GetMapping("/consultant/list")
    public TableDataInfo list(MentalProfile mentalProfile) {
        // 权限检查
        if (!checkConsultantPermission()) {
            TableDataInfo rspData = new TableDataInfo();
            rspData.setCode(403);
            rspData.setMsg("您不是咨询师或管理员，无权访问此页面");
            return rspData;
        }

        startPage();
        List<MentalProfile> list = mentalProfileService.selectVisibleProfileList(mentalProfile);
        return getDataTable(list);
    }

    /**
     * 获取咨询师可见的心理档案详细信息
     */
    @GetMapping(value = "/consultant/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId) {
        // 权限检查
        if (!checkConsultantPermission()) {
            return AjaxResult.error("您不是咨询师或管理员，无权访问此页面");
        }

        MentalProfile mentalProfile = mentalProfileService.selectProfileByUserId(userId);

        if (mentalProfile == null) {
            return AjaxResult.error("用户尚未填写心理档案");
        }

        // 管理员可以查看所有档案
        if (SecurityUtils.isAdmin()) {
            return AjaxResult.success(mentalProfile);
        }

        // 咨询师只能查看可见的档案
        Integer visibleStatus = mentalProfile.getIsVisibleToConsultant();
        if (visibleStatus == null || visibleStatus == 1) {
            return AjaxResult.success(mentalProfile);
        } else {
            return AjaxResult.error("该用户心理档案对咨询师不可见");
        }
    }

    /**
     * 获取用户的备注列表
     */
    @GetMapping("/note/{userId}")
    public AjaxResult getNotes(@PathVariable("userId") Long userId) {
        // 权限检查
        if (!checkConsultantPermission()) {
            return AjaxResult.error("您不是咨询师或管理员，无权访问此页面");
        }

        List<ProfileConsultantNote> notes = profileConsultantNoteService.selectNotesByUserId(userId);
        return AjaxResult.success(notes);
    }

    /**
     * 新增咨询师备注
     */
    @Log(title = "咨询师档案备注", businessType = BusinessType.INSERT)
    @PostMapping("/note")
    public AjaxResult addNote(@RequestBody ProfileConsultantNote profileConsultantNote) {
        // 权限检查
        if (!checkConsultantPermission()) {
            return AjaxResult.error("您不是咨询师或管理员，无权访问此页面");
        }

        // 设置咨询师信息
        Long consultantId = SecurityUtils.getUserId();
        String consultantName = SecurityUtils.getUsername();

        profileConsultantNote.setConsultantId(consultantId);
        profileConsultantNote.setConsultantName(consultantName);

        // 根据userId查找档案ID
        MentalProfile profile = mentalProfileService.selectProfileByUserId(profileConsultantNote.getUserId());
        if (profile != null) {
            profileConsultantNote.setProfileId(profile.getId());
        }

        int result = profileConsultantNoteService.insertProfileConsultantNote(profileConsultantNote);
        return toAjax(result);
    }

    /**
     * 权限检查方法
     */
    private boolean checkConsultantPermission() {
        try {
            return SecurityUtils.canAccessConsultant();
        } catch (Exception e) {
            logger.error("权限检查失败", e);
            return false;
        }
    }
}