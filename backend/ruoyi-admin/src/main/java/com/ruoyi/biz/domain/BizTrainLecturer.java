package com.ruoyi.biz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 培训讲师资质库对象 biz_train_lecturer
 * 
 * @author admin
 * @date 2026-03-04
 */
public class BizTrainLecturer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 讲师ID */
    private Long lecturerId;

    /** 讲师姓名 */
    @Excel(name = "讲师姓名")
    private String lecturerName;

    /** 讲师类型（0内部员工 1外部聘请） */
    @Excel(name = "讲师类型", readConverterExp = "0=内部员工,1=外部聘请")
    private String lecturerType;

    /** 内部员工关联的系统用户ID */
    @Excel(name = "内部员工关联的系统用户ID")
    private Long userId;

    /** 所属机构（针对外部讲师，如中食药信息网） */
    @Excel(name = "所属机构", readConverterExp = "针=对外部讲师，如中食药信息网")
    private String companyName;

    /** 擅长领域（如：QC理化、GMP规范、验证管理） */
    @Excel(name = "擅长领域", readConverterExp = "如=：QC理化、GMP规范、验证管理")
    private String expertiseArea;

    /** 资质证明附件（如执业药师证、聘书扫描件） */
    @Excel(name = "资质证明附件", readConverterExp = "如=执业药师证、聘书扫描件")
    private String qualificationUrl;

    /** 状态（0可用 1停用） */
    @Excel(name = "状态", readConverterExp = "0=可用,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public void setLecturerId(Long lecturerId) 
    {
        this.lecturerId = lecturerId;
    }

    public Long getLecturerId() 
    {
        return lecturerId;
    }

    public void setLecturerName(String lecturerName) 
    {
        this.lecturerName = lecturerName;
    }

    public String getLecturerName() 
    {
        return lecturerName;
    }

    public void setLecturerType(String lecturerType) 
    {
        this.lecturerType = lecturerType;
    }

    public String getLecturerType() 
    {
        return lecturerType;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setCompanyName(String companyName) 
    {
        this.companyName = companyName;
    }

    public String getCompanyName() 
    {
        return companyName;
    }

    public void setExpertiseArea(String expertiseArea) 
    {
        this.expertiseArea = expertiseArea;
    }

    public String getExpertiseArea() 
    {
        return expertiseArea;
    }

    public void setQualificationUrl(String qualificationUrl) 
    {
        this.qualificationUrl = qualificationUrl;
    }

    public String getQualificationUrl() 
    {
        return qualificationUrl;
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
            .append("lecturerId", getLecturerId())
            .append("lecturerName", getLecturerName())
            .append("lecturerType", getLecturerType())
            .append("userId", getUserId())
            .append("companyName", getCompanyName())
            .append("expertiseArea", getExpertiseArea())
            .append("qualificationUrl", getQualificationUrl())
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
