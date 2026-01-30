package com.ruoyi.health.mapper;

import com.ruoyi.health.domain.PsychologistSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 心理咨询师排班Mapper接口
 */
public interface PsychologistScheduleMapper {
    /**
     * 查询心理咨询师排班
     */
    PsychologistSchedule selectPsychologistScheduleById(Long id);

    /**
     * 查询心理咨询师排班列表
     */
    List<PsychologistSchedule> selectPsychologistScheduleList(PsychologistSchedule psychologistSchedule);

    /**
     * 新增心理咨询师排班
     */
    int insertPsychologistSchedule(PsychologistSchedule psychologistSchedule);

    /**
     * 修改心理咨询师排班
     */
    int updatePsychologistSchedule(PsychologistSchedule psychologistSchedule);

    /**
     * 删除心理咨询师排班
     */
    int deletePsychologistScheduleById(Long id);

    /**
     * 批量删除心理咨询师排班
     */
    int deletePsychologistScheduleByIds(Long[] ids);

    /**
     * 根据咨询师ID和日期查询排班
     */
    PsychologistSchedule selectScheduleByPsychologistAndDate(@Param("psychologistId") Long psychologistId,
                                                             @Param("scheduleDate") Date scheduleDate);

    /**
     * 根据咨询师ID查询指定月份的排班
     */
    List<PsychologistSchedule> selectScheduleByMonth(@Param("psychologistId") Long psychologistId,
                                                     @Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    /**
     * 批量插入排班
     */
    int batchInsertPsychologistSchedule(List<PsychologistSchedule> scheduleList);

//    /**
//     * 更新排班状态
//     */
//    int updateScheduleStatus(@Param("id") Long id, @Param("isAvailable") String isAvailable);
}