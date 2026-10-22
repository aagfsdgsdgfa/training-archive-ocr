-- ============================================================
-- 台账 project_category NULL/空 回填脚本 (fix_ledger_null_category.sql)
-- ============================================================
-- 适用版本:trainarchive
-- 创建日期:2026-06-30
-- 维护者:admin
--
-- 背景:
--   首页「台账业务分布」饼图 3 条台账全部聚成一格。
--   根因:历史台账的 project_category 是 NULL 或空字符串,
--        归档链路 OCR 批处理路径里只在 plan != null 时设字段,
--        计划为空时直接漏写;BizTrainSessionServiceImpl 兜底写成 ""。
--   前端兜底 "未分类" 虽然不撒谎,但仍只显示 1 个分类,
--   用户体感「看不到分布」。
--
-- 方案:
--   把 NULL/空 的 project_category 一次性回填为归档链路兜底值
--   「常规业务培训」(与 BizTrainAttendanceController L247 与
--   BizTrainOcrBatchServiceImpl L307-308 保持一致)。
--
-- 依赖:
--   - 仅 biz_train_ledger 表,无需 JOIN
--   - 适用于 MySQL 5.x / 8.x
--
-- 使用方法:
--   1. 执行"诊断段"看会被影响的行数
--   2. 执行"回填段"
--   3. 执行"验证段"确认
-- ============================================================


-- ============================================================
-- 第 1 段:诊断(只读)
-- ============================================================

-- 1.1 当前还有多少条 ledger 的 project_category 为空(NULL 或空字符串)
SELECT COUNT(*) AS null_or_empty_count
FROM biz_train_ledger
WHERE project_category IS NULL OR TRIM(project_category) = '';

-- 1.2 抽样列出这些 ledger(确认是否就是首页那 3 条)
SELECT ledger_id, subject, host_dept_name, project_category, train_date
FROM biz_train_ledger
WHERE project_category IS NULL OR TRIM(project_category) = ''
LIMIT 20;


-- ============================================================
-- 第 2 段:回填 project_category
-- ============================================================

UPDATE biz_train_ledger
SET project_category = '常规业务培训'
WHERE project_category IS NULL OR TRIM(project_category) = '';


-- ============================================================
-- 第 3 段:验证回填效果
-- ============================================================

-- 3.1 残余 NULL/空 的 ledger 数(理想为 0)
SELECT COUNT(*) AS still_null_count
FROM biz_train_ledger
WHERE project_category IS NULL OR TRIM(project_category) = '';

-- 3.2 首页按项目分类维度聚合(理想:三类别的累计为总台账数)
SELECT project_category, COUNT(*) AS cnt
FROM biz_train_ledger
GROUP BY project_category
ORDER BY cnt DESC;

-- 3.3 抽样核对最新 10 条
SELECT ledger_id, subject, project_category, train_date
FROM biz_train_ledger
ORDER BY ledger_id DESC
LIMIT 10;
