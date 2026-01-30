package com.ruoyi.health.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.health.domain.AppointmentRating;

public interface AppointmentRatingMapper {

    // 基础CRUD方法
    AppointmentRating selectAppointmentRatingById(Long id);
    List<AppointmentRating> selectAppointmentRatingList(AppointmentRating appointmentRating);
    int insertAppointmentRating(AppointmentRating appointmentRating);
    int updateAppointmentRating(AppointmentRating appointmentRating);
    int deleteAppointmentRatingById(Long id);
    int deleteAppointmentRatingByIds(Long[] ids);

    // 自定义查询方法
    AppointmentRating selectByAppointmentId(Long appointmentId);
    BigDecimal selectAvgRatingByPsychologistId(Long psychologistId);
    int selectRatingCountByPsychologistId(Long psychologistId);
    List<AppointmentRating> selectByPsychologistId(Long psychologistId);
}