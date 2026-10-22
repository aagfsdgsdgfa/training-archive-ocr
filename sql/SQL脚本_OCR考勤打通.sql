-- =============================================
-- OCR扫表数据流入考勤审核页 - 数据库改动
-- 执行时间：2026-04-10
-- =============================================

-- 1. attendance 表：标识来源（线上打卡 / OCR扫表）
ALTER TABLE biz_train_attendance
  ADD COLUMN source VARCHAR(10) DEFAULT 'online' COMMENT '来源: online=线上打卡, ocr=OCR扫表' AFTER del_flag;

-- 2. session 表：标识来源
ALTER TABLE biz_train_session
  ADD COLUMN source VARCHAR(10) DEFAULT 'online' COMMENT '来源: online=线上打卡, ocr=OCR扫表' AFTER del_flag;

-- 3. session 表：关联OCR批次ID（仅OCR来源时有值）
ALTER TABLE biz_train_session
  ADD COLUMN batch_id BIGINT DEFAULT NULL COMMENT '关联OCR批次ID(仅OCR来源有效)' AFTER source;

-- 4. 给现有OCR产生的session打上source标记（如果有数据的话）
UPDATE biz_train_session
SET source = 'ocr'
WHERE sign_type = '1' AND (source IS NULL OR source = 'online');

-- 5. 给现有OCR产生的attendance打上source标记（用 JOIN 方式避免子查询限制）
UPDATE biz_train_attendance a
INNER JOIN biz_train_session s ON a.session_id = s.session_id
SET a.source = 'ocr'
WHERE s.sign_type = '1'
  AND (a.source IS NULL OR a.source = 'online');
