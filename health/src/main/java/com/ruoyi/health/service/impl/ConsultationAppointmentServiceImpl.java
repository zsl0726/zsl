package com.ruoyi.health.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.ConsultationAppointment;
import com.ruoyi.health.mapper.ConsultationAppointmentMapper;
import com.ruoyi.health.service.IConsultationAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultationAppointmentServiceImpl implements IConsultationAppointmentService {

    @Autowired
    private ConsultationAppointmentMapper consultationAppointmentMapper;

    @Override
    public ConsultationAppointment selectConsultationAppointmentById(Long id) {
        return consultationAppointmentMapper.selectConsultationAppointmentById(id);
    }

    @Override
    public List<ConsultationAppointment> selectConsultationAppointmentList(ConsultationAppointment consultationAppointment) {
        return consultationAppointmentMapper.selectConsultationAppointmentList(consultationAppointment);
    }

    @Override
    @Transactional
    public int insertConsultationAppointment(ConsultationAppointment consultationAppointment) {
        consultationAppointment.setCreateTime(DateUtils.getNowDate());
        return consultationAppointmentMapper.insertConsultationAppointment(consultationAppointment);
    }

    @Override
    @Transactional
    public int updateConsultationAppointment(ConsultationAppointment consultationAppointment) {
        consultationAppointment.setUpdateTime(DateUtils.getNowDate());
        return consultationAppointmentMapper.updateConsultationAppointment(consultationAppointment);
    }

    @Override
    @Transactional
    public int deleteConsultationAppointmentByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += consultationAppointmentMapper.deleteConsultationAppointmentById(id);
        }
        return count;
    }

    @Override
    public List<ConsultationAppointment> selectUserAppointments(Long userId, String status) {
        return consultationAppointmentMapper.selectUserAppointments(userId, status);
    }

    @Override
    @Transactional
    public int createAppointment(ConsultationAppointment consultationAppointment) {
        Long userId = SecurityUtils.getUserId();
        String userName = SecurityUtils.getUsername();

        consultationAppointment.setUserId(userId);
        consultationAppointment.setUserName(userName);
        consultationAppointment.setStatus("pending");
        consultationAppointment.setCreateTime(DateUtils.getNowDate());

        // 如果咨询师姓名为空，设置为默认值
        if (consultationAppointment.getPsychologistName() == null ||
                consultationAppointment.getPsychologistName().isEmpty()) {
            consultationAppointment.setPsychologistName("心理咨询师");
        }

        return consultationAppointmentMapper.insertConsultationAppointment(consultationAppointment);
    }

    @Override
    @Transactional
    public int cancelAppointment(Long id, Long userId) {
        return consultationAppointmentMapper.cancelAppointment(id, userId);
    }

    @Override
    public List<ConsultationAppointment> selectPsychologistAppointments(Long psychologistId, String status) {
        return consultationAppointmentMapper.selectPsychologistAppointments(psychologistId, status);
    }

    @Override
    @Transactional
    public int acceptAppointment(Long id) {
        return consultationAppointmentMapper.updateAppointmentStatus(id, "accepted", null);
    }

    @Override
    @Transactional
    public int rejectAppointment(Long id) {
        return consultationAppointmentMapper.updateAppointmentStatus(id, "rejected", null);
    }

    @Override
    @Transactional
    public int completeAppointment(Long id, String counselingNotes) {
        return consultationAppointmentMapper.updateAppointmentStatus(id, "completed", counselingNotes);
    }

    @Override
    public Map<String, Object> getAppointmentStatistics(Long psychologistId) {
        List<Map<String, Object>> stats = consultationAppointmentMapper.selectAppointmentStatistics(psychologistId);

        Map<String, Object> result = new HashMap<>();
        if (stats != null && !stats.isEmpty()) {
            Map<String, Object> statData = stats.get(0);
            // 处理数据库查询结果
            result.put("total", statData.get("total") != null ? statData.get("total") : 0);
            result.put("pending_count", statData.get("pending_count") != null ? statData.get("pending_count") : 0);
            result.put("accepted_count", statData.get("accepted_count") != null ? statData.get("accepted_count") : 0);
            result.put("completed_count", statData.get("completed_count") != null ? statData.get("completed_count") : 0);
            result.put("cancelled_count", statData.get("cancelled_count") != null ? statData.get("cancelled_count") : 0);
        } else {
            // 返回默认值
            result.put("total", 0);
            result.put("pending_count", 0);
            result.put("accepted_count", 0);
            result.put("completed_count", 0);
            result.put("cancelled_count", 0);
        }

        return result;
    }

    @Override
    public List<ConsultationAppointment> getTodayAppointments(Long psychologistId) {
        return consultationAppointmentMapper.selectTodayAppointments(psychologistId);
    }

    @Override
    public Map<String, Long> getAppointmentCountByStatus(Long userId, Long psychologistId) {
        List<Map<String, Object>> result = consultationAppointmentMapper.selectAppointmentCountByStatus(userId, psychologistId);
        Map<String, Long> statusCount = new HashMap<>();

        // 初始化所有状态为0
        statusCount.put("pending", 0L);
        statusCount.put("accepted", 0L);
        statusCount.put("completed", 0L);
        statusCount.put("cancelled", 0L);
        statusCount.put("rejected", 0L);

        // 填充实际数据
        if (result != null) {
            for (Map<String, Object> row : result) {
                String status = (String) row.get("status");
                Object countObj = row.get("count");
                Long count = 0L;

                if (countObj != null) {
                    if (countObj instanceof Number) {
                        count = ((Number) countObj).longValue();
                    } else if (countObj instanceof String) {
                        try {
                            count = Long.parseLong((String) countObj);
                        } catch (NumberFormatException e) {
                            count = 0L;
                        }
                    }
                }

                if (status != null) {
                    statusCount.put(status, count);
                }
            }
        }

        return statusCount;
    }

    // ============ 新增方法实现 ============

    @Override
    public List<ConsultationAppointment> selectTodayAppointmentsByPsychologistId(Long psychologistId, String date) {
        return consultationAppointmentMapper.selectTodayAppointmentsByPsychologistId(psychologistId, date);
    }

    @Override
    public List<ConsultationAppointment> selectPendingAppointmentsByPsychologistId(Long psychologistId) {
        return consultationAppointmentMapper.selectPendingAppointmentsByPsychologistId(psychologistId);
    }

    @Override
    public int countTodayAppointments(Long psychologistId) {
        String today = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, new Date());
        return consultationAppointmentMapper.countTodayAppointmentsByPsychologistId(psychologistId, today);
    }

    @Override
    public int countPendingAppointments(Long psychologistId) {
        return consultationAppointmentMapper.countPendingAppointmentsByPsychologistId(psychologistId);
    }

    @Override
    public int countCompletedAppointments(Long psychologistId) {
        return consultationAppointmentMapper.countCompletedAppointmentsByPsychologistId(psychologistId);
    }
}