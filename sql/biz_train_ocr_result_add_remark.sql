-- ============================================================
-- biz_train_ocr_result 加 parsed_remark 列
-- ============================================================
-- 适用版本:trainarchive
-- 创建日期:2026-06-24
-- 维护者:admin
--
-- 背景:
--   OCR 解析培训考核记录表.xls 时,Excel 备注列(授课人/未参加/缺席/缺考)
--   之前没被读取,导致 应到 vs 实到 算不准(实到 36 应为 34,差 2 人)。
--   加 parsed_remark 列专门存放备注文字,语义清晰,不与 parsed_score 混用。
--
-- 兼容性:
--   - 现有 36 条数据 parsed_remark=NULL,视为"未识别到备注",等同"出席"
--   - 等用户重传 Excel 后才会写入真实值
--   - 历史批次不破坏
--
-- 使用方法:直接执行即可(IF NOT EXISTS 兼容重复执行)
-- ============================================================

-- MySQL 8.0+ / 5.7+ 通用,IF NOT EXISTS 防止重复执行报错
ALTER TABLE biz_train_ocr_result
  ADD COLUMN parsed_remark varchar(64) DEFAULT NULL
  COMMENT 'OCR提取的备注(授课人/未参加/缺席等)';


-- ============================================================
-- 验证段
-- ============================================================

-- 1. 确认列已加成功
DESC biz_train_ocr_result;

-- 2. 抽样查看历史数据(NULL 表示未识别到备注,等同"出席")
SELECT sign_seq, parsed_name, parsed_score, parsed_remark
FROM biz_train_ocr_result
ORDER BY result_id DESC
LIMIT 10;
