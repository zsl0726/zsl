package com.ruoyi.health.service;

import com.ruoyi.health.domain.PsychologistSchedule;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 心理咨询师排班Service接口
 */
public interface IPsychologistScheduleService {
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
     * 批量删除心理咨询师排班
     */
    int deletePsychologistScheduleByIds(Long[] ids);

    /**
     * 根据咨询师ID和日期查询排班
     */
    PsychologistSchedule selectScheduleByPsychologistAndDate(Long psychologistId, Date scheduleDate);

    /**
     * 根据咨询师ID和日期查询排班（简单版）
     */
    PsychologistSchedule selectScheduleByDate(Long psychologistId, Date scheduleDate);

    /**
     * 获取排班日历数据
     */
    Map<String, Object> getScheduleCalendar(Long psychologistId, String month);

    /**
     * 保存排班（新增或更新）
     */
    int saveSchedule(PsychologistSchedule psychologistSchedule);

    /**
     * 批量保存排班
     */
    int batchSaveSchedule(List<PsychologistSchedule> scheduleList);

    /**
     * 批量新增排班
     */
    int batchInsertPsychologistSchedule(List<PsychologistSchedule> scheduleList);

    /**
     * 更新排班状态
     */
    int updateScheduleStatus(Long id, String isAvailable);
}