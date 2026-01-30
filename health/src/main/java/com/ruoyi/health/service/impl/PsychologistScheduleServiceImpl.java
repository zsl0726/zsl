package com.ruoyi.health.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.PsychologistSchedule;
import com.ruoyi.health.mapper.PsychologistScheduleMapper;
import com.ruoyi.health.service.IPsychologistScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PsychologistScheduleServiceImpl implements IPsychologistScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(PsychologistScheduleServiceImpl.class);

    @Autowired
    private PsychologistScheduleMapper psychologistScheduleMapper;

    @Override
    public PsychologistSchedule selectPsychologistScheduleById(Long id) {
        return psychologistScheduleMapper.selectPsychologistScheduleById(id);
    }

    @Override
    public List<PsychologistSchedule> selectPsychologistScheduleList(PsychologistSchedule psychologistSchedule) {
        return psychologistScheduleMapper.selectPsychologistScheduleList(psychologistSchedule);
    }

    @Override
    public int insertPsychologistSchedule(PsychologistSchedule psychologistSchedule) {
        psychologistSchedule.setCreateBy(SecurityUtils.getUsername());
        psychologistSchedule.setCreateTime(DateUtils.getNowDate());
        return psychologistScheduleMapper.insertPsychologistSchedule(psychologistSchedule);
    }

    @Override
    public int updatePsychologistSchedule(PsychologistSchedule psychologistSchedule) {
        psychologistSchedule.setUpdateBy(SecurityUtils.getUsername());
        psychologistSchedule.setUpdateTime(DateUtils.getNowDate());
        return psychologistScheduleMapper.updatePsychologistSchedule(psychologistSchedule);
    }

    @Override
    public int deletePsychologistScheduleByIds(Long[] ids) {
        return psychologistScheduleMapper.deletePsychologistScheduleByIds(ids);
    }

    @Override
    public PsychologistSchedule selectScheduleByPsychologistAndDate(Long psychologistId, Date scheduleDate) {
        return psychologistScheduleMapper.selectScheduleByPsychologistAndDate(psychologistId, scheduleDate);
    }

    @Override
    public PsychologistSchedule selectScheduleByDate(Long psychologistId, Date scheduleDate) {
        return psychologistScheduleMapper.selectScheduleByPsychologistAndDate(psychologistId, scheduleDate);
    }

    @Override
    public Map<String, Object> getScheduleCalendar(Long psychologistId, String month) {
        Map<String, Object> result = new HashMap<>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date monthDate = sdf.parse(month);

            Calendar cal = Calendar.getInstance();
            cal.setTime(monthDate);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date startDate = cal.getTime();

            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date endDate = cal.getTime();

            // 查询该月的所有排班
            List<PsychologistSchedule> schedules = psychologistScheduleMapper.selectScheduleByMonth(
                    psychologistId, startDate, endDate);

            // 转换为按日期索引的Map
            Map<String, PsychologistSchedule> scheduleMap = new HashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (PsychologistSchedule schedule : schedules) {
                String dateStr = dateFormat.format(schedule.getScheduleDate());
                scheduleMap.put(dateStr, schedule);
            }

            result.put("scheduleMap", scheduleMap);
            result.put("month", month);
            result.put("scheduleCount", schedules.size());

        } catch (ParseException e) {
            logger.error("解析月份失败: {}", month, e);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveSchedule(PsychologistSchedule psychologistSchedule) {
        // 设置创建和更新信息
        String username = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();

        if (psychologistSchedule.getId() == null) {
            psychologistSchedule.setCreateBy(username);
            psychologistSchedule.setCreateTime(now);
        }
        psychologistSchedule.setUpdateBy(username);
        psychologistSchedule.setUpdateTime(now);

        // 直接使用 INSERT ON DUPLICATE KEY UPDATE，自动处理重复
        return psychologistScheduleMapper.insertPsychologistSchedule(psychologistSchedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveSchedule(List<PsychologistSchedule> scheduleList) {
        if (scheduleList == null || scheduleList.isEmpty()) {
            return 0;
        }

        String username = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();

        // 设置创建和更新信息
        for (PsychologistSchedule schedule : scheduleList) {
            schedule.setCreateBy(username);
            schedule.setCreateTime(now);
            schedule.setUpdateBy(username);
            schedule.setUpdateTime(now);
        }

        // 直接使用 INSERT ON DUPLICATE KEY UPDATE，无需区分插入和更新
        return psychologistScheduleMapper.batchInsertPsychologistSchedule(scheduleList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertPsychologistSchedule(List<PsychologistSchedule> scheduleList) {
        String username = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();

        for (PsychologistSchedule schedule : scheduleList) {
            schedule.setCreateBy(username);
            schedule.setCreateTime(now);
            schedule.setUpdateBy(username);
            schedule.setUpdateTime(now);
        }

        return psychologistScheduleMapper.batchInsertPsychologistSchedule(scheduleList);
    }

    @Override
    public int updateScheduleStatus(Long id, String isAvailable) {
        PsychologistSchedule schedule = psychologistScheduleMapper.selectPsychologistScheduleById(id);
        if (schedule == null) {
            return 0;
        }

        schedule.setIsAvailable(isAvailable);
        schedule.setUpdateBy(SecurityUtils.getUsername());
        schedule.setUpdateTime(DateUtils.getNowDate());

        return psychologistScheduleMapper.updatePsychologistSchedule(schedule);

        // return psychologistScheduleMapper.updateScheduleStatus(id, isAvailable);
    }
}