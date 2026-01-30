package com.ruoyi.health.service;

import com.ruoyi.health.domain.MentalProfile;
import java.util.List;

/**
 * 心理档案Service接口
 */
public interface IMentalProfileService {
    MentalProfile selectMentalProfileById(Long id);
    MentalProfile selectMentalProfileByUserId(Long userId);
    boolean hasMentalProfile(Long userId);
    List<MentalProfile> selectMentalProfileList(MentalProfile mentalProfile);
    int insertMentalProfile(MentalProfile mentalProfile);
    int updateMentalProfile(MentalProfile mentalProfile);
    int deleteMentalProfileByIds(Long[] ids);
    int deleteMentalProfileById(Long id);

    /**
     * 查询咨询师可见的心理档案列表
     */
    List<MentalProfile> selectVisibleProfileList(MentalProfile mentalProfile);

    /**
     * 根据用户ID查询心理档案
     */
    MentalProfile selectProfileByUserId(Long userId);
}