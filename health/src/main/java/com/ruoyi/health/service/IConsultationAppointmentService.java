package com.ruoyi.health.service;

import com.ruoyi.health.domain.ConsultationAppointment;
import java.util.List;
import java.util.Map;

public interface IConsultationAppointmentService {
    // 基础CRUD
    ConsultationAppointment selectConsultationAppointmentById(Long id);
    List<ConsultationAppointment> selectConsultationAppointmentList(ConsultationAppointment consultationAppointment);
    int insertConsultationAppointment(ConsultationAppointment consultationAppointment);
    int updateConsultationAppointment(ConsultationAppointment consultationAppointment);
    int deleteConsultationAppointmentByIds(Long[] ids);

    // 用户功能
    List<ConsultationAppointment> selectUserAppointments(Long userId, String status);
    int createAppointment(ConsultationAppointment consultationAppointment);
    int cancelAppointment(Long id, Long userId);

    // 咨询师功能
    List<ConsultationAppointment> selectPsychologistAppointments(Long psychologistId, String status);
    int acceptAppointment(Long id);
    int rejectAppointment(Long id);
    int completeAppointment(Long id, String counselingNotes);
    Map<String, Object> getAppointmentStatistics(Long psychologistId);
    List<ConsultationAppointment> getTodayAppointments(Long psychologistId);

    // 通用统计
    Map<String, Long> getAppointmentCountByStatus(Long userId, Long psychologistId);

    // ============ 新增：咨询师首页功能 ============

    /**
     * 查询咨询师今日预约（支持指定日期）
     */
    List<ConsultationAppointment> selectTodayAppointmentsByPsychologistId(Long psychologistId, String date);

    /**
     * 查询咨询师待处理预约
     */
    List<ConsultationAppointment> selectPendingAppointmentsByPsychologistId(Long psychologistId);

    /**
     * 统计咨询师今日预约数量
     */
    int countTodayAppointments(Long psychologistId);

    /**
     * 统计咨询师待处理预约数量
     */
    int countPendingAppointments(Long psychologistId);

    /**
     * 统计咨询师已完成咨询数量
     */
    int countCompletedAppointments(Long psychologistId);
}