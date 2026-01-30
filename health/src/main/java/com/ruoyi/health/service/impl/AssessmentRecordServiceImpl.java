package com.ruoyi.health.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.health.mapper.AssessmentRecordMapper;
import com.ruoyi.health.domain.AssessmentRecord;
import com.ruoyi.health.service.IAssessmentRecordService;

/**
 * 评估记录Service业务层处理
 *
 * @author GGbond
 * @date 2025-12-17
 */
@Service
public class AssessmentRecordServiceImpl implements IAssessmentRecordService
{
    @Autowired
    private AssessmentRecordMapper assessmentRecordMapper;

    /**
     * 查询评估记录
     *
     * @param id 评估记录主键
     * @return 评估记录
     */
    @Override
    public AssessmentRecord selectAssessmentRecordById(Long id)
    {
        return assessmentRecordMapper.selectAssessmentRecordById(id);
    }

    /**
     * 查询评估记录列表
     *
     * @param assessmentRecord 评估记录
     * @return 评估记录集合
     */
    @Override
    public List<AssessmentRecord> selectAssessmentRecordList(AssessmentRecord assessmentRecord)
    {
        return assessmentRecordMapper.selectAssessmentRecordList(assessmentRecord);
    }

    /**
     * 新增评估记录
     *
     * @param assessmentRecord 评估记录
     * @return 结果
     */
    @Override
    public int insertAssessmentRecord(AssessmentRecord assessmentRecord)
    {
        return assessmentRecordMapper.insertAssessmentRecord(assessmentRecord);
    }

    /**
     * 修改评估记录
     *
     * @param assessmentRecord 评估记录
     * @return 结果
     */
    @Override
    public int updateAssessmentRecord(AssessmentRecord assessmentRecord)
    {
        return assessmentRecordMapper.updateAssessmentRecord(assessmentRecord);
    }

    /**
     * 批量删除评估记录
     *
     * @param ids 需要删除的评估记录主键集合
     * @return 结果
     */
    @Override
    public int deleteAssessmentRecordByIds(Long[] ids)
    {
        return assessmentRecordMapper.deleteAssessmentRecordByIds(ids);
    }

    /**
     * 删除评估记录信息
     *
     * @param id 评估记录主键
     * @return 结果
     */
    @Override
    public int deleteAssessmentRecordById(Long id)
    {
        return assessmentRecordMapper.deleteAssessmentRecordById(id);
    }
}