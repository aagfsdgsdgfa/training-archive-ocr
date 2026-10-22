-- =============================================================
-- 补充 role_id=1（超级管理员）对所有菜单的授权
-- 原因：sys_role_menu.sql 只有 role_id=2，缺少 role_id=1 的记录
-- 安全：使用 INSERT IGNORE 避免重复键报错
-- 执行时机：恢复 sys_role/sys_user/sys_user_role/sys_menu 之后执行
-- =============================================================

-- 1. 根级系统目录
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 3);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 4);

-- 2. 系统管理子菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 100);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 101);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 102);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 103);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 104);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 105);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 106);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 107);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 108);

-- 3. 系统监控子菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 109);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 110);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 111);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 112);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 113);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 114);

-- 4. 系统工具子菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 115);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 116);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 117);

-- 5. 日志管理子菜单（parent=108）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 500);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 501);

-- 6. 用户管理权限按钮（parent=100）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1000);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1001);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1002);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1003);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1004);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1005);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1006);

-- 7. 角色管理权限按钮（parent=101）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1007);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1008);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1009);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1010);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1011);

-- 8. 菜单管理权限按钮（parent=102）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1012);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1013);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1014);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1015);

-- 9. 部门管理权限按钮（parent=103）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1016);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1017);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1018);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1019);

-- 10. 岗位管理权限按钮（parent=104）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1020);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1021);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1022);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1023);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1024);

-- 11. 字典管理权限按钮（parent=105）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1025);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1026);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1027);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1028);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1029);

-- 12. 参数设置权限按钮（parent=106）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1030);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1031);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1032);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1033);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1034);

-- 13. 通知公告权限按钮（parent=107）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1035);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1036);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1037);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1038);

-- 14. 操作日志权限按钮（parent=500）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1039);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1040);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1041);

-- 15. 登录日志权限按钮（parent=501）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1042);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1043);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1044);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1045);

-- 16. 在线用户权限按钮（parent=109）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1046);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1047);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1048);

-- 17. 定时任务权限按钮（parent=110）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1049);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1050);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1051);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1052);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1053);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1054);

-- 18. 代码生成权限按钮（parent=116）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1055);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1056);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1057);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1058);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1059);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 1060);

-- 19. 业务菜单（2000-2011）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2000);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2001);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2002);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2003);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2004);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2005);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2006);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2007);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2008);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2009);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2010);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2011);

-- =============================================================
-- 验证：查询 role_id=1 的授权数量
-- 期望：>= 82 条（4目录 + 18子菜单 + 61按钮 + 12业务菜单）
-- =============================================================
-- SELECT role_id, COUNT(*) FROM sys_role_menu WHERE role_id = 1 GROUP BY role_id;
