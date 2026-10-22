# Backend 专属规范

> **声明：本文件不记录具体技术栈细节。处理具体任务时，必须优先去读取对应子目录下的 CLAUDE.md 专属规范。**

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | RuoYi-Vue（Spring Boot + MyBatis + Vue） |
| 语言 | Java 8+ |
| 数据库 | MySQL |
| 缓存 | Redis |
| 定时任务 | Quartz |

---

## 项目结构

```
backend/
├── ruoyi-admin/           # 主应用（控制器、服务、Mapper）
│   └── src/main/java/com/ruoyi/biz/
│       ├── controller/     # 业务控制器
│       │   ├── BizTrainPlanController.java      # 年度培训计划
│       │   ├── BizTrainSessionController.java  # 培训执行场次
│       │   ├── BizTrainAttendanceController.java # 线上考勤与申诉
│       │   ├── BizTrainArchiveController.java   # 个人培训档案
│       │   └── BizTrainLedgerController.java    # 培训台账总表
│       ├── service/impl/  # 服务实现
│       │   └── BizTrainSessionServiceImpl.java  # 归档核心逻辑
│       └── mapper/        # 数据访问层
├── ruoyi-common/          # 公共模块（注解、工具类）
│   └── annotation/
│       ├── DataScope.java # 数据权限注解
│       ├── Log.java       # 操作日志注解
│       └── Excel.java     # Excel导出注解
├── ruoyi-framework/       # 框架模块（切面）
│   └── aspectj/
│       └── DataScopeAspect.java # 数据权限切面
├── ruoyi-system/          # 系统模块（用户、部门、角色）
└── sql/                  # 数据库SQL脚本
```

---

## 规范与约定

### 1. 注解使用规范

#### @DataScope（数据权限）
- **位置**：Controller 的 list() 方法
- **参数**：`deptAlias`（部门表别名）、`userAlias`（用户表别名）
- **示例**：
```java
@DataScope(deptAlias = "p", userAlias = "p")
@GetMapping("/list")
public TableDataInfo list(BizTrainPlan bizTrainPlan)
```

#### @Log（操作日志）
- **位置**：Controller 类级 + 方法级
- **参数**：title（模块名）、businessType（操作类型）
- **类级注解**：
```java
@Log(title = "年度培训计划", businessType = BusinessType.OTHER, operatorType = OperatorType.MANAGE)
```

### 2. 归档逻辑规范（重要）

**文件**：`BizTrainSessionServiceImpl.archiveSession()`

**正确顺序**：
1. 先收集合格人员名单
2. **先插入台账**（获取 ledgerId）
3. **后插入档案**（关联 ledgerId）
4. 更新场次状态
5. 更新计划状态（如全部场次已归档）

**错误示例**（已修复）：
```java
// 错误：先插档案，后插台账，导致 ledgerId 为 NULL
for (BizTrainAttendance att : allAtts) {
    bizTrainArchiveMapper.insertBizTrainArchive(archive); // ledgerId=null
}
bizTrainLedgerMapper.insertBizTrainLedger(ledger);
```

### 3. 状态流转规则

| 状态值 | 名称 | 允许转换 |
|--------|------|----------|
| 0 | 未开始 | → 1（进行中） |
| 1 | 进行中 | → 2（待审归档） |
| 2 | 待审归档 | → 1（进行中）或 3（已归档） |
| 3 | 已归档 | 禁止任何修改 |

### 4. 服务层校验

`updateBizTrainSession()` 方法已实现：
- 已归档场次禁止修改
- 状态转换规则校验

---

## 已完成任务

### PLAN.md 任务完成情况

| 任务 | 状态 | 备注 |
|------|------|------|
| 1. 修复档案-台账关联 | ✅ 已完成 | 调整插入顺序，先台账后档案 |
| 2. 计划状态自动更新 | ✅ 已完成 | 全部场次归档后计划变为"已完成" |
| 3. 人数统计动态更新 | ⏳ 待确认 | 归档时已实时统计，申诉后更新需确认业务需求 |
| 4. 状态转换校验 | ✅ 已完成 | 已归档禁止修改，状态流转规则校验 |
| 5. @DataScope 注解 | ✅ 已完成 | 5个业务控制器已添加 |
| 6. @Log 类级注解 | ✅ 已完成 | 5个业务控制器已添加 |
| 7. @Excel 注解 | ✅ 已完善 | 实体类已有完整注解，无需修改 |

---

## 开发注意事项

1. **归档操作具有事务性**：`archiveSession()` 使用 `@Transactional` 保证数据一致性
2. **@DataScope 生效条件**：Mapper XML 中 SQL 需使用对应表别名
3. **状态修改前**：必须先查询数据库当前状态，再进行校验
4. **台账ID生成**：通过 `useGeneratedKeys="true" keyProperty="ledgerId"` 自增生成

---

## 关键文件索引

| 功能 | 文件路径 |
|------|----------|
| 归档核心逻辑 | `ruoyi-admin/.../service/impl/BizTrainSessionServiceImpl.java` |
| 数据权限切面 | `ruoyi-framework/.../aspectj/DataScopeAspect.java` |
| 日志注解定义 | `ruoyi-common/.../annotation/Log.java` |
| 数据权限注解 | `ruoyi-common/.../annotation/DataScope.java` |
| Excel注解定义 | `ruoyi-common/.../annotation/Excel.java` |
