-- =============================================================
-- biz_train_plan 增加 audience_text 字段
-- 用途：保留 Excel 里的"培训对象"原始文本（含多部门+角色筛选等复杂语义）
-- audience_scope 仍为 0/1/2 桶（保证 AttendanceCheckTask 兼容）
-- =============================================================

ALTER TABLE `biz_train_plan`
    ADD COLUMN `audience_text` TEXT NULL COMMENT '受众原文（保留 Excel 原始培训对象文本）' AFTER `audience_ids`;

-- 若 RuoYi 代码生成器表（gen_table_column）有元数据登记，可选同步：
-- UPDATE gen_table_column
--    SET column_comment = '受众原文（保留 Excel 原始培训对象文本）',
--        column_type = 'text',
--        is_list = 1, is_insert = 1, is_edit = 1
--  WHERE column_name = 'audience_text' AND table_id = 5;
