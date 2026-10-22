package com.ruoyi.biz.util;

import java.time.LocalDate;

/**
 * 流水号生成结果
 *
 * @author admin
 * @date 2026-06-16
 */
public class SerialNoResult
{
    /** 业务流水号(格式: yyyyMMdd-NNN) */
    private final String serialNo;

    /** 流水号日期(解析后的 LocalDate,用于写入 serial_date 列) */
    private final LocalDate serialDate;

    public SerialNoResult(String serialNo, LocalDate serialDate)
    {
        this.serialNo = serialNo;
        this.serialDate = serialDate;
    }

    public String getSerialNo()
    {
        return serialNo;
    }

    public LocalDate getSerialDate()
    {
        return serialDate;
    }
}
