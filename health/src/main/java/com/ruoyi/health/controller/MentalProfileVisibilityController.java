package com.ruoyi.health.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.MentalProfile;
import com.ruoyi.health.service.IMentalProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 心理档案可见性控制器
 */
@RestController
@RequestMapping("/health/profile")
public class MentalProfileVisibilityController extends BaseController {

    @Autowired
    private IMentalProfileService mentalProfileService;

    /**
     * 获取当前用户的档案可见性设置
     */
    @GetMapping("/visibility")
    public AjaxResult getVisibility() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return AjaxResult.error("未获取到用户信息，请重新登录");
        }

        MentalProfile profile = mentalProfileService.selectMentalProfileByUserId(userId);
        if (profile == null) {
            return AjaxResult.success(false);
        }

        Boolean isVisible = profile.getIsVisibleToConsultant() != null
                ? profile.getIsVisibleToConsultant() == 1
                : true;

        return AjaxResult.success(isVisible);
    }

    /**
     * 更新当前用户的档案可见性设置
     */
    @PutMapping("/visibility")
    public AjaxResult updateVisibility(@RequestBody UpdateVisibilityRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return AjaxResult.error("未获取到用户信息，请重新登录");
        }

        MentalProfile profile = mentalProfileService.selectMentalProfileByUserId(userId);
        if (profile == null) {
            return AjaxResult.error("用户尚未填写心理档案");
        }

        // 更新可见性
        profile.setIsVisibleToConsultant(request.getVisible() ? 1 : 0);
        int result = mentalProfileService.updateMentalProfile(profile);

        return toAjax(result);
    }

    /**
     * 可见性更新请求对象
     */
    public static class UpdateVisibilityRequest {
        private Boolean visible;

        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }
    }
}