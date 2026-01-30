package com.ruoyi.health.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.health.domain.ConsultationLocation;
import com.ruoyi.health.service.IConsultationLocationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 咨询地点Controller
 *
 * @author GGbond
 * @date 2025-12-19
 */
@RestController
@RequestMapping("/health/location")
public class ConsultationLocationController extends BaseController
{
    @Autowired
    private IConsultationLocationService consultationLocationService;

    /**
     * 查询咨询地点列表
     */
    @PreAuthorize("@ss.hasPermi('health:location:list')")
    @GetMapping("/list")
    public TableDataInfo list(ConsultationLocation consultationLocation)
    {
        startPage();
        List<ConsultationLocation> list = consultationLocationService.selectConsultationLocationList(consultationLocation);
        return getDataTable(list);
    }

    /**
     * 导出咨询地点列表
     */
    @PreAuthorize("@ss.hasPermi('health:location:export')")
    @Log(title = "咨询地点", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ConsultationLocation consultationLocation)
    {
        List<ConsultationLocation> list = consultationLocationService.selectConsultationLocationList(consultationLocation);
        ExcelUtil<ConsultationLocation> util = new ExcelUtil<ConsultationLocation>(ConsultationLocation.class);
        util.exportExcel(response, list, "咨询地点数据");
    }

    /**
     * 获取咨询地点详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:location:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(consultationLocationService.selectConsultationLocationById(id));
    }

    /**
     * 新增咨询地点
     */
    @PreAuthorize("@ss.hasPermi('health:location:add')")
    @Log(title = "咨询地点", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ConsultationLocation consultationLocation)
    {
        return toAjax(consultationLocationService.insertConsultationLocation(consultationLocation));
    }

    /**
     * 修改咨询地点
     */
    @PreAuthorize("@ss.hasPermi('health:location:edit')")
    @Log(title = "咨询地点", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ConsultationLocation consultationLocation)
    {
        return toAjax(consultationLocationService.updateConsultationLocation(consultationLocation));
    }

    /**
     * 删除咨询地点
     */
    @PreAuthorize("@ss.hasPermi('health:location:remove')")
    @Log(title = "咨询地点", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(consultationLocationService.deleteConsultationLocationByIds(ids));
    }
}
