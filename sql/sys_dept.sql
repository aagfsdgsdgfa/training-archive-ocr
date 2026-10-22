-- =============================================================
-- 部门信息表 sys_dept
-- 与 sys_user_history / test_history_data_2025 中的 dept_id 对应
-- dept_id=103 研发部门  | dept_id=104 市场部门  | dept_id=105 测试部门
-- =============================================================
DELETE FROM sys_dept WHERE dept_id IN (100, 101, 103, 104, 105);

INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, 0,   '0',           '培训档案云科技',    0, 'admin', '15888888888', 'admin@example.com', '0', '0', 'admin', '2026-03-03 15:50:18', '', NULL),
(101, 100, '0,100',       '总经办',      1, 'admin', '15888888888', 'admin@example.com', '0', '0', 'admin', '2026-03-03 15:50:18', '', NULL),
(103, 101, '0,100,101',   '研发部门',    1, 'admin', '15888888888', 'admin@example.com', '0', '0', 'admin', '2026-03-03 15:50:18', '', NULL),
(104, 101, '0,100,101',   '市场部门',    2, 'admin', '15888888888', 'admin@example.com', '0', '0', 'admin', '2026-03-03 15:50:18', '', NULL),
(105, 101, '0,100,101',   '测试部门',    3, 'ry',    '15666666666', 'demo@example.com',  '0', '0', 'admin', '2026-03-03 15:50:18', '', NULL);
