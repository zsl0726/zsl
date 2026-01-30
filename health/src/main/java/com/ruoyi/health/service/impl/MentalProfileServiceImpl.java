package com.ruoyi.health.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.health.domain.MentalProfile;
import com.ruoyi.health.mapper.MentalProfileMapper;
import com.ruoyi.health.service.IMentalProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 心理档案Service业务层处理
 */
@Service
public class MentalProfileServiceImpl implements IMentalProfileService {
    @Autowired
    private MentalProfileMapper mentalProfileMapper;

    @Override
    public MentalProfile selectMentalProfileById(Long id) {
        return mentalProfileMapper.selectMentalProfileById(id);
    }

    @Override
    public MentalProfile selectMentalProfileByUserId(Long userId) {
        return mentalProfileMapper.selectMentalProfileByUserId(userId);
    }

    @Override
    public boolean hasMentalProfile(Long userId) {
        if (userId == null) {
            return false;
        }
        MentalProfile profile = selectMentalProfileByUserId(userId);
        return profile != null;
    }

    @Override
    public List<MentalProfile> selectMentalProfileList(MentalProfile mentalProfile) {
        return mentalProfileMapper.selectMentalProfileList(mentalProfile);
    }

    @Override
    @Transactional
    public int insertMentalProfile(MentalProfile mentalProfile) {
        Long userId = SecurityUtils.getUserId();
        mentalProfile.setUserId(userId);
        mentalProfile.setCreateBy(SecurityUtils.getUsername());
        mentalProfile.setCreateTime(DateUtils.getNowDate());

        if (mentalProfile.getStatus() == null || mentalProfile.getStatus().isEmpty()) {
            mentalProfile.setStatus("0");
        }

        if (mentalProfile.getIsVisibleToConsultant() == null) {
            mentalProfile.setIsVisibleToConsultant(1);
        }

        String preference = generateServicePreference(mentalProfile);
        mentalProfile.setPreference(preference);

        return mentalProfileMapper.insertMentalProfile(mentalProfile);
    }

    private String generateServicePreference(MentalProfile profile) {
        StringBuilder preference = new StringBuilder();

        if (profile.getStressLevel() != null && profile.getStressLevel() >= 70) {
            preference.append("压力管理课程,");
        }

        if (profile.getEmotionalState() != null && profile.getEmotionalState() <= 2) {
            preference.append("情绪调节训练,");
        }

        if (profile.getSleepQuality() != null && profile.getSleepQuality() <= 2) {
            preference.append("睡眠改善指导,");
        }

        String experience = profile.getTherapyExperience();
        if ("从未有过".equals(experience)) {
            preference.append("新手引导咨询,");
        } else if ("有过但已结束".equals(experience)) {
            preference.append("进阶心理咨询,");
        } else if ("正在接受".equals(experience)) {
            preference.append("持续跟踪咨询,");
        }

        if (preference.length() == 0) {
            preference.append("AI智能疏导,在线咨询");
        } else {
            preference.deleteCharAt(preference.length() - 1);
        }

        return preference.toString();
    }

    @Override
    @Transactional
    public int updateMentalProfile(MentalProfile mentalProfile) {
        mentalProfile.setUpdateBy(SecurityUtils.getUsername());
        mentalProfile.setUpdateTime(new Date());

        if (mentalProfile.getIsVisibleToConsultant() != null) {
            mentalProfile.setVisibilityUpdatedTime(new Date());
        }

        String preference = generateServicePreference(mentalProfile);
        mentalProfile.setPreference(preference);
        return mentalProfileMapper.updateMentalProfile(mentalProfile);
    }

    @Override
    public int deleteMentalProfileByIds(Long[] ids) {
        return mentalProfileMapper.deleteMentalProfileByIds(ids);
    }

    @Override
    public int deleteMentalProfileById(Long id) {
        return mentalProfileMapper.deleteMentalProfileById(id);
    }

    @Override
    public List<MentalProfile> selectVisibleProfileList(MentalProfile mentalProfile) {
        return mentalProfileMapper.selectVisibleProfileList(mentalProfile);
    }

    @Override
    public MentalProfile selectProfileByUserId(Long userId) {
        return mentalProfileMapper.selectProfileByUserId(userId);
    }
}