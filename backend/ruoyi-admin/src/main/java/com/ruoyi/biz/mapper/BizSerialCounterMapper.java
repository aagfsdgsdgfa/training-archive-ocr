package com.ruoyi.biz.mapper;

import com.ruoyi.biz.domain.BizSerialCounter;
import org.apache.ibatis.annotations.Param;

/**
 * 业务流水号计数器Mapper
 *
 * @author admin
 * @date 2026-06-16
 */
public interface BizSerialCounterMapper
{
    /**
     * 原子自增并返回新序号。
     * 通过 MySQL 的 INSERT...ON DUPLICATE KEY UPDATE...LAST_INSERT_ID(...) 模式实现并发安全。
     * 调用方后续通过 SELECT LAST_INSERT_ID() 读取新序号(同连接 session)。
     *
     * @param bizSerialCounter 业务类型 + 日期键
     * @return 影响行数(1=新插入, 2=更新)
     */
    int incrementAndGet(BizSerialCounter bizSerialCounter);
}
