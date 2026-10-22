package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 线上考勤与申诉明细对象 biz_train_attendance
 * 
 * @author admin
 * @date 2026-03-03
 */
public class BizTrainAttendance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 考勤记录ID */
    private Long recordId;

    /** 关联执行场次ID */
    @Excel(name = "关联执行场次ID")
    private Long sessionId;

    /** 打卡员工ID */
    @Excel(name = "打卡员工ID")
    private Long userId;

    /** 实际打卡签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际打卡签到时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date signTime;

    /** 签到状态（0正常 1迟到 2缺卡 3免签） */
    @Excel(name = "签到状态", readConverterExp = "0=正常,1=迟到,2=缺卡,3=免签")
    private String signStatus;

    /** 申诉状态（0无申诉 1待管理员审核 2已通过 3已驳回） */
    @Excel(name = "申诉状态", readConverterExp = "0=无申诉,1=待管理员审核,2=已通过,3=已驳回")
    private String appealStatus;

    /** 补卡/申诉原因 */
    @Excel(name = "补卡/申诉原因")
    private String appealReason;

    /** 申诉佐证截图地址 */
    @Excel(name = "申诉佐证截图地址")
    private String appealImage;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 来源（online=线上打卡, ocr=OCR扫表） */
    @Excel(name = "来源", readConverterExp = "online=线上打卡,ocr=OCR扫表")
    private String source;

    /** 签到序号 */
    @Excel(name = "签到序号")
    private String signSeq;

    /** 考核成绩/签到情况 */
    @Excel(name = "考核成绩")
    private String score;

    /** 成绩等级（前端查询用：excellent/good/pass/fail，不入库） */
    private String scoreLevel;

    public String getSignSeq() {
        return signSeq;
    }
    public void setSignSeq(String signSeq) {
        this.signSeq = signSeq;
    }
    public String getScore() {
        return score;
    }
    public void setScore(String score) {
        this.score = score;
    }
    public String getScoreLevel() {
        return scoreLevel;
    }
    public void setScoreLevel(String scoreLevel) {
        this.scoreLevel = scoreLevel;
    }

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setSessionId(Long sessionId) 
    {
        this.sessionId = sessionId;
    }

    public Long getSessionId() 
    {
        return sessionId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setSignTime(Date signTime) 
    {
        this.signTime = signTime;
    }

    public Date getSignTime() 
    {
        return signTime;
    }

    public void setSignStatus(String signStatus) 
    {
        this.signStatus = signStatus;
    }

    public String getSignStatus() 
    {
        return signStatus;
    }

    public void setAppealStatus(String appealStatus) 
    {
        this.appealStatus = appealStatus;
    }

    public String getAppealStatus() 
    {
        return appealStatus;
    }

    public void setAppealReason(String appealReason) 
    {
        this.appealReason = appealReason;
    }

    public String getAppealReason() 
    {
        return appealReason;
    }

    public void setAppealImage(String appealImage) 
    {
        this.appealImage = appealImage;
    }

    public String getAppealImage() 
    {
        return appealImage;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("sessionId", getSessionId())
            .append("userId", getUserId())
            .append("signTime", getSignTime())
            .append("signStatus", getSignStatus())
            .append("appealStatus", getAppealStatus())
            .append("appealReason", getAppealReason())
            .append("appealImage", getAppealImage())
            .append("delFlag", getDelFlag())
            .append("source", getSource())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
