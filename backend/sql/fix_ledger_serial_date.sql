-- =============================================================
-- biz_train_ledger.serial_date 一次性回填
-- =============================================================
-- 背景:
--   2026-07-06 修复:BizTrainLedgerServiceImpl.updateBizTrainLedger
--   在人工校正 train_date 后,同步更新 serial_date,避免列表
--   ORDER BY serial_date desc 排序错位。
--
--   修复只对未来 edit 生效。已经被人手工校正过、但 serial_date
--   还停在旧值的那批存量行,需要用本脚本一次性回填。
--
-- 执行时机:
--   部署了上述 service 修复 + 重启后端 之后,执行一次。
--   之后再跑也是 no-op(已对的行不会被改)。
--
-- 影响的行:
--   1. 被人工校正过 train_date、serial_date 没同步的(本次 bug 的主目标)
--   2. 通过 POST /biz/attendance/archiveSession 路径写入、serial_date 本就 NULL 的
--      (这条路径 BizTrainAttendanceController 已补,后续新建会自带 serial_date;
--       旧的 NULL 行被本脚本一起修掉)
--
-- 执行方式:
--   mysql -h 192.168.1.234 -u xd-trainarchive-db -p xd-trainarchive-db < fix_ledger_serial_date.sql
--   或者在 Navicat / DBeaver 里直接执行 UPDATE 段。
--
-- 预期结果:
--   - 客户之前手动校正到 2026-04-13 的那条:serial_date 从 2026-07-01 → 2026-04-13
--   - 列表里 4 月那条立即从位置 1 跳到 6 月那条之后
-- =============================================================


-- ---------------------------------------------------------------
-- 1. 主回填:解析 train_date → serial_date
-- ---------------------------------------------------------------
--   单值 "yyyy-MM-dd"                            → 取该日
--   区间 "yyyy-MM-dd — yyyy-MM-dd"               → 取起始日
--   其它(无法解析 / train_date 为 NULL)            → 保留 serial_date 原值(无副作用)

UPDATE biz_train_ledger
SET serial_date = CASE
        WHEN train_date REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$' THEN
            STR_TO_DATE(train_date, '%Y-%m-%d')
        WHEN train_date LIKE '%—%' THEN
            STR_TO_DATE(
                TRIM(SUBSTRING(train_date, 1, LOCATE('—', train_date) - 1)),
                '%Y-%m-%d'
            )
        ELSE serial_date
    END
WHERE 1 = 1;


-- ---------------------------------------------------------------
-- 2. 验证:最近被人改过 train_date 的行,train_date 和 serial_date 应该一致
-- ---------------------------------------------------------------
-- SELECT
--     ledger_id,
--     train_date,
--     serial_date,
--     update_time
-- FROM biz_train_ledger
-- WHERE update_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
-- ORDER BY update_time DESC
-- LIMIT 30;
--
-- 期望:每行的 train_date 起始日 == serial_date。


-- ---------------------------------------------------------------
-- 3. 验证:列表顺序(模拟后端 ORDER BY serial_date desc, serial_no desc)
-- ---------------------------------------------------------------
-- SELECT
--     ledger_id,
--     train_date,
--     serial_date,
--     serial_no
-- FROM biz_train_ledger
-- WHERE del_flag = '0'
-- ORDER BY serial_date DESC, serial_no DESC
-- LIMIT 30;
--
-- 期望:第一行是 serial_date 最大的那条;之前"4 月那条排在 6 月之前"的怪现象消失。