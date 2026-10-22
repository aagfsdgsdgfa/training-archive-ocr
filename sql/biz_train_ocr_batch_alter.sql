-- ============================================================
-- 培训考核OCR批次表：补齐应到/实到人数字段
-- 日期：2026-06-16
-- 说明：
--   1) ocr_target_count 应到人数：从Excel顶部元数据区"应培训人数/应到人数"提取
--   2) ocr_actual_count 实到人数：在确认归档时根据明细实际记录数写入
-- 历史数据该两列为 NULL，无需回填
-- ============================================================

ALTER TABLE `biz_train_ocr_batch`
  ADD COLUMN `ocr_target_count` INT DEFAULT NULL COMMENT '应到人数(从Excel提取)',
  ADD COLUMN `ocr_actual_count` INT DEFAULT NULL COMMENT '实到人数(扫描明细统计)';
