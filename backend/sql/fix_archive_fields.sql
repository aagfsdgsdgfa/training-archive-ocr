-- ============================================================
-- 个人档案字段回填脚本 (fix_archive_fields.sql)
-- ============================================================
-- 适用版本:trainarchive
-- 创建日期:2026-06-22
-- 维护者:admin
--
-- 背景:
--   OCR 批处理路径(BizTrainOcrBatchServiceImpl.processBatch)
--   历史生成的个人档案记录里,train_dept 与 train_location 字段
--   经常为空,因为该路径未在 archive 创建处复制这两个值。
--   本脚本通过 biz_train_ledger 中转,把这两个字段回填回去。
--
-- 依赖:
--   - biz_train_archive.ledger_id 必须有值
--   - biz_train_ledger.host_dept_name / location_name 必须有值
--   - 适用于 MySQL 5.x / 8.x 全版本(多表 UPDATE 语法,无需 JOIN)
--
-- 使用方法:
--   1. 先执行"诊断段",确认触发器/存储过程没有干扰
--   2. 再执行"回填段"
--   3. 最后执行"验证段"确认效果
-- ============================================================


-- ============================================================
-- 第 1 段:诊断(排查 "anage" 语法错)
-- ============================================================
-- 历史问题:用户在 MySQL 客户端执行 UPDATE 时,反复报
--   "near 'anage ... WHERE ...'" 语法错。
-- 经代码扫描确认,项目本身不会注入 "manage" 前缀,
-- 几乎可以确定是数据库触发器或客户端模板侧的问题。
-- 跑下面 4 个查询定位元凶。

-- 1.1 检查当前数据库的 UPDATE 触发器
SELECT TRIGGER_NAME, EVENT_MANIPULATION, EVENT_OBJECT_TABLE, ACTION_STATEMENT
FROM information_schema.TRIGGERS
WHERE EVENT_OBJECT_SCHEMA = DATABASE()
  AND EVENT_MANIPULATION = 'UPDATE';

-- 1.2 检查触发器或存储过程定义里是否包含 "manage"
SELECT TRIGGER_NAME AS object_name, 'TRIGGER' AS object_type, ACTION_STATEMENT AS body
FROM information_schema.TRIGGERS
WHERE EVENT_OBJECT_SCHEMA = DATABASE()
  AND (TRIGGER_NAME LIKE '%manage%' OR ACTION_STATEMENT LIKE '%manage%');

-- 1.3 列出所有存储过程 / 函数
SHOW PROCEDURE STATUS WHERE Db = DATABASE();
SHOW FUNCTION STATUS WHERE Db = DATABASE();

-- 1.4 检查 MySQL 全局 init 配置
SHOW VARIABLES LIKE 'init%';


-- ============================================================
-- 第 2 段:回填 train_dept(主办部门)
-- ============================================================
-- 把 biz_train_archive.train_dept 从
--     biz_train_ledger.host_dept_name
-- 反查补齐(走 ledger 中转,数据稳定)。

-- 2.1 先预览会被更新的行数(只查,不写)
SELECT a.archive_id, a.user_name, a.train_dept AS old_train_dept,
       l.host_dept_name AS new_train_dept, l.ledger_id
FROM biz_train_archive a
INNER JOIN biz_train_ledger l ON a.ledger_id = l.ledger_id
WHERE (a.train_dept IS NULL OR a.train_dept = '')
  AND l.host_dept_name IS NOT NULL
LIMIT 20;

-- 2.2 正式回填(多表 UPDATE 语法,MySQL 5.x/8.x 都支持)
UPDATE biz_train_archive a, biz_train_ledger l
SET a.train_dept = l.host_dept_name
WHERE a.ledger_id = l.ledger_id
  AND (a.train_dept IS NULL OR a.train_dept = '')
  AND l.host_dept_name IS NOT NULL;

-- 2.3 兜底:对 ledger_id 为空的 archive,通过 session 关联
UPDATE biz_train_archive a, biz_train_ledger l, biz_train_session s
SET a.train_dept = s.host_dept_name
WHERE a.ledger_id = l.ledger_id
  AND l.session_id = s.session_id
  AND (a.train_dept IS NULL OR a.train_dept = '')
  AND s.host_dept_name IS NOT NULL;


-- ============================================================
-- 第 3 段:回填 train_location(培训地点)
-- ============================================================
-- 同样的逻辑,把 archive 的 train_location 从 ledger 的 location_name 补齐。

-- 3.1 先预览会被更新的行数
SELECT a.archive_id, a.user_name, a.train_location AS old_train_location,
       l.location_name AS new_train_location, l.ledger_id
FROM biz_train_archive a
INNER JOIN biz_train_ledger l ON a.ledger_id = l.ledger_id
WHERE (a.train_location IS NULL OR a.train_location = '')
  AND l.location_name IS NOT NULL
LIMIT 20;

-- 3.2 正式回填
UPDATE biz_train_archive a, biz_train_ledger l
SET a.train_location = l.location_name
WHERE a.ledger_id = l.ledger_id
  AND (a.train_location IS NULL OR a.train_location = '')
  AND l.location_name IS NOT NULL;

-- 3.3 兜底:通过 session 取
UPDATE biz_train_archive a, biz_train_ledger l, biz_train_session s
SET a.train_location = s.location_name
WHERE a.ledger_id = l.ledger_id
  AND l.session_id = s.session_id
  AND (a.train_location IS NULL OR a.train_location = '')
  AND s.location_name IS NOT NULL;


-- ============================================================
-- 第 4 段:验证回填效果
-- ============================================================

-- 4.1 残余 train_dept 为空的 archive 数(理想为 0)
SELECT COUNT(*) AS train_dept_still_null
FROM biz_train_archive
WHERE train_dept IS NULL OR train_dept = '';

-- 4.2 残余 train_location 为空的 archive 数(理想为 0)
SELECT COUNT(*) AS train_location_still_null
FROM biz_train_archive
WHERE train_location IS NULL OR train_location = '';

-- 4.3 抽样核对最新 10 条
SELECT archive_id, user_name, train_dept, train_location, train_date
FROM biz_train_archive
ORDER BY archive_id DESC
LIMIT 10;

-- 4.4 仍有空值且 ledger_id 不为空,但 ledger 也没有值的(需要手工补录)
SELECT a.archive_id, a.user_name, a.ledger_id, a.train_dept, a.train_location
FROM biz_train_archive a
WHERE (a.train_dept IS NULL OR a.train_dept = ''
       OR a.train_location IS NULL OR a.train_location = '')
  AND a.ledger_id IS NOT NULL
LIMIT 20;


-- ============================================================
-- 第 5 段(可选):单行 SQL 兜底,如果 UPDATE 还是报 anage
-- ============================================================
-- 把多行压成单行,绕开某些客户端对换行的特殊处理。

-- 单行版 train_dept 回填
UPDATE biz_train_archive a, biz_train_ledger l SET a.train_dept = l.host_dept_name WHERE a.ledger_id = l.ledger_id AND (a.train_dept IS NULL OR a.train_dept = '') AND l.host_dept_name IS NOT NULL;

-- 单行版 train_location 回填
UPDATE biz_train_archive a, biz_train_ledger l SET a.train_location = l.location_name WHERE a.ledger_id = l.ledger_id AND (a.train_location IS NULL OR a.train_location = '') AND l.location_name IS NOT NULL;


-- ============================================================
-- 附录:若以上仍报 "anage" 语法错,极可能是 BEFORE UPDATE 触发器在作怪
-- ============================================================
-- 临时禁用可疑触发器(注意先备份定义):
--   SHOW CREATE TRIGGER 触发器名;     -- 备份定义
--   DROP TRIGGER IF EXISTS 触发器名;    -- 临时删除
--   -- 重新执行回填 SQL
--   -- 跑完后用备份的定义恢复
--
-- 若想走 Java/MyBatis 完全绕开 MySQL 客户端:
-- 在 BizTrainArchiveController 新增:
--   @GetMapping("/backfillArchiveFields")
--   public AjaxResult backfill() {
--       int r1 = archiveMapper.backfillTrainDeptFromLedger();
--       int r2 = archiveMapper.backfillTrainLocationFromLedger();
--       return AjaxResult.success("train_dept=" + r1 + ", train_location=" + r2);
--   }
-- 在 BizTrainArchiveMapper.xml 新增:
--   <update id="backfillTrainDeptFromLedger">
--       UPDATE biz_train_archive, biz_train_ledger
--       SET biz_train_archive.train_dept = biz_train_ledger.host_dept_name
--       WHERE biz_train_archive.ledger_id = biz_train_ledger.ledger_id
--         AND (biz_train_archive.train_dept IS NULL OR biz_train_archive.train_dept = '')
--         AND biz_train_ledger.host_dept_name IS NOT NULL
--   </update>
--   <update id="backfillTrainLocationFromLedger">
--       UPDATE biz_train_archive, biz_train_ledger
--       SET biz_train_archive.train_location = biz_train_ledger.location_name
--       WHERE biz_train_archive.ledger_id = biz_train_ledger.ledger_id
--         AND (biz_train_archive.train_location IS NULL OR biz_train_archive.train_location = '')
--         AND biz_train_ledger.location_name IS NOT NULL
--   </update>
-- ============================================================