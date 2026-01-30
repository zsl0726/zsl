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
import com.ruoyi.health.domain.AssessmentScale;
import com.ruoyi.health.service.IAssessmentScaleService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 心理评估量Controller
 * 
 * @author GGbond
 * @date 2025-12-17
 */
@RestController
@RequestMapping("/health/scale")
public class AssessmentScaleController extends BaseController
{
    @Autowired
    private IAssessmentScaleService assessmentScaleService;

    /**
     * 查询心理评估量列表
     */
    @PreAuthorize("@ss.hasPermi('health:scale:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessmentScale assessmentScale)
    {
        startPage();
        List<AssessmentScale> list = assessmentScaleService.selectAssessmentScaleList(assessmentScale);
        return getDataTable(list);
    }

    /**
     * 导出心理评估量列表
     */
    @PreAuthorize("@ss.hasPermi('health:scale:export')")
    @Log(title = "心理评估量", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssessmentScale assessmentScale)
    {
        List<AssessmentScale> list = assessmentScaleService.selectAssessmentScaleList(assessmentScale);
        ExcelUtil<AssessmentScale> util = new ExcelUtil<AssessmentScale>(AssessmentScale.class);
        util.exportExcel(response, list, "心理评估量数据");
    }

    /**
     * 获取心理评估量详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:scale:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(assessmentScaleService.selectAssessmentScaleById(id));
    }

    /**
     * 新增心理评估量
     */
    @PreAuthorize("@ss.hasPermi('health:scale:add')")
    @Log(title = "心理评估量", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssessmentScale assessmentScale)
    {
        return toAjax(assessmentScaleService.insertAssessmentScale(assessmentScale));
    }

    /**
     * 修改心理评估量
     */
    @PreAuthorize("@ss.hasPermi('health:scale:edit')")
    @Log(title = "心理评估量", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssessmentScale assessmentScale)
    {
        return toAjax(assessmentScaleService.updateAssessmentScale(assessmentScale));
    }

    /**
     * 删除心理评估量
     */
    @PreAuthorize("@ss.hasPermi('health:scale:remove')")
    @Log(title = "心理评估量", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(assessmentScaleService.deleteAssessmentScaleByIds(ids));
    }
}
