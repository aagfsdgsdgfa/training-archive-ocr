package com.ruoyi.biz.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 个人培训档案明细对象 biz_train_archive
 * 
 * @author admin
 * @date 2026-03-03
 */
public class BizTrainArchive extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 档案记录ID */
    private Long archiveId;

    /** 系统用户ID（导入的历史老数据此项可为空） */
    @Excel(name = "系统用户ID", readConverterExp = "导=入的历史老数据此项可为空")
    private Long userId;

    /** 系统部门ID（历史老数据可为空） */
    @Excel(name = "系统部门ID", readConverterExp = "历=史老数据可为空")
    private Long deptId;

    /** 姓名（强冗余：防止未来员工离职丢失历史档案） */
    @Excel(name = "姓名", readConverterExp = "强=冗余：防止未来员工离职丢失历史档案")
    private String userName;

    /** 部门（强冗余：防止部门改名或重组丢失信息） */
    @Excel(name = "部门", readConverterExp = "强=冗余：防止部门改名或重组丢失信息")
    private String deptName;

    /** 关联年度计划ID */
    @Excel(name = "关联年度计划ID")
    private Long planId;

    /** 关联台账总表ID */
    @Excel(name = "关联台账总表ID")
    private Long ledgerId;

    /** 培训日期（兼顾历史长文本跨日格式） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 培训日期 */
    @Excel(name = "培训日期", readConverterExp = "兼顾历史长文本跨日格式",width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private String trainDate;

    /** 开始时间（查询用） */
    private String beginTrainDate;

    /** 结束时间（查询用） */
    private String endTrainDate;

    /** 培训部门 */
    @Excel(name = "培训部门")
    private String trainDept;

    /** 培训地点 */
    @Excel(name = "培训地点")
    private String trainLocation;

    /** 培训内容 */
    @Excel(name = "培训主题")
    private String subject;

    /** 具体的培训内容 */
    @Excel(name = "培训内容")
    private String trainContent;

    /** 项目分类（从年度计划继承） */
    @Excel(name = "项目分类")
    private String projectCategory;

    /** 签到序号（专门为复刻旧版历史档案预留） */
    @Excel(name = "签到序号", readConverterExp = "专=门为复刻旧版历史档案预留")
    private String signSeq;

    /** 授课人 */
    @Excel(name = "授课人")
    private String speaker;

    /** 成绩（使用文本存储，如 98.0 / 合格 / 未考核） */
    @Excel(name = "成绩", readConverterExp = "使=用文本存储，如,9=8.0,/=,合=格,/=,未=考核")
    private String score;

    /** 培训学时 */
    @Excel(name = "培训学时")
    private String trainHours;

    /** 岗位变化（为旧版档案结构预留） */
    @Excel(name = "岗位变化", readConverterExp = "为=旧版档案结构预留")
    private String jobChange;

    /** 岗位名称（从历史快照获取） */
    private String postName;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public String getTrainContent() {
        return trainContent;
    }

    public void setTrainContent(String trainContent) {
        this.trainContent = trainContent;
    }

    public void setArchiveId(Long archiveId)
    {
        this.archiveId = archiveId;
    }

    public Long getArchiveId() 
    {
        return archiveId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setDeptName(String deptName) 
    {
        this.deptName = deptName;
    }

    public String getDeptName() 
    {
        return deptName;
    }

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }

    public void setLedgerId(Long ledgerId) 
    {
        this.ledgerId = ledgerId;
    }

    public Long getLedgerId() 
    {
        return ledgerId;
    }

    public void setTrainDate(String trainDate)
    {
        this.trainDate = trainDate;
    }

    public String getTrainDate()
    {
        return trainDate;
    }

    public String getBeginTrainDate()
    {
        return beginTrainDate;
    }

    public void setBeginTrainDate(String beginTrainDate)
    {
        this.beginTrainDate = beginTrainDate;
    }

    public String getEndTrainDate()
    {
        return endTrainDate;
    }

    public void setEndTrainDate(String endTrainDate)
    {
        this.endTrainDate = endTrainDate;
    }

    public void setTrainDept(String trainDept) 
    {
        this.trainDept = trainDept;
    }

    public String getTrainDept() 
    {
        return trainDept;
    }

    public void setTrainLocation(String trainLocation) 
    {
        this.trainLocation = trainLocation;
    }

    public String getTrainLocation() 
    {
        return trainLocation;
    }

    public void setSubject(String subject) 
    {
        this.subject = subject;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setProjectCategory(String projectCategory)
    {
        this.projectCategory = projectCategory;
    }

    public String getProjectCategory()
    {
        return projectCategory;
    }

    public void setSignSeq(String signSeq) 
    {
        this.signSeq = signSeq;
    }

    public String getSignSeq() 
    {
        return signSeq;
    }

    public void setSpeaker(String speaker) 
    {
        this.speaker = speaker;
    }

    public String getSpeaker() 
    {
        return speaker;
    }

    public void setScore(String score) 
    {
        this.score = score;
    }

    public String getScore() 
    {
        return score;
    }

    public void setTrainHours(String trainHours) 
    {
        this.trainHours = trainHours;
    }

    public String getTrainHours() 
    {
        return trainHours;
    }

    public void setJobChange(String jobChange) 
    {
        this.jobChange = jobChange;
    }

    public String getJobChange()
    {
        return jobChange;
    }

    public String getPostName()
    {
        return postName;
    }

    public void setPostName(String postName)
    {
        this.postName = postName;
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
            .append("archiveId", getArchiveId())
            .append("userId", getUserId())
            .append("deptId", getDeptId())
            .append("userName", getUserName())
            .append("deptName", getDeptName())
            .append("planId", getPlanId())
            .append("ledgerId", getLedgerId())
            .append("trainDate", getTrainDate())
            .append("trainDept", getTrainDept())
            .append("trainLocation", getTrainLocation())
            .append("subject", getSubject())
            .append("projectCategory", getProjectCategory())
            .append("signSeq", getSignSeq())
            .append("speaker", getSpeaker())
            .append("score", getScore())
            .append("trainHours", getTrainHours())
            .append("jobChange", getJobChange())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
