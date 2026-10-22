-- ============================================================
-- 业务流水号计数器表（按 biz_type + date_key 原子自增）
-- 用于培训台账流水号生成(yyyyMMdd-NNN)
-- 通过 INSERT...ON DUPLICATE KEY UPDATE...LAST_INSERT_ID(...) 模式
-- 保证并发安全,无需 Redis 或额外锁
-- ============================================================
DROP TABLE IF EXISTS `biz_serial_counter`;
CREATE TABLE `biz_serial_counter` (
  `biz_type` VARCHAR(50) NOT NULL COMMENT '业务类型(预留多业务共用)',
  `date_key` VARCHAR(20) NOT NULL COMMENT '日期键 yyyyMMdd',
  `current_seq` BIGINT NOT NULL DEFAULT 0 COMMENT '当前序号',
  PRIMARY KEY (`biz_type`, `date_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务流水号计数器';
