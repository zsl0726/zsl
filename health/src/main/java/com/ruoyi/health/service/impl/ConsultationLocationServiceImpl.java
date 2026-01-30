package com.ruoyi.health.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.health.mapper.ConsultationLocationMapper;
import com.ruoyi.health.domain.ConsultationLocation;
import com.ruoyi.health.service.IConsultationLocationService;

/**
 * 咨询地点Service业务层处理
 * 
 * @author GGbond
 * @date 2025-12-19
 */
@Service
public class ConsultationLocationServiceImpl implements IConsultationLocationService 
{
    @Autowired
    private ConsultationLocationMapper consultationLocationMapper;

    /**
     * 查询咨询地点
     * 
     * @param id 咨询地点主键
     * @return 咨询地点
     */
    @Override
    public ConsultationLocation selectConsultationLocationById(Long id)
    {
        return consultationLocationMapper.selectConsultationLocationById(id);
    }

    /**
     * 查询咨询地点列表
     * 
     * @param consultationLocation 咨询地点
     * @return 咨询地点
     */
    @Override
    public List<ConsultationLocation> selectConsultationLocationList(ConsultationLocation consultationLocation)
    {
        return consultationLocationMapper.selectConsultationLocationList(consultationLocation);
    }

    /**
     * 新增咨询地点
     * 
     * @param consultationLocation 咨询地点
     * @return 结果
     */
    @Override
    public int insertConsultationLocation(ConsultationLocation consultationLocation)
    {
        consultationLocation.setCreateTime(DateUtils.getNowDate());
        return consultationLocationMapper.insertConsultationLocation(consultationLocation);
    }

    /**
     * 修改咨询地点
     * 
     * @param consultationLocation 咨询地点
     * @return 结果
     */
    @Override
    public int updateConsultationLocation(ConsultationLocation consultationLocation)
    {
        consultationLocation.setUpdateTime(DateUtils.getNowDate());
        return consultationLocationMapper.updateConsultationLocation(consultationLocation);
    }

    /**
     * 批量删除咨询地点
     * 
     * @param ids 需要删除的咨询地点主键
     * @return 结果
     */
    @Override
    public int deleteConsultationLocationByIds(Long[] ids)
    {
        return consultationLocationMapper.deleteConsultationLocationByIds(ids);
    }

    /**
     * 删除咨询地点信息
     * 
     * @param id 咨询地点主键
     * @return 结果
     */
    @Override
    public int deleteConsultationLocationById(Long id)
    {
        return consultationLocationMapper.deleteConsultationLocationById(id);
    }
}
