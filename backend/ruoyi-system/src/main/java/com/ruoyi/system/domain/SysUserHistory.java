package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 员工信息历史快照表 SCD Type 2
 * 用于追溯员工姓名、部门、岗位的历史变更
 *
 * @author admin
 */
public class SysUserHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 历史记录ID */
    private Long historyId;

    /** 员工ID */
    private Long userId;

    /** 姓名 */
    private String userName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称（冗余存储） */
    private String deptName;

    /** 岗位ID */
    private Long postId;

    /** 岗位名称（冗余存储） */
    private String postName;

    /** 生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    /** 失效时间（NULL=当前有效） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    public Long getHistoryId()
    {
        return historyId;
    }

    public void setHistoryId(Long historyId)
    {
        this.historyId = historyId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public Long getPostId()
    {
        return postId;
    }

    public void setPostId(Long postId)
    {
        this.postId = postId;
    }

    public String getPostName()
    {
        return postName;
    }

    public void setPostName(String postName)
    {
        this.postName = postName;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("historyId", getHistoryId())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("deptId", getDeptId())
                .append("deptName", getDeptName())
                .append("postId", getPostId())
                .append("postName", getPostName())
                .append("startDate", getStartDate())
                .append("endDate", getEndDate())
                .toString();
    }
}
