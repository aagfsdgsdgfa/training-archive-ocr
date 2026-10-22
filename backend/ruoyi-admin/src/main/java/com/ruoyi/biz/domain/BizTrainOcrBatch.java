package com.ruoyi.biz.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OCR扫描批次主对象 biz_train_ocr_batch
 * 
 * @author admin
 * @date 2026-03-04
 */
public class BizTrainOcrBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 批次ID */
    private Long batchId;

    /** 管理员扫表前选择绑定的年度计划ID */
    @Excel(name = "管理员扫表前选择绑定的年度计划ID")
    private Long planId;

    /** 批次流水号（如 OCR20260304001） */
    @Excel(name = "批次流水号", readConverterExp = "如=,O=CR20260304001")
    private String batchNo;

    /** 本次连扫总页数 */
    @Excel(name = "本次连扫总页数")
    private Integer totalPages;

    /** OCR提取的综合培训主题 */
    @Excel(name = "OCR提取的综合培训主题")
    private String ocrSubject;

    /** 培训内容 */
    @Excel(name = "培训内容")
    private String trainContent;

    public String getTrainContent() { return trainContent; }
    public void setTrainContent(String trainContent) { this.trainContent = trainContent; }

    /** OCR提取的综合培训日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "OCR提取的综合培训日期",width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private String ocrDate;

    /** 批次状态（0-解析中 1-待人工校对编辑 2-已结算入库 3-已作废） */
    @Excel(name = "批次状态", readConverterExp = "0=-解析中,1=-待人工校对编辑,2=-已结算入库,3=-已作废")
    private String status;


    private String ocrHostDept;
    private String ocrSpeaker;
    private String ocrLocation;
    private String ocrTrainHours;
    private String ocrTrainContent;

    /** 应到人数(从Excel顶部元数据"应培训人数/应到人数"提取) */
    @Excel(name = "应到人数")
    private Integer ocrTargetCount;

    /** 实到人数(确认归档时根据明细实际记录数统计) */
    @Excel(name = "实到人数")
    private Integer ocrActualCount;

    public String getOcrHostDept() { return ocrHostDept; }
    public void setOcrHostDept(String ocrHostDept) { this.ocrHostDept = ocrHostDept; }
    public String getOcrSpeaker() { return ocrSpeaker; }
    public void setOcrSpeaker(String ocrSpeaker) { this.ocrSpeaker = ocrSpeaker; }
    public String getOcrLocation() { return ocrLocation; }
    public void setOcrLocation(String ocrLocation) { this.ocrLocation = ocrLocation; }
    public String getOcrTrainHours() { return ocrTrainHours; }
    public void setOcrTrainHours(String ocrTrainHours) { this.ocrTrainHours = ocrTrainHours; }
    public String getOcrTrainContent() { return ocrTrainContent; }
    public void setOcrTrainContent(String ocrTrainContent) { this.ocrTrainContent = ocrTrainContent; }

    public Integer getOcrTargetCount() { return ocrTargetCount; }
    public void setOcrTargetCount(Integer ocrTargetCount) { this.ocrTargetCount = ocrTargetCount; }
    public Integer getOcrActualCount() { return ocrActualCount; }
    public void setOcrActualCount(Integer ocrActualCount) { this.ocrActualCount = ocrActualCount; }

    public void setBatchId(Long batchId) 
    {
        this.batchId = batchId;
    }

    public Long getBatchId() 
    {
        return batchId;
    }

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }

    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }

    public void setTotalPages(Integer totalPages) 
    {
        this.totalPages = totalPages;
    }

    public Integer getTotalPages() 
    {
        return totalPages;
    }

    public void setOcrSubject(String ocrSubject) 
    {
        this.ocrSubject = ocrSubject;
    }

    public String getOcrSubject() 
    {
        return ocrSubject;
    }

    public void setOcrDate(String ocrDate) 
    {
        this.ocrDate = ocrDate;
    }

    public String getOcrDate() 
    {
        return ocrDate;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("batchId", getBatchId())
            .append("planId", getPlanId())
            .append("batchNo", getBatchNo())
            .append("totalPages", getTotalPages())
            .append("ocrSubject", getOcrSubject())
            .append("ocrDate", getOcrDate())
            .append("status", getStatus())
            .append("ocrHostDept", getOcrHostDept())
            .append("ocrSpeaker", getOcrSpeaker())
            .append("ocrLocation", getOcrLocation())
            .append("ocrTrainHours", getOcrTrainHours())
            .append("ocrTrainContent", getOcrTrainContent())
            .append("ocrTargetCount", getOcrTargetCount())
            .append("ocrActualCount", getOcrActualCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
