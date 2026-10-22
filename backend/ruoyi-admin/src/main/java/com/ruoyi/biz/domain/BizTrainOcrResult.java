package com.ruoyi.biz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OCR识别结果暂存明细对象 biz_train_ocr_result
 * 
 * @author admin
 * @date 2026-03-04
 */
public class BizTrainOcrResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结果明细ID */
    private Long resultId;

    /** 所属批次ID */
    @Excel(name = "所属批次ID")
    private Long batchId;

    /** 原始签到序号 */
    @Excel(name = "签到序号")
    private String signSeq;

    /** 来源于哪张图片(关联ocr_log) */
    @Excel(name = "来源于哪张图片(关联ocr_log)")
    private Long logId;

    /** OCR提取的姓名（允许前端直接修改） */
    @Excel(name = "OCR提取的姓名", readConverterExp = "允=许前端直接修改")
    private String parsedName;

    /** OCR提取的部门 */
    @Excel(name = "OCR提取的部门")
    private String parsedDept;

    /** OCR提取的签到/考核成绩 */
    @Excel(name = "OCR提取的签到/考核成绩")
    private String parsedScore;

    /** OCR提取的备注(授课人/未参加/缺席/缺考等,缺席人员不计入实到) */
    @Excel(name = "OCR提取的备注")
    private String parsedRemark;

    /** 是否为系统未知人员（0否 1是，若是则最终入库时自动建号） */
    @Excel(name = "是否为系统未知人员", readConverterExp = "0=否,1=是，若是则最终入库时自动建号")
    private String isNewUser;

    /** 数据状态（0-正常待确认 1-管理员手动剔除/删除） */
    @Excel(name = "数据状态", readConverterExp = "0=-正常待确认,1=-管理员手动剔除/删除")
    private String status;

    public void setResultId(Long resultId) 
    {
        this.resultId = resultId;
    }

    public Long getResultId() 
    {
        return resultId;
    }

    public void setBatchId(Long batchId) 
    {
        this.batchId = batchId;
    }

    public Long getBatchId() 
    {
        return batchId;
    }

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }

    public void setParsedName(String parsedName) 
    {
        this.parsedName = parsedName;
    }

    public String getParsedName() 
    {
        return parsedName;
    }

    public void setParsedDept(String parsedDept) 
    {
        this.parsedDept = parsedDept;
    }

    public String getParsedDept() 
    {
        return parsedDept;
    }

    public void setParsedScore(String parsedScore)
    {
        this.parsedScore = parsedScore;
    }

    public String getParsedScore()
    {
        return parsedScore;
    }

    public void setParsedRemark(String parsedRemark)
    {
        this.parsedRemark = parsedRemark;
    }

    public String getParsedRemark()
    {
        return parsedRemark;
    }

    public void setIsNewUser(String isNewUser)
    {
        this.isNewUser = isNewUser;
    }

    public String getIsNewUser() 
    {
        return isNewUser;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public String getSignSeq() {
        return signSeq;
    }

    public void setSignSeq(String signSeq) {
        this.signSeq = signSeq;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("resultId", getResultId())
            .append("batchId", getBatchId())
            .append("logId", getLogId())
            .append("parsedName", getParsedName())
            .append("parsedDept", getParsedDept())
            .append("parsedScore", getParsedScore())
            .append("parsedRemark", getParsedRemark())
            .append("isNewUser", getIsNewUser())
            .append("status", getStatus())
            .toString();
    }
}
