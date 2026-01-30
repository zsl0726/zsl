package com.ruoyi.health.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 咨询地点对象 consultation_location
 * 
 * @author GGbond
 * @date 2025-12-19
 */
public class ConsultationLocation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 地点名称 */
    private String locationName;

    /** 详细地址 */
    private String address;

    /** 可用房间号（JSON格式） */
    private String roomNumbers;

    /** 可预约时间段（JSON格式） */
    private String availableTimes;

    /** 同时可容纳咨询数 */
    private Long capacity;

    /** 是否可用（0-不可用 1-可用） */
    private String isAvailable;

    /** 地点描述 */
    private String description;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setLocationName(String locationName) 
    {
        this.locationName = locationName;
    }

    public String getLocationName() 
    {
        return locationName;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setRoomNumbers(String roomNumbers) 
    {
        this.roomNumbers = roomNumbers;
    }

    public String getRoomNumbers() 
    {
        return roomNumbers;
    }

    public void setAvailableTimes(String availableTimes) 
    {
        this.availableTimes = availableTimes;
    }

    public String getAvailableTimes() 
    {
        return availableTimes;
    }

    public void setCapacity(Long capacity) 
    {
        this.capacity = capacity;
    }

    public Long getCapacity() 
    {
        return capacity;
    }

    public void setIsAvailable(String isAvailable) 
    {
        this.isAvailable = isAvailable;
    }

    public String getIsAvailable() 
    {
        return isAvailable;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("locationName", getLocationName())
            .append("address", getAddress())
            .append("roomNumbers", getRoomNumbers())
            .append("availableTimes", getAvailableTimes())
            .append("capacity", getCapacity())
            .append("isAvailable", getIsAvailable())
            .append("description", getDescription())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }
}
