-- =============================================================
-- 个人培训档案明细表 biz_train_archive
-- 根据 BizTrainArchive.java + BizTrainArchiveMapper.xml + 现有 INSERT 数据反推
-- 适用：MySQL 5.7 / 8.x
-- 使用：先执行本脚本建表，再执行 sql/biz_train_archive.sql 导入数据
-- =============================================================

DROP TABLE IF EXISTS `biz_train_archive`;

CREATE TABLE `biz_train_archive` (
  `archive_id`        bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '档案记录ID',
  `user_id`           bigint(20)   DEFAULT NULL              COMMENT '系统用户ID（导入历史老数据可为空）',
  `dept_id`           bigint(20)   DEFAULT NULL              COMMENT '系统部门ID（历史老数据可为空）',
  `user_name`         varchar(64)  DEFAULT ''                COMMENT '姓名（强冗余，防止员工离职丢失历史档案）',
  `dept_name`         varchar(128) DEFAULT ''                COMMENT '部门（强冗余，防止部门改名或重组丢失信息）',
  `plan_id`           bigint(20)   DEFAULT NULL              COMMENT '关联年度计划ID',
  `ledger_id`         bigint(20)   DEFAULT NULL              COMMENT '关联台账总表ID',
  `train_date`        varchar(64)  DEFAULT NULL              COMMENT '培训日期（兼顾历史长文本跨日格式）',
  `train_dept`        varchar(128) DEFAULT NULL              COMMENT '培训部门',
  `train_location`    varchar(128) DEFAULT NULL              COMMENT '培训地点',
  `subject`           varchar(255) DEFAULT NULL              COMMENT '培训主题',
  `train_content`     text         DEFAULT NULL              COMMENT '具体的培训内容',
  `project_category`  varchar(64)  DEFAULT NULL              COMMENT '项目分类（从年度计划继承）',
  `sign_seq`          varchar(32)  DEFAULT NULL              COMMENT '签到序号（复刻旧版历史档案预留）',
  `speaker`           varchar(64)  DEFAULT NULL              COMMENT '授课人',
  `score`             varchar(32)  DEFAULT NULL              COMMENT '成绩（文本：98.0 / 合格 / 未考核）',
  `train_hours`       varchar(16)  DEFAULT NULL              COMMENT '培训学时',
  `job_change`        varchar(64)  DEFAULT NULL              COMMENT '岗位变化（旧版档案结构预留）',
  `del_flag`          char(1)      DEFAULT '0'               COMMENT '删除标志（0存在 2删除）',
  `create_by`         varchar(64)  DEFAULT ''                COMMENT '创建者',
  `create_time`       datetime     DEFAULT NULL              COMMENT '创建时间',
  `update_by`         varchar(64)  DEFAULT ''                COMMENT '更新者',
  `update_time`       datetime     DEFAULT NULL              COMMENT '更新时间',
  `remark`            varchar(500) DEFAULT NULL              COMMENT '备注',
  PRIMARY KEY (`archive_id`) USING BTREE,
  KEY `idx_user_id`    (`user_id`),
  KEY `idx_dept_id`    (`dept_id`),
  KEY `idx_plan_id`    (`plan_id`),
  KEY `idx_ledger_id`  (`ledger_id`),
  KEY `idx_train_date` (`train_date`),
  KEY `idx_del_flag`   (`del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人培训档案明细';
