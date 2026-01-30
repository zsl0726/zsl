package com.ruoyi.health.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.AppointmentRating;
import com.ruoyi.health.domain.ConsultationAppointment;
import com.ruoyi.health.mapper.AppointmentRatingMapper;
import com.ruoyi.health.mapper.ConsultationAppointmentMapper;
import com.ruoyi.health.service.IAppointmentRatingService;

@Service
public class AppointmentRatingServiceImpl implements IAppointmentRatingService {

    @Autowired
    private AppointmentRatingMapper appointmentRatingMapper;

    @Autowired
    private ConsultationAppointmentMapper consultationAppointmentMapper;

    @Override
    public AppointmentRating selectAppointmentRatingById(Long id) {
        return appointmentRatingMapper.selectAppointmentRatingById(id);
    }

    @Override
    public List<AppointmentRating> selectAppointmentRatingList(AppointmentRating appointmentRating) {
        return appointmentRatingMapper.selectAppointmentRatingList(appointmentRating);
    }

    @Override
    public int insertAppointmentRating(AppointmentRating appointmentRating) {
        appointmentRating.setCreateTime(new Date());
        appointmentRating.setDelFlag("0");
        return appointmentRatingMapper.insertAppointmentRating(appointmentRating);
    }

    @Override
    public int updateAppointmentRating(AppointmentRating appointmentRating) {
        appointmentRating.setUpdateTime(new Date());
        return appointmentRatingMapper.updateAppointmentRating(appointmentRating);
    }

    @Override
    public int deleteAppointmentRatingByIds(Long[] ids) {
        return appointmentRatingMapper.deleteAppointmentRatingByIds(ids);
    }

    @Override
    public int deleteAppointmentRatingById(Long id) {
        return appointmentRatingMapper.deleteAppointmentRatingById(id);
    }

    @Override
    @Transactional
    public AjaxResult submitRating(AppointmentRating rating) {
        try {
            // 验证预约是否存在且已完成
            ConsultationAppointment appointment = consultationAppointmentMapper.selectConsultationAppointmentById(rating.getAppointmentId());
            if (appointment == null) {
                return AjaxResult.error("预约不存在");
            }

            if (!"completed".equals(appointment.getStatus())) {
                return AjaxResult.error("只有已完成的预约才能评价");
            }

            // 验证是否已经评价过
            AppointmentRating existing = selectByAppointmentId(rating.getAppointmentId());
            if (existing != null) {
                return AjaxResult.error("该预约已经评价过了");
            }

            // 验证评分范围
            if (rating.getRatingScore() == null ||
                    rating.getRatingScore().compareTo(BigDecimal.ONE) < 0 ||
                    rating.getRatingScore().compareTo(new BigDecimal("5")) > 0) {
                return AjaxResult.error("评分必须在1-5分之间");
            }

            // 设置评价信息
            rating.setUserId(SecurityUtils.getUserId());
            rating.setPsychologistId(appointment.getPsychologistId());
            rating.setCreateTime(new Date());
            rating.setDelFlag("0");

            // 插入评价记录
            int result = appointmentRatingMapper.insertAppointmentRating(rating);

            if (result > 0) {
                // 更新预约表的评分信息
                ConsultationAppointment updateAppointment = new ConsultationAppointment();
                updateAppointment.setId(rating.getAppointmentId());
                updateAppointment.setRatingId(rating.getId());
                updateAppointment.setRatingScore(rating.getRatingScore());
                consultationAppointmentMapper.updateConsultationAppointment(updateAppointment);

                return AjaxResult.success("评价成功");
            }

            return AjaxResult.error("评价失败");
        } catch (Exception e) {
            return AjaxResult.error("评价失败：" + e.getMessage());
        }
    }

    @Override
    public AppointmentRating selectByAppointmentId(Long appointmentId) {
        return appointmentRatingMapper.selectByAppointmentId(appointmentId);
    }

    @Override
    public List<AppointmentRating> selectByPsychologistId(Long psychologistId) {
        return appointmentRatingMapper.selectByPsychologistId(psychologistId);
    }

    @Override
    public Map<String, Object> getPsychologistRatingInfo(Long psychologistId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取平均评分
            BigDecimal avgRating = appointmentRatingMapper.selectAvgRatingByPsychologistId(psychologistId);
            if (avgRating != null) {
                avgRating = avgRating.setScale(1, RoundingMode.HALF_UP);
                result.put("averageRating", avgRating.doubleValue());
            } else {
                result.put("averageRating", 0.0);
            }

            // 获取评价数量
            int ratingCount = appointmentRatingMapper.selectRatingCountByPsychologistId(psychologistId);
            result.put("totalRatings", ratingCount);
            result.put("psychologistId", psychologistId);

        } catch (Exception e) {
            result.put("averageRating", 0.0);
            result.put("totalRatings", 0);
        }

        return result;
    }
}