package com.ruoyi.health.mapper;

import com.ruoyi.health.domain.MentalProfile;
import java.util.List;

/**
 * 心理档案Mapper接口
 */
public interface MentalProfileMapper {
    MentalProfile selectMentalProfileById(Long id);
    MentalProfile selectMentalProfileByUserId(Long userId);
    List<MentalProfile> selectMentalProfileList(MentalProfile mentalProfile);
    int insertMentalProfile(MentalProfile mentalProfile);
    int updateMentalProfile(MentalProfile mentalProfile);
    int deleteMentalProfileById(Long id);
    int deleteMentalProfileByIds(Long[] ids);
    /**
     * 查询咨询师可见的心理档案列表
     *
     * @param mentalProfile 心理档案
     * @return 心理档案集合
     */
    List<MentalProfile> selectVisibleProfileList(MentalProfile mentalProfile);

    /**
     * 根据用户ID查询心理档案
     *
     * @param userId 用户ID
     * @return 心理档案信息
     */
    MentalProfile selectProfileByUserId(Long userId);
}