package com.ruoyi.biz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 培训地点与会议室管理对象 biz_train_location
 * 
 * @author admin
 * @date 2026-03-04
 */
public class BizTrainLocation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 地点ID */
    private Long locationId;

    /** 地点/会议室名称（如：高新厂区技术部会议室） */
    @Excel(name = "地点/会议室名称", readConverterExp = "如=：高新厂区技术部会议室")
    private String locationName;

    /** 最大容纳人数（用于后期防超载预警） */
    @Excel(name = "最大容纳人数", readConverterExp = "用=于后期防超载预警")
    private Integer capacity;

    /** 状态（0正常 1停用/维修中） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用/维修中")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setLocationId(Long locationId) 
    {
        this.locationId = locationId;
    }

    public Long getLocationId() 
    {
        return locationId;
    }

    public void setLocationName(String locationName) 
    {
        this.locationName = locationName;
    }

    public String getLocationName() 
    {
        return locationName;
    }

    public void setCapacity(Integer capacity) 
    {
        this.capacity = capacity;
    }

    public Integer getCapacity() 
    {
        return capacity;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("locationId", getLocationId())
            .append("locationName", getLocationName())
            .append("capacity", getCapacity())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
