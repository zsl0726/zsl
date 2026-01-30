package com.ruoyi.health.service;

import java.util.List;
import com.ruoyi.health.domain.AssessmentScale;

/**
 * 心理评估量Service接口
 * 
 * @author GGbond
 * @date 2025-12-17
 */
public interface IAssessmentScaleService 
{
    /**
     * 查询心理评估量
     * 
     * @param id 心理评估量主键
     * @return 心理评估量
     */
    public AssessmentScale selectAssessmentScaleById(Long id);

    /**
     * 查询心理评估量列表
     * 
     * @param assessmentScale 心理评估量
     * @return 心理评估量集合
     */
    public List<AssessmentScale> selectAssessmentScaleList(AssessmentScale assessmentScale);

    /**
     * 新增心理评估量
     * 
     * @param assessmentScale 心理评估量
     * @return 结果
     */
    public int insertAssessmentScale(AssessmentScale assessmentScale);

    /**
     * 修改心理评估量
     * 
     * @param assessmentScale 心理评估量
     * @return 结果
     */
    public int updateAssessmentScale(AssessmentScale assessmentScale);

    /**
     * 批量删除心理评估量
     * 
     * @param ids 需要删除的心理评估量主键集合
     * @return 结果
     */
    public int deleteAssessmentScaleByIds(Long[] ids);

    /**
     * 删除心理评估量信息
     * 
     * @param id 心理评估量主键
     * @return 结果
     */
    public int deleteAssessmentScaleById(Long id);
}
