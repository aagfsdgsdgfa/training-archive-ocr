package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 部门信息历史快照表 SCD Type 2
 * 用于追溯部门名称的历史变更
 *
 * @author admin
 */
public class SysDeptHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 历史记录ID */
    private Long historyId;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

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
                .append("deptId", getDeptId())
                .append("deptName", getDeptName())
                .append("startDate", getStartDate())
                .append("endDate", getEndDate())
                .toString();
    }
}
