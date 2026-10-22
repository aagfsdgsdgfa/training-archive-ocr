package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 培训执行场次对象 biz_train_session
 * 
 * @author admin
 * @date 2026-03-03
 */
public class BizTrainSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 场次ID */
    private Long sessionId;

    /** 关联年度计划ID（强绑定核销使用） */
    @Excel(name = "关联年度计划ID", readConverterExp = "强=绑定核销使用")
    private Long planId;

    /** 培训主题 */
    @Excel(name = "培训主题")
    private String subject;

    /** 主办部门ID */
    @Excel(name = "主办部门ID")
    private Long hostDeptId;

    /** 主办部门名称 */
    @Excel(name = "主办部门名称")
    private String hostDeptName;

    /** 授课人 */
    @Excel(name = "授课人")
    private String speaker;

    /** 培训学时（如 1.5, 2.0） */
    @Excel(name = "培训学时", readConverterExp = "如=,1=.5,,2=.0")
    private BigDecimal trainHours;

    /** 培训地点（关联地点字典库） */
    @Excel(name = "培训地点", readConverterExp = "关=联地点字典库")
    private String locationName;
    /** 培训内容 */
    @Excel(name = "培训内容")
    private String trainContent;

    public String getTrainContent() {
        return trainContent;
    }

    public void setTrainContent(String trainContent) {
        this.trainContent = trainContent;
    }

    /** 培训日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "培训日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date trainDate;

    /** 培训开始时间（考勤打卡边界） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "培训开始时间", readConverterExp = "考=勤打卡边界", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 培训结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "培训结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 录入考勤方式（0线上打卡 1线下纸质OCR） */
    @Excel(name = "录入考勤方式", readConverterExp = "0=线上打卡,1=线下纸质OCR")
    private String signType;

    /** 防飞检凭证（OCR扫描件/现场照片的存储路径，多个用逗号隔开） */
    @Excel(name = "防飞检凭证", readConverterExp = "O=CR扫描件/现场照片的存储路径，多个用逗号隔开")
    private String attachmentUrl;

    /** 场次状态（0未开始 1进行中 2待审归档 3已归档） */
    @Excel(name = "场次状态", readConverterExp = "0=未开始,1=进行中,2=待审归档,3=已归档")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 来源（online=线上打卡, ocr=OCR扫表） */
    @Excel(name = "来源", readConverterExp = "online=线上打卡,ocr=OCR扫表")
    private String source;

    /** 关联OCR批次ID（仅OCR来源有效） */
    @Excel(name = "关联OCR批次ID")
    private Long batchId;

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public Long getSessionId() 
    {
        return sessionId;
    }

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }

    public void setSubject(String subject) 
    {
        this.subject = subject;
    }

    public String getSubject() 
    {
        return subject;
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

    public void setSpeaker(String speaker) 
    {
        this.speaker = speaker;
    }

    public String getSpeaker() 
    {
        return speaker;
    }

    public void setTrainHours(BigDecimal trainHours) 
    {
        this.trainHours = trainHours;
    }

    public BigDecimal getTrainHours() 
    {
        return trainHours;
    }

    public void setLocationName(String locationName) 
    {
        this.locationName = locationName;
    }

    public String getLocationName() 
    {
        return locationName;
    }

    public void setTrainDate(Date trainDate) 
    {
        this.trainDate = trainDate;
    }

    public Date getTrainDate() 
    {
        return trainDate;
    }

    public void setStartTime(Date startTime) 
    {
        this.startTime = startTime;
    }

    public Date getStartTime() 
    {
        return startTime;
    }

    public void setEndTime(Date endTime) 
    {
        this.endTime = endTime;
    }

    public Date getEndTime() 
    {
        return endTime;
    }

    public void setSignType(String signType) 
    {
        this.signType = signType;
    }

    public String getSignType() 
    {
        return signType;
    }

    public void setAttachmentUrl(String attachmentUrl) 
    {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentUrl() 
    {
        return attachmentUrl;
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

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getSource()
    {
        return source;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("sessionId", getSessionId())
            .append("planId", getPlanId())
            .append("subject", getSubject())
            .append("hostDeptId", getHostDeptId())
            .append("hostDeptName", getHostDeptName())
            .append("speaker", getSpeaker())
            .append("trainHours", getTrainHours())
            .append("locationName", getLocationName())
            .append("trainDate", getTrainDate())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("signType", getSignType())
            .append("attachmentUrl", getAttachmentUrl())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("source", getSource())
            .append("batchId", getBatchId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
