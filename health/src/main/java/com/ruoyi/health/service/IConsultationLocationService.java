package com.ruoyi.health.service;

import java.util.List;
import com.ruoyi.health.domain.ConsultationLocation;

/**
 * 咨询地点Service接口
 * 
 * @author GGbond
 * @date 2025-12-19
 */
public interface IConsultationLocationService 
{
    /**
     * 查询咨询地点
     * 
     * @param id 咨询地点主键
     * @return 咨询地点
     */
    public ConsultationLocation selectConsultationLocationById(Long id);

    /**
     * 查询咨询地点列表
     * 
     * @param consultationLocation 咨询地点
     * @return 咨询地点集合
     */
    public List<ConsultationLocation> selectConsultationLocationList(ConsultationLocation consultationLocation);

    /**
     * 新增咨询地点
     * 
     * @param consultationLocation 咨询地点
     * @return 结果
     */
    public int insertConsultationLocation(ConsultationLocation consultationLocation);

    /**
     * 修改咨询地点
     * 
     * @param consultationLocation 咨询地点
     * @return 结果
     */
    public int updateConsultationLocation(ConsultationLocation consultationLocation);

    /**
     * 批量删除咨询地点
     * 
     * @param ids 需要删除的咨询地点主键集合
     * @return 结果
     */
    public int deleteConsultationLocationByIds(Long[] ids);

    /**
     * 删除咨询地点信息
     * 
     * @param id 咨询地点主键
     * @return 结果
     */
    public int deleteConsultationLocationById(Long id);
}
