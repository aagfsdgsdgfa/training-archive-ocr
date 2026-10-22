package com.ruoyi.biz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 年度培训计划对象 biz_train_plan
 * 
 * @author admin
 * @date 2026-03-03
 */
public class BizTrainPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计划ID */
    private Long planId;

    /** 计划年份，如2026 */
    @Excel(name = "计划年份，如2026")
    private Integer planYear;

    /** 计划执行月份（"2026年6月" / "6月" / "不定时" / null） */
    @Excel(name = "计划执行月份")
    private String planMonth;

    /** 计划类型（1计划内 2计划外） */
    @Excel(name = "计划类型", readConverterExp = "1=计划内,2=计划外")
    private String planType;

    /** 项目分类（如新员工入职、GMP文件） */
    @Excel(name = "项目分类", readConverterExp = "如=新员工入职、GMP文件")
    private String projectCategory;

    /** 培训主题 */
    @Excel(name = "培训主题")
    private String subject;

    /** 培训级别（公司级/部门级） */
    @Excel(name = "培训级别", readConverterExp = "公=司级/部门级")
    private String trainLevel;

    /** 主办/发起部门ID */
    @Excel(name = "主办/发起部门ID")
    private Long hostDeptId;

    /** 主办/发起部门名称 */
    @Excel(name = "主办/发起部门名称")
    private String hostDeptName;

    /** 受众范围（0全体员工 1部门全员 2指定人员） */
    @Excel(name = "受众范围", readConverterExp = "0=全体员工,1=部门全员,2=指定人员")
    private String audienceScope;

    /** 受众ID集合（存部门ID或人员ID，逗号分隔，如 101,103） */
    @Excel(name = "受众ID集合", readConverterExp = "存=部门ID或人员ID，逗号分隔，如,1=01,103")
    private String audienceIds;

    /** 受众原文（保留 Excel 里的"培训对象"原始文本，含多部门+角色筛选等复杂语义） */
    @Excel(name = "受众原文")
    private String audienceText;

    /** 计划状态（0未完成 1已完成 2已取消） */
    @Excel(name = "计划状态", readConverterExp = "0=未完成,1=已完成,2=已取消")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }

    public void setPlanYear(Integer planYear) 
    {
        this.planYear = planYear;
    }

    public Integer getPlanYear() 
    {
        return planYear;
    }

    public void setPlanMonth(String planMonth)
    {
        this.planMonth = planMonth;
    }

    public String getPlanMonth()
    {
        return planMonth;
    }

    public void setPlanType(String planType) 
    {
        this.planType = planType;
    }

    public String getPlanType() 
    {
        return planType;
    }

    public void setProjectCategory(String projectCategory) 
    {
        this.projectCategory = projectCategory;
    }

    public String getProjectCategory() 
    {
        return projectCategory;
    }

    public void setSubject(String subject) 
    {
        this.subject = subject;
    }

    public String getSubject() 
    {
        return subject;
    }

    public void setTrainLevel(String trainLevel) 
    {
        this.trainLevel = trainLevel;
    }

    public String getTrainLevel() 
    {
        return trainLevel;
    }

    public void setHostDeptId(Long hostDeptId) 
    {
        this.hostDeptId = hostDeptId;
    }

    public Long getHostDeptId() 
    {
        return hostDeptId;
    }

    public void setHostDeptName(String hostDeptName) 
    {
        this.hostDeptName = hostDeptName;
    }

    public String getHostDeptName() 
    {
        return hostDeptName;
    }

    public void setAudienceScope(String audienceScope) 
    {
        this.audienceScope = audienceScope;
    }

    public String getAudienceScope() 
    {
        return audienceScope;
    }

    public void setAudienceIds(String audienceIds) 
    {
        this.audienceIds = audienceIds;
    }

    public String getAudienceIds()
    {
        return audienceIds;
    }

    public void setAudienceText(String audienceText)
    {
        this.audienceText = audienceText;
    }

    public String getAudienceText()
    {
        return audienceText;
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
            .append("planId", getPlanId())
            .append("planYear", getPlanYear())
            .append("planMonth", getPlanMonth())
            .append("planType", getPlanType())
            .append("projectCategory", getProjectCategory())
            .append("subject", getSubject())
            .append("trainLevel", getTrainLevel())
            .append("hostDeptId", getHostDeptId())
            .append("hostDeptName", getHostDeptName())
            .append("audienceScope", getAudienceScope())
            .append("audienceIds", getAudienceIds())
            .append("audienceText", getAudienceText())
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
