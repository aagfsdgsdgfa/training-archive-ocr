package com.ruoyi.biz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OCR防重与拦截记录对象 biz_train_ocr_log
 * 
 * @author admin
 * @date 2026-03-04
 */
public class BizTrainOcrLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志记录ID */
    private Long logId;

    /** 归属的批次ID */
    @Excel(name = "归属的批次ID")
    private Long batchId;

    /** 图片文件的MD5哈希值（物理层防重核心） */
    @Excel(name = "图片文件的MD5哈希值", readConverterExp = "物=理层防重核心")
    private String fileMd5;

    /** 上传的图片存储地址 */
    @Excel(name = "上传的图片存储地址")
    private String fileUrl;

    /** OCR提取的培训主题 */
    @Excel(name = "OCR提取的培训主题")
    private String ocrSubject;

    /** OCR提取的培训日期 */
    @Excel(name = "OCR提取的培训日期")
    private String ocrDate;

    /** OCR识别出打勾/签字的实际人数 */
    @Excel(name = "OCR识别出打勾/签字的实际人数")
    private Integer ocrActualCount;

    /** 处理状态（0拦截阻断重复 1正常入库解析通过） */
    @Excel(name = "处理状态", readConverterExp = "0=拦截阻断重复,1=正常入库解析通过")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }

    public void setBatchId(Long batchId) 
    {
        this.batchId = batchId;
    }

    public Long getBatchId() 
    {
        return batchId;
    }

    public void setFileMd5(String fileMd5) 
    {
        this.fileMd5 = fileMd5;
    }

    public String getFileMd5() 
    {
        return fileMd5;
    }

    public void setFileUrl(String fileUrl) 
    {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() 
    {
        return fileUrl;
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

    public void setOcrActualCount(Integer ocrActualCount) 
    {
        this.ocrActualCount = ocrActualCount;
    }

    public Integer getOcrActualCount() 
    {
        return ocrActualCount;
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
            .append("logId", getLogId())
            .append("batchId", getBatchId())
            .append("fileMd5", getFileMd5())
            .append("fileUrl", getFileUrl())
            .append("ocrSubject", getOcrSubject())
            .append("ocrDate", getOcrDate())
            .append("ocrActualCount", getOcrActualCount())
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
