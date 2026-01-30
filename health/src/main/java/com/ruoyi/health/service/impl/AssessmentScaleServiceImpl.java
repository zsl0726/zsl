package com.ruoyi.health.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.health.mapper.AssessmentScaleMapper;
import com.ruoyi.health.domain.AssessmentScale;
import com.ruoyi.health.service.IAssessmentScaleService;

/**
 * 心理评估量Service业务层处理
 * 
 * @author GGbond
 * @date 2025-12-17
 */
@Service
public class AssessmentScaleServiceImpl implements IAssessmentScaleService 
{
    @Autowired
    private AssessmentScaleMapper assessmentScaleMapper;

    /**
     * 查询心理评估量
     * 
     * @param id 心理评估量主键
     * @return 心理评估量
     */
    @Override
    public AssessmentScale selectAssessmentScaleById(Long id)
    {
        return assessmentScaleMapper.selectAssessmentScaleById(id);
    }

    /**
     * 查询心理评估量列表
     * 
     * @param assessmentScale 心理评估量
     * @return 心理评估量
     */
    @Override
    public List<AssessmentScale> selectAssessmentScaleList(AssessmentScale assessmentScale)
    {
        return assessmentScaleMapper.selectAssessmentScaleList(assessmentScale);
    }

    /**
     * 新增心理评估量
     * 
     * @param assessmentScale 心理评估量
     * @return 结果
     */
    @Override
    public int insertAssessmentScale(AssessmentScale assessmentScale)
    {
        assessmentScale.setCreateTime(DateUtils.getNowDate());
        return assessmentScaleMapper.insertAssessmentScale(assessmentScale);
    }

    /**
     * 修改心理评估量
     * 
     * @param assessmentScale 心理评估量
     * @return 结果
     */
    @Override
    public int updateAssessmentScale(AssessmentScale assessmentScale)
    {
        assessmentScale.setUpdateTime(DateUtils.getNowDate());
        return assessmentScaleMapper.updateAssessmentScale(assessmentScale);
    }

    /**
     * 批量删除心理评估量
     * 
     * @param ids 需要删除的心理评估量主键
     * @return 结果
     */
    @Override
    public int deleteAssessmentScaleByIds(Long[] ids)
    {
        return assessmentScaleMapper.deleteAssessmentScaleByIds(ids);
    }

    /**
     * 删除心理评估量信息
     * 
     * @param id 心理评估量主键
     * @return 结果
     */
    @Override
    public int deleteAssessmentScaleById(Long id)
    {
        return assessmentScaleMapper.deleteAssessmentScaleById(id);
    }
}
