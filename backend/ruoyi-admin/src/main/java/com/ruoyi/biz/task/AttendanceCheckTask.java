package com.ruoyi.biz.task;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysUserService;

import com.ruoyi.biz.domain.BizTrainSession;
import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.domain.BizTrainAttendance;
import com.ruoyi.biz.service.IBizTrainSessionService;
import com.ruoyi.biz.service.IBizTrainPlanService;
import com.ruoyi.biz.service.IBizTrainAttendanceService;

/**
 * 考勤每日自动收尾/结算定时任务
 */
@Component("attendanceCheckTask")
public class AttendanceCheckTask {

    private static final Logger log = LoggerFactory.getLogger(AttendanceCheckTask.class);

    @Autowired
    private IBizTrainSessionService bizTrainSessionService;

    @Autowired
    private IBizTrainPlanService bizTrainPlanService;

    @Autowired
    private IBizTrainAttendanceService bizTrainAttendanceService;

    @Autowired
    private ISysUserService userService;

    /**
     * 自动扫描已结束的培训，为未打卡人员强行补齐“缺卡”记录
     * 建议执行频率：每天晚上 23:30
     */
    public void closeFinishedSessions() {
        log.info("========== 开始执行【培训考勤每日收尾结算】定时任务 ==========");

        // 1. 查询当前所有状态为 "1" (进行中) 的培训场次
        BizTrainSession querySession = new BizTrainSession();
        querySession.setStatus("1");
        List<BizTrainSession> activeSessions = bizTrainSessionService.selectBizTrainSessionList(querySession);

        Date now = DateUtils.getNowDate();
        int processedCount = 0;

        // 2. 遍历判断哪些培训已经到了结束时间
        for (BizTrainSession session : activeSessions) {
            if (session.getEndTime() != null && session.getEndTime().before(now)) {
                log.info("发现已结束的培训场次 ID: {}, 正在进行缺卡核算...", session.getSessionId());

                // 核心：处理该场次的缺卡逻辑
                processMissingAttendance(session);

                // 结算完成后，把场次状态扭转为 "2" (待审归档)
                session.setStatus("2");
                session.setUpdateBy("admin");
                bizTrainSessionService.updateBizTrainSession(session);

                processedCount++;
            }
        }

        log.info("========== 考勤收尾结算完毕，本次共自动结算了 {} 场培训 ==========", processedCount);
    }

    /**
     * 对比应到名单与实到打卡名单，批量标记缺席人员
     */
    private void processMissingAttendance(BizTrainSession session) {
        if (session.getPlanId() == null) return;

        // 获取源头年度计划
        BizTrainPlan plan = bizTrainPlanService.selectBizTrainPlanByPlanId(session.getPlanId());
        if (plan == null) return;

        // 1. 根据 audience_scope 解析【应到人员 ID 集合】
        Set<Long> expectedUserIds = new HashSet<>();
        String scope = plan.getAudienceScope();
        String audIds = plan.getAudienceIds();

        if ("0".equals(scope)) {
            // 受众 0=全体员工
            SysUser uQuery = new SysUser();
            uQuery.setStatus("0");
            List<SysUser> allUsers = userService.selectUserList(uQuery);
            allUsers.forEach(u -> expectedUserIds.add(u.getUserId()));

        } else if ("1".equals(scope)) {
            // 受众 1=部门全员，audience_ids 是 dept_id 列表（逗号分隔）
            if (StringUtils.isEmpty(audIds)) {
                log.warn("计划 {} 的 audience_scope=1 但 audience_ids 为空，跳过应到人员计算", plan.getPlanId());
            } else {
                String[] deptIds = audIds.split(",");
                for (String did : deptIds) {
                    SysUser uQuery = new SysUser();
                    uQuery.setDeptId(Long.valueOf(did.trim()));
                    uQuery.setStatus("0");
                    List<SysUser> deptUsers = userService.selectUserList(uQuery);
                    deptUsers.forEach(u -> expectedUserIds.add(u.getUserId()));
                }
            }

        } else if ("2".equals(scope)) {
            // 受众 2=指定人员，audience_ids 是 user_id 列表（逗号分隔）
            if (StringUtils.isEmpty(audIds)) {
                log.warn("计划 {} 的 audience_scope=2 但 audience_ids 为空，跳过应到人员计算", plan.getPlanId());
            } else {
                String[] userIds = audIds.split(",");
                for (String uid : userIds) {
                    expectedUserIds.add(Long.valueOf(uid.trim()));
                }
            }
        } else {
            log.warn("计划 {} 的 audience_scope={} 非预期值，跳过应到人员计算", plan.getPlanId(), scope);
        }

        // 2. 获取【实到人员 ID 集合】(查询这张场次现有的考勤记录)
        BizTrainAttendance attQuery = new BizTrainAttendance();
        attQuery.setSessionId(session.getSessionId());
        List<BizTrainAttendance> actualAtts = bizTrainAttendanceService.selectBizTrainAttendanceList(attQuery);
        Set<Long> actualUserIds = actualAtts.stream().map(BizTrainAttendance::getUserId).collect(Collectors.toSet());

        // 3. 对比并插入缺席人员记录
        int missingCount = 0;
        for (Long expectedId : expectedUserIds) {
            // 如果应到人员没在实到名单里
            if (!actualUserIds.contains(expectedId)) {
                BizTrainAttendance missingRecord = new BizTrainAttendance();
                missingRecord.setSessionId(session.getSessionId());
                missingRecord.setUserId(expectedId);
                missingRecord.setSignStatus("2"); // 2-代表缺卡
                missingRecord.setAppealStatus("0"); // 0-无申诉，待员工自己去发起
                missingRecord.setCreateBy("admin"); // 系统自动创建
                missingRecord.setRemark("系统每日定时核算判定：缺席未打卡");

                bizTrainAttendanceService.insertBizTrainAttendance(missingRecord);
                missingCount++;
            }
        }

        log.info("该场次核算结束：应到 {} 人，实到 {} 人，系统强行标记缺卡 {} 人。",
                expectedUserIds.size(), actualUserIds.size(), missingCount);
    }
}