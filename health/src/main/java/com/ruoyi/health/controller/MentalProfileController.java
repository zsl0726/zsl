package com.ruoyi.health.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.MentalProfile;
import com.ruoyi.health.service.IMentalProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心理档案Controller
 */
@RestController
@RequestMapping("/health/profile")
public class MentalProfileController extends BaseController {
    @Autowired
    private IMentalProfileService mentalProfileService;

    /**
     * 查询我的心理档案
     */
    @GetMapping("/my")
    public AjaxResult getMyProfile() {
        Long userId = SecurityUtils.getUserId();
        MentalProfile profile = mentalProfileService.selectMentalProfileByUserId(userId);
        if (profile == null) {
            return AjaxResult.success(null);
        }
        return AjaxResult.success(profile);
    }

    /**
     * 检查是否需要填写心理档案
     */
    @GetMapping("/check")
    public AjaxResult checkProfile() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return AjaxResult.success(false);
        }
        boolean hasProfile = mentalProfileService.hasMentalProfile(userId);
        return AjaxResult.success(hasProfile);
    }

    /**
     * 创建我的心理档案
     */
    @PostMapping("/my")
    public AjaxResult createMyProfile(@RequestBody MentalProfile mentalProfile) {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return AjaxResult.error("未获取到用户信息，请重新登录");
            }
            if (mentalProfileService.hasMentalProfile(userId)) {
                return AjaxResult.error("您已经填写过心理档案");
            }

            if (mentalProfile.getMainConcernsArray() != null && mentalProfile.getMainConcernsArray().length > 0) {
                mentalProfile.setMainConcerns(String.join(",", mentalProfile.getMainConcernsArray()));
            }

            int result = mentalProfileService.insertMentalProfile(mentalProfile);
            if (result > 0) {
                return AjaxResult.success("心理档案创建成功");
            } else {
                return AjaxResult.error("心理档案创建失败");
            }
        } catch (Exception e) {
            return AjaxResult.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 更新我的心理档案
     */
    @PutMapping("/my")
    public AjaxResult updateMyProfile(@RequestBody MentalProfile mentalProfile) {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return AjaxResult.error("未获取到用户信息，请重新登录");
            }
            MentalProfile existingProfile = mentalProfileService.selectMentalProfileByUserId(userId);

            if (existingProfile == null) {
                return AjaxResult.error("未找到您的心理档案");
            }

            if (mentalProfile.getMainConcernsArray() != null && mentalProfile.getMainConcernsArray().length > 0) {
                mentalProfile.setMainConcerns(String.join(",", mentalProfile.getMainConcernsArray()));
            }

            mentalProfile.setId(existingProfile.getId());
            mentalProfile.setUserId(userId);

            int result = mentalProfileService.updateMentalProfile(mentalProfile);
            if (result > 0) {
                return AjaxResult.success("心理档案更新成功");
            } else {
                return AjaxResult.error("心理档案更新失败");
            }
        } catch (Exception e) {
            return AjaxResult.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 查询心理档案列表（管理员）
     */
    @GetMapping("/list")
    public TableDataInfo list(MentalProfile mentalProfile) {
        startPage();
        List<MentalProfile> list = mentalProfileService.selectMentalProfileList(mentalProfile);
        return getDataTable(list);
    }

    /**
     * 获取心理档案详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(mentalProfileService.selectMentalProfileById(id));
    }

    /**
     * 新增心理档案（管理员）
     */
    @PostMapping
    public AjaxResult add(@RequestBody MentalProfile mentalProfile) {
        try {
            int result = mentalProfileService.insertMentalProfile(mentalProfile);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改心理档案（管理员）
     */
    @PutMapping
    public AjaxResult edit(@RequestBody MentalProfile mentalProfile) {
        try {
            int result = mentalProfileService.updateMentalProfile(mentalProfile);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除心理档案（管理员）
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            int result = mentalProfileService.deleteMentalProfileByIds(ids);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("删除失败：" + e.getMessage());
        }
    }
}