package com.ruoyi.health.mapper;

import com.ruoyi.health.domain.ConsultationAppointment;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface ConsultationAppointmentMapper {
    // 基础CRUD方法
    ConsultationAppointment selectConsultationAppointmentById(Long id);
    List<ConsultationAppointment> selectConsultationAppointmentList(ConsultationAppointment consultationAppointment);
    int insertConsultationAppointment(ConsultationAppointment consultationAppointment);
    int updateConsultationAppointment(ConsultationAppointment consultationAppointment);
    int deleteConsultationAppointmentById(Long id);
    int deleteConsultationAppointmentByIds(Long[] ids);

    // 业务方法
    List<ConsultationAppointment> selectUserAppointments(@Param("userId") Long userId,
                                                         @Param("status") String status);

    List<ConsultationAppointment> selectPsychologistAppointments(@Param("psychologistId") Long psychologistId,
                                                                 @Param("status") String status);

    int updateAppointmentStatus(@Param("id") Long id,
                                @Param("status") String status,
                                @Param("counselingNotes") String counselingNotes);

    int cancelAppointment(@Param("id") Long id,
                          @Param("userId") Long userId);

    List<Map<String, Object>> selectAppointmentStatistics(@Param("psychologistId") Long psychologistId);

    List<ConsultationAppointment> selectTodayAppointments(@Param("psychologistId") Long psychologistId);

    List<Map<String, Object>> selectAppointmentCountByStatus(@Param("userId") Long userId,
                                                             @Param("psychologistId") Long psychologistId);

    // ============ 新增：咨询师首页方法 ============

    /**
     * 查询咨询师今日预约（支持指定日期）
     */
    List<ConsultationAppointment> selectTodayAppointmentsByPsychologistId(
            @Param("psychologistId") Long psychologistId,
            @Param("date") String date
    );

    /**
     * 查询咨询师待处理预约
     */
    List<ConsultationAppointment> selectPendingAppointmentsByPsychologistId(
            @Param("psychologistId") Long psychologistId
    );

    /**
     * 统计咨询师今日预约数量
     */
    int countTodayAppointmentsByPsychologistId(
            @Param("psychologistId") Long psychologistId,
            @Param("date") String date
    );

    /**
     * 统计咨询师待处理预约数量
     */
    int countPendingAppointmentsByPsychologistId(
            @Param("psychologistId") Long psychologistId
    );

    /**
     * 统计咨询师已完成咨询数量
     */
    int countCompletedAppointmentsByPsychologistId(
            @Param("psychologistId") Long psychologistId
    );


}