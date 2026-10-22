package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 硬件设备与高拍仪管理对象 biz_device
 * 
 * @author 杨正和
 * @date 2026-03-04
 */
public class BizDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备ID */
    private Long deviceId;

    /** 设备名称（如：莲池厂区一楼高拍仪） */
    @Excel(name = "设备名称", readConverterExp = "如=：莲池厂区一楼高拍仪")
    private String deviceName;

    /** 设备类型（关联字典，如：高拍仪、考勤机） */
    @Excel(name = "设备类型", readConverterExp = "关=联字典，如：高拍仪、考勤机")
    private String deviceType;

    /** 设备序列号/MAC地址（唯一硬件标识，防篡改） */
    @Excel(name = "设备序列号/MAC地址", readConverterExp = "唯=一硬件标识，防篡改")
    private String deviceSn;

    /** 归属部门ID */
    @Excel(name = "归属部门ID")
    private Long deptId;

    /** 部署地点ID（关联地点表） */
    @Excel(name = "部署地点ID", readConverterExp = "关=联地点表")
    private Long locationId;

    /** 绑定IP地址（可选验证项） */
    @Excel(name = "绑定IP地址", readConverterExp = "可=选验证项")
    private String ipAddress;

    /** 状态（0正常 1停用 2报废） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用,2=报废")
    private String status;

    /** 最后一次上传活跃时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后一次上传活跃时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastActiveTime;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public void setDeviceId(Long deviceId) 
    {
        this.deviceId = deviceId;
    }

    public Long getDeviceId() 
    {
        return deviceId;
    }

    public void setDeviceName(String deviceName) 
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName() 
    {
        return deviceName;
    }

    public void setDeviceType(String deviceType) 
    {
        this.deviceType = deviceType;
    }

    public String getDeviceType() 
    {
        return deviceType;
    }

    public void setDeviceSn(String deviceSn) 
    {
        this.deviceSn = deviceSn;
    }

    public String getDeviceSn() 
    {
        return deviceSn;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setLocationId(Long locationId) 
    {
        this.locationId = locationId;
    }

    public Long getLocationId() 
    {
        return locationId;
    }

    public void setIpAddress(String ipAddress) 
    {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() 
    {
        return ipAddress;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setLastActiveTime(Date lastActiveTime) 
    {
        this.lastActiveTime = lastActiveTime;
    }

    public Date getLastActiveTime() 
    {
        return lastActiveTime;
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
            .append("deviceId", getDeviceId())
            .append("deviceName", getDeviceName())
            .append("deviceType", getDeviceType())
            .append("deviceSn", getDeviceSn())
            .append("deptId", getDeptId())
            .append("locationId", getLocationId())
            .append("ipAddress", getIpAddress())
            .append("status", getStatus())
            .append("lastActiveTime", getLastActiveTime())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
