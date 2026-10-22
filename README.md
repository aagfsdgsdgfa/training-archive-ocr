# 培训档案云 TrainArchive（企业培训档案与 OCR 考勤台账系统）

![Java](https://img.shields.io/badge/Java-8-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5-green) ![Vue](https://img.shields.io/badge/Vue-2-brightgreen) ![Element UI](https://img.shields.io/badge/Element%20UI-2-blue) ![uni-app](https://img.shields.io/badge/uni--app-3-blue) ![百度OCR](https://img.shields.io/badge/%E7%99%BE%E5%BA%A6OCR-API-2932E1)

企业培训档案管理系统：**培训计划 → 场次执行 → 签到考勤 → OCR 批量识别 → 台账汇总 → 个人档案** 全链路数字化。管理端基于 RuoYi-Vue 二次开发，移动端 uni-app，接入百度 OCR 实现纸质签到表批量数字化。

## 功能亮点

- **OCR 批量考勤数字化**：纸质签到表拍照/扫描批量上传，百度 OCR 自动识别人员与场次信息，防重拦截（ocr_log 拦截重复与无效识别），人工复核修正（ocr_result 备注回填）
- **多场次台账自动汇总**：培训台账总表自动聚合场次/课程/讲师/参训明细，支持按部门、公司多维度模板导出（Excel 模板 + Word 导出）
- **个人培训档案闭环**：每人一档，历史培训、考勤、考核自动归档，可追溯
- **培训全流程管理**：年度计划（部门/公司两级）→ 课程/讲师/地点库 → 场次安排 → 线上考勤与申诉
- **单号序列中心**：biz_serial_counter 统一编号规则，台账/档案编号稳定递增
- **移动端**：uni-app 移动端签到与查询

## 技术栈

| 模块 | 技术 |
|---|---|
| backend | Spring Boot + MyBatis + Druid + Redis + Quartz + EasyExcel（RuoYi-Vue 基线二次开发） |
| frontend | Vue2 + Element UI + Axios（管理端） |
| app | uni-app（移动端） |
| 外部服务 | 百度 OCR 文字识别 |
| 数据库 | MySQL |

## 项目结构

```
├── backend/    # Spring Boot 后端（biz 培训业务模块 + ruoyi-system 基础模块）
├── frontend/   # 管理端 Vue2 工程
├── app/        # 移动端 uni-app 工程
└── sql/        # 数据库脚本（RuoYi 基础表 + 培训业务表 DDL + 菜单/代码生成配置）
```

## 快速开始

### 1. 数据库
按序导入：`sql/ry_20250522.sql`（基础表）→ `sql/biz_train_archive_ddl.sql` 等业务 DDL → `sql/sys_menu.sql`、`sql/gen_table.sql`（菜单与代码生成配置）。历史业务数据已清空，可自行造数体验。

### 2. 后端（端口 8080）
```bash
cd backend
mvn spring-boot:run
```

配置通过环境变量注入（配置文件无明文密钥）：

| 变量 | 用途 |
|---|---|
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 数据库连接（默认 localhost/trainarchive-db） |
| `TOKEN_SECRET` | JWT 签名密钥 |
| `BAIDU_APP_ID` / `BAIDU_API_KEY` / `BAIDU_SECRET_KEY` | 百度 OCR 凭证 |
| `DRUID_PASSWORD` | Druid 监控台密码 |

### 3. 管理端
```bash
cd frontend
npm install
npm run dev
```

### 4. 移动端
HBuilderX 打开 `app/` 目录运行。

## 我做了什么

- 独立完成需求分析、数据库设计（14 张培训业务表）、后端接口、管理端页面与移动端开发
- 设计 OCR 批量识别流水线：上传 → 识别 → 防重拦截 → 人工复核 → 台账归档
- 为客户实际生产环境交付使用，并持续迭代 OCR 识别准确率与导出模板体验
- 基于 RuoYi-Vue（MIT）二次开发，保留上游版权声明

## License

MIT
