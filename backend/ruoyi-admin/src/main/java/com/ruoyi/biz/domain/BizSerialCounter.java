package com.ruoyi.biz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 业务流水号计数器 biz_serial_counter
 *
 * @author admin
 * @date 2026-06-16
 */
public class BizSerialCounter
{
    private static final long serialVersionUID = 1L;

    /** 业务类型 */
    private String bizType;

    /** 日期键 yyyyMMdd */
    private String dateKey;

    /** 当前序号 */
    private Long currentSeq;

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setDateKey(String dateKey)
    {
        this.dateKey = dateKey;
    }

    public String getDateKey()
    {
        return dateKey;
    }

    public void setCurrentSeq(Long currentSeq)
    {
        this.currentSeq = currentSeq;
    }

    public Long getCurrentSeq()
    {
        return currentSeq;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("bizType", getBizType())
            .append("dateKey", getDateKey())
            .append("currentSeq", getCurrentSeq())
            .toString();
    }
}
