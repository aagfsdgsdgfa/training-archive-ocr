package com.ruoyi.biz.util;

import com.ruoyi.biz.domain.BizSerialCounter;
import com.ruoyi.biz.mapper.BizSerialCounterMapper;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 业务流水号生成器
 *
 * 规则: yyyyMMdd-NNN 格式(紧凑日期 + 3 位当日序号,每天重置)
 * 时间基准: 优先取业务传入的日期字符串(如 train_date),
 *           解析失败时回退到 LocalDate.now()(即 create_time 的日期)
 *
 * 并发安全: 通过 MySQL 的 INSERT...ON DUPLICATE KEY UPDATE...LAST_INSERT_ID() 原子自增
 *           (见 BizSerialCounterMapper.incrementAndGet)
 *
 * @author admin
 * @date 2026-06-16
 */
@Component
public class SerialNoGenerator
{
    private static final DateTimeFormatter DATE_KEY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private BizSerialCounterMapper bizSerialCounterMapper;

    /**
     * 生成业务流水号
     *
     * @param bizType       业务类型(如 "biz_train_ledger")
     * @param dateStr       业务日期字符串(train_date 原始值,可能为 yyyy-MM-dd / yyyy-MM-dd HH:mm 等)
     * @return 流水号生成结果(包含 serialNo 与解析后的 LocalDate)
     */
    public SerialNoResult generate(String bizType, String dateStr)
    {
        LocalDate date = parseDate(dateStr);
        if (date == null)
        {
            date = LocalDate.now();
        }
        String dateKey = date.format(DATE_KEY_FMT);

        BizSerialCounter counter = new BizSerialCounter();
        counter.setBizType(bizType);
        counter.setDateKey(dateKey);
        bizSerialCounterMapper.incrementAndGet(counter);
        long seq = counter.getCurrentSeq();

        String serialNo = String.format("%s-%03d", dateKey, seq);
        return new SerialNoResult(serialNo, date);
    }

    private LocalDate parseDate(String dateStr)
    {
        if (dateStr == null || dateStr.trim().isEmpty())
        {
            return null;
        }
        Date parsed = DateUtils.parseDate(dateStr);
        if (parsed == null)
        {
            return null;
        }
        return parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
