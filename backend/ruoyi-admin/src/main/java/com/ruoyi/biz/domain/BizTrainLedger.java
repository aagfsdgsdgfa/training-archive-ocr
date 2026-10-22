package com.ruoyi.biz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.time.LocalDate;

/**
 * 培训台账总对象 biz_train_ledger
 *
 * @author admin
 * @date 2026-03-03
 */
public class BizTrainLedger extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 台账ID */
    private Long ledgerId;

    /** 业务流水号(yyyyMMdd-NNN,按培训日期生成,每天重置) */
    @Excel(name = "台账流号")
    private String serialNo;

    /** 流水号日期(取培训日期,解析失败时回退到创建日期) */
    private LocalDate serialDate;

    /** 溯源计划ID */
    @Excel(name = "溯源计划ID")
    private Long planId;

    /** 溯源场次ID */
    @Excel(name = "溯源场次ID")
    private Long sessionId;

    /** 培训部门 */
    @Excel(name = "培训部门")
    private String hostDeptName;

    /** 项目分类 */
    @Excel(name = "项目分类")
    private String projectCategory;

    /** 培训主题 */
    @Excel(name = "培训主题")
    private String subject;

    /** 培训日期（用varchar兼容如"2023年8月22-24日"的合并文本） */
    @Excel(name = "培训日期", readConverterExp = "用=varchar兼容如2023年8月22-24日的合并文本")
    private String trainDate;

    /** 培训学时 */
    @Excel(name = "培训学时")
    private String trainHours;

    /** 授课人 */
    @Excel(name = "授课人")
    private String speaker;

    /** 培训地点 */
    @Excel(name = "培训地点")
    private String locationName;

    /** 参加部门/人员摘要文本 */
    @Excel(name = "参加部门/人员摘要文本")
    private String attendeeNames;

    /** 参加人员ID列表（逗号分隔，从biz_train_archive关联获取，用于历史快照查询） */
    private String attendeeUserIds;

    /** 应参加人数 */
    @Excel(name = "应参加人数")
    private Integer targetCount;

    /** 实际参加人数 */
    @Excel(name = "实际参加人数")
    private Integer actualCount;

    /** 计划情况（计划内/计划外） */
    @Excel(name = "计划情况", readConverterExp = "计=划内/计划外")
    private String planType;

    /** 级别（公司级/部门级） */
    @Excel(name = "级别", readConverterExp = "公=司级/部门级")
    private String trainLevel;

    /** 电子凭证/扫描件地址（用于溯源核对原始签字情况） */
    @Excel(name = "电子凭证/扫描件地址", readConverterExp = "用=于溯源核对原始签字情况")
    private String attachmentUrl;

    /** 备注说明 */
    @Excel(name = "备注")
    private String remark;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 开始日期（查询用，不入库） */
    private String beginTrainDate;

    /** 结束日期（查询用，不入库） */
    private String endTrainDate;

    private Integer index;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setLedgerId(Long ledgerId)
    {
        this.ledgerId = ledgerId;
    }

    public Long getLedgerId()
    {
        return ledgerId;
    }

    public void setSerialNo(String serialNo)
    {
        this.serialNo = serialNo;
    }

    public String getSerialNo()
    {
        return serialNo;
    }

    public void setSerialDate(LocalDate serialDate)
    {
        this.serialDate = serialDate;
    }

    public LocalDate getSerialDate()
    {
        return serialDate;
    }

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }

    public void setSessionId(Long sessionId) 
    {
        this.sessionId = sessionId;
    }

    public Long getSessionId() 
    {
        return sessionId;
    }

    public void setHostDeptName(String hostDeptName) 
    {
        this.hostDeptName = hostDeptName;
    }

    public String getHostDeptName() 
    {
        return hostDeptName;
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

    public void setTrainDate(String trainDate) 
    {
        this.trainDate = trainDate;
    }

    public String getTrainDate()
    {
        return trainDate;
    }

    public void setTrainHours(String trainHours) 
    {
        this.trainHours = trainHours;
    }

    public String getTrainHours() 
    {
        return trainHours;
    }

    public void setSpeaker(String speaker) 
    {
        this.speaker = speaker;
    }

    public String getSpeaker() 
    {
        return speaker;
    }

    public void setLocationName(String locationName) 
    {
        this.locationName = locationName;
    }

    public String getLocationName() 
    {
        return locationName;
    }

    public void setAttendeeNames(String attendeeNames)
    {
        this.attendeeNames = attendeeNames;
    }

    public String getAttendeeNames()
    {
        return attendeeNames;
    }

    public String getAttendeeUserIds()
    {
        return attendeeUserIds;
    }

    public void setAttendeeUserIds(String attendeeUserIds)
    {
        this.attendeeUserIds = attendeeUserIds;
    }

    public void setTargetCount(Integer targetCount) 
    {
        this.targetCount = targetCount;
    }

    public Integer getTargetCount() 
    {
        return targetCount;
    }

    public void setActualCount(Integer actualCount) 
    {
        this.actualCount = actualCount;
    }

    public Integer getActualCount() 
    {
        return actualCount;
    }

    public void setPlanType(String planType) 
    {
        this.planType = planType;
    }

    public String getPlanType() 
    {
        return planType;
    }

    public void setTrainLevel(String trainLevel) 
    {
        this.trainLevel = trainLevel;
    }

    public String getTrainLevel() 
    {
        return trainLevel;
    }

    public void setAttachmentUrl(String attachmentUrl) 
    {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentUrl()
    {
        return attachmentUrl;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public String getBeginTrainDate() {
        return beginTrainDate;
    }

    public void setBeginTrainDate(String beginTrainDate) {
        this.beginTrainDate = beginTrainDate;
    }

    public String getEndTrainDate() {
        return endTrainDate;
    }

    public void setEndTrainDate(String endTrainDate) {
        this.endTrainDate = endTrainDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("ledgerId", getLedgerId())
            .append("serialNo", getSerialNo())
            .append("serialDate", getSerialDate())
            .append("planId", getPlanId())
            .append("sessionId", getSessionId())
            .append("hostDeptName", getHostDeptName())
            .append("projectCategory", getProjectCategory())
            .append("subject", getSubject())
            .append("trainDate", getTrainDate())
            .append("trainHours", getTrainHours())
            .append("speaker", getSpeaker())
            .append("locationName", getLocationName())
            .append("attendeeNames", getAttendeeNames())
            .append("targetCount", getTargetCount())
            .append("actualCount", getActualCount())
            .append("planType", getPlanType())
            .append("trainLevel", getTrainLevel())
            .append("attachmentUrl", getAttachmentUrl())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
