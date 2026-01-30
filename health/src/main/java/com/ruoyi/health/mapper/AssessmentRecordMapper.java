package com.ruoyi.health.mapper;

import java.util.List;
import com.ruoyi.health.domain.AssessmentRecord;

/**
 * 评估记录Mapper接口
 *
 * @author GGbond
 * @date 2025-12-17
 */
public interface AssessmentRecordMapper
{
    /**
     * 查询评估记录
     *
     * @param id 评估记录主键
     * @return 评估记录
     */
    public AssessmentRecord selectAssessmentRecordById(Long id);

    /**
     * 查询评估记录列表
     *
     * @param assessmentRecord 评估记录
     * @return 评估记录集合
     */
    public List<AssessmentRecord> selectAssessmentRecordList(AssessmentRecord assessmentRecord);

    /**
     * 新增评估记录
     *
     * @param assessmentRecord 评估记录
     * @return 结果
     */
    public int insertAssessmentRecord(AssessmentRecord assessmentRecord);

    /**
     * 修改评估记录
     *
     * @param assessmentRecord 评估记录
     * @return 结果
     */
    public int updateAssessmentRecord(AssessmentRecord assessmentRecord);

    /**
     * 删除评估记录
     *
     * @param id 评估记录主键
     * @return 结果
     */
    public int deleteAssessmentRecordById(Long id);

    /**
     * 批量删除评估记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAssessmentRecordByIds(Long[] ids);
}
