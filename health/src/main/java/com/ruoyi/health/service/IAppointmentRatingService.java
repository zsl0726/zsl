package com.ruoyi.health.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.health.domain.AppointmentRating;

public interface IAppointmentRatingService {

    // 基础CRUD
    AppointmentRating selectAppointmentRatingById(Long id);
    List<AppointmentRating> selectAppointmentRatingList(AppointmentRating appointmentRating);
    int insertAppointmentRating(AppointmentRating appointmentRating);
    int updateAppointmentRating(AppointmentRating appointmentRating);
    int deleteAppointmentRatingByIds(Long[] ids);
    int deleteAppointmentRatingById(Long id);

    // 评价功能
    AjaxResult submitRating(AppointmentRating rating);
    AppointmentRating selectByAppointmentId(Long appointmentId);
    List<AppointmentRating> selectByPsychologistId(Long psychologistId);
    Map<String, Object> getPsychologistRatingInfo(Long psychologistId);
}