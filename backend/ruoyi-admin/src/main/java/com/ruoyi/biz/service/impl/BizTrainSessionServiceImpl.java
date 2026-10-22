package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.biz.util.SerialNoGenerator;
import com.ruoyi.biz.util.SerialNoResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainSessionMapper;
import com.ruoyi.biz.domain.BizTrainSession;
import com.ruoyi.biz.service.IBizTrainSessionService;

/**
 * 培训执行场次Service业务层处理
 * 
 * @author admin
 * @date 2026-03-03
 */
@Service
public class BizTrainSessionServiceImpl implements IBizTrainSessionService 
{
    @Autowired
    private BizTrainSessionMapper bizTrainSessionMapper;


    @Autowired
    private com.ruoyi.biz.mapper.BizTrainLedgerMapper bizTrainLedgerMapper;
    @Autowired
    private com.ruoyi.biz.mapper.BizTrainArchiveMapper bizTrainArchiveMapper;
    @Autowired
    private com.ruoyi.biz.mapper.BizTrainPlanMapper bizTrainPlanMapper;
    @Autowired
    private com.ruoyi.biz.mapper.BizTrainAttendanceMapper bizTrainAttendanceMapper;
    @Autowired
    private com.ruoyi.system.mapper.SysUserMapper sysUserMapper;

    @Autowired
    private SerialNoGenerator serialNoGenerator;

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class) // 开启强事务保证一致性
    public void archiveSession(Long sessionId, String operName) {
        // 1. 校验场次状态
        BizTrainSession session = bizTrainSessionMapper.selectBizTrainSessionBySessionId(sessionId);
        if (session == null || !"2".equals(session.getStatus())) {
            throw new com.ruoyi.common.exception.ServiceException("只有处于【待审归档】状态的培训场次才能进行归档操作！");
        }

        // 获取关联计划信息
        com.ruoyi.biz.domain.BizTrainPlan plan = null;
        if (session.getPlanId() != null) {
            plan = bizTrainPlanMapper.selectBizTrainPlanByPlanId(session.getPlanId());
        }

        // 2. 先收集合格人员名单（此处以线上考勤正常打卡 0 和 迟到 1 为例，缺卡 2 不计入档案）
        com.ruoyi.biz.domain.BizTrainAttendance attQuery = new com.ruoyi.biz.domain.BizTrainAttendance();
        attQuery.setSessionId(sessionId);
        List<com.ruoyi.biz.domain.BizTrainAttendance> allAtts = bizTrainAttendanceMapper.selectBizTrainAttendanceList(attQuery);

        List<com.ruoyi.biz.domain.BizTrainAttendance> actualAttendees = new java.util.ArrayList<>();
        StringBuilder attendeeNames = new StringBuilder();

        for (com.ruoyi.biz.domain.BizTrainAttendance att : allAtts) {
            if ("0".equals(att.getSignStatus()) || "1".equals(att.getSignStatus())) {
                actualAttendees.add(att);
                com.ruoyi.common.core.domain.entity.SysUser user = sysUserMapper.selectUserById(att.getUserId());
                if (user != null) {
                    attendeeNames.append(user.getNickName()).append(",");
                }
            }
        }

        if (attendeeNames.length() > 0) {
            attendeeNames.deleteCharAt(attendeeNames.length() - 1);
        }

        // 3. 先生成【培训台账总表】（获取 ledgerId）
        com.ruoyi.biz.domain.BizTrainLedger ledger = new com.ruoyi.biz.domain.BizTrainLedger();
        ledger.setPlanId(session.getPlanId());
        ledger.setSessionId(sessionId);
        ledger.setHostDeptName(session.getHostDeptName());
        ledger.setProjectCategory(plan != null ? plan.getProjectCategory() : "");
        ledger.setSubject(session.getSubject());
        if (session.getStartTime() != null) {
            ledger.setTrainDate(com.ruoyi.common.utils.DateUtils.parseDateToStr("yyyy-MM-dd HH:mm", session.getStartTime()));
        }
        ledger.setTrainHours(session.getTrainHours() != null ? session.getTrainHours().toString() : "0");
        ledger.setSpeaker(session.getSpeaker());
        ledger.setLocationName(session.getLocationName());
        ledger.setAttendeeNames(attendeeNames.toString());
        ledger.setActualCount(actualAttendees.size());

        // 解析应到人数，如果有计划则按需统计，没有则等于实到人数
        ledger.setTargetCount(plan != null && com.ruoyi.common.utils.StringUtils.isNotEmpty(plan.getAudienceIds())
                ? plan.getAudienceIds().split(",").length : actualAttendees.size());
        ledger.setPlanType(plan != null ? (plan.getPlanType().equals("1") ? "计划内" : "计划外") : "计划外");
        ledger.setTrainLevel(plan != null ? plan.getTrainLevel() : "");
        ledger.setCreateBy(operName);
        ledger.setCreateTime(com.ruoyi.common.utils.DateUtils.getNowDate());
        // 归档路径不走 Service 层,需手动生成业务流水号
        SerialNoResult sn = serialNoGenerator.generate("biz_train_ledger", ledger.getTrainDate());
        ledger.setSerialNo(sn.getSerialNo());
        ledger.setSerialDate(sn.getSerialDate());
        bizTrainLedgerMapper.insertBizTrainLedger(ledger);

        // 4. 遍历合格人员，生成【个人培训档案】（使用已生成的 ledgerId）
        for (com.ruoyi.biz.domain.BizTrainAttendance att : actualAttendees) {
            com.ruoyi.common.core.domain.entity.SysUser user = sysUserMapper.selectUserById(att.getUserId());
            if (user != null) {
                com.ruoyi.biz.domain.BizTrainArchive archive = new com.ruoyi.biz.domain.BizTrainArchive();
                archive.setUserId(user.getUserId());
                archive.setUserName(user.getNickName());
                archive.setDeptId(user.getDeptId());
                archive.setDeptName(user.getDept() != null ? user.getDept().getDeptName() : "");
                archive.setPlanId(session.getPlanId());
                archive.setLedgerId(ledger.getLedgerId());  // 【修复】关联台账ID
                archive.setProjectCategory(plan != null ? plan.getProjectCategory() : "");
                if (session.getStartTime() != null) {
                    archive.setTrainDate(com.ruoyi.common.utils.DateUtils.parseDateToStr("yyyy-MM-dd", session.getStartTime()));
                }
                archive.setTrainDept(session.getHostDeptName());
                archive.setTrainLocation(session.getLocationName());
                archive.setSubject(session.getSubject());
                archive.setSpeaker(session.getSpeaker());
                archive.setTrainHours(session.getTrainHours() != null ? session.getTrainHours().toString() : "0");
                archive.setSignSeq(att.getSignSeq());  // 复制签到序号
                // 从考勤记录复制成绩，如果不存在则默认为"合格"
                if (StringUtils.isNotEmpty(att.getScore())) {
                    archive.setScore(att.getScore());
                } else {
                    archive.setScore("合格");
                }
                archive.setCreateBy(operName);
                archive.setCreateTime(com.ruoyi.common.utils.DateUtils.getNowDate());
                bizTrainArchiveMapper.insertBizTrainArchive(archive);
            }
        }

        // 5. 更新场次状态为 3（已归档）
        session.setStatus("3");
        session.setUpdateBy(operName);
        session.setUpdateTime(com.ruoyi.common.utils.DateUtils.getNowDate());
        bizTrainSessionMapper.updateBizTrainSession(session);

        // 6. 更新关联计划的完成状态（当所有场次都归档后，计划自动变为已完成）
        if (plan != null) {
            checkAndMarkPlanCompleted(plan.getPlanId(), operName);
        }
    }

    @Override
    public void checkAndMarkPlanCompleted(Long planId, String operName) {
        if (planId == null) return;
        com.ruoyi.biz.domain.BizTrainPlan plan = bizTrainPlanMapper.selectBizTrainPlanByPlanId(planId);
        if (plan == null) return;
        // 已完成 / 已取消的计划不要再覆盖（防止 OCR 二次结算把已取消的复活）
        if (!"0".equals(plan.getStatus())) return;

        BizTrainSession querySession = new BizTrainSession();
        querySession.setPlanId(planId);
        List<BizTrainSession> relatedSessions = bizTrainSessionMapper.selectBizTrainSessionList(querySession);
        if (relatedSessions.isEmpty()) return;
        boolean allArchived = relatedSessions.stream().allMatch(s -> "3".equals(s.getStatus()));
        if (allArchived) {
            plan.setStatus("1");  // 1=已完成
            plan.setUpdateBy(operName);
            plan.setUpdateTime(com.ruoyi.common.utils.DateUtils.getNowDate());
            bizTrainPlanMapper.updateBizTrainPlan(plan);
        }
    }

    /**
     * 查询培训执行场次
     * 
     * @param sessionId 培训执行场次主键
     * @return 培训执行场次
     */
    @Override
    public BizTrainSession selectBizTrainSessionBySessionId(Long sessionId)
    {
        return bizTrainSessionMapper.selectBizTrainSessionBySessionId(sessionId);
    }

    /**
     * 查询培训执行场次列表
     * 
     * @param bizTrainSession 培训执行场次
     * @return 培训执行场次
     */
    @Override
    public List<BizTrainSession> selectBizTrainSessionList(BizTrainSession bizTrainSession)
    {
        return bizTrainSessionMapper.selectBizTrainSessionList(bizTrainSession);
    }

    /**
     * 校验场地在指定时间段内是否被占用
     */
    @Override
    public int checkLocationConflict(BizTrainSession bizTrainSession)
    {
        return bizTrainSessionMapper.checkLocationConflict(bizTrainSession);
    }

    /**
     * 新增培训执行场次
     * 
     * @param bizTrainSession 培训执行场次
     * @return 结果
     */
    @Override
    public int insertBizTrainSession(BizTrainSession bizTrainSession)
    {
        bizTrainSession.setCreateTime(DateUtils.getNowDate());
        return bizTrainSessionMapper.insertBizTrainSession(bizTrainSession);
    }

    /**
     * 修改培训执行场次
     *
     * @param bizTrainSession 培训执行场次
     * @return 结果
     */
    @Override
    public int updateBizTrainSession(BizTrainSession bizTrainSession)
    {
        // 1. 查询数据库中该场次的当前状态
        BizTrainSession dbSession = bizTrainSessionMapper.selectBizTrainSessionBySessionId(bizTrainSession.getSessionId());
        if (dbSession == null) {
            throw new com.ruoyi.common.exception.ServiceException("培训场次不存在！");
        }

        // 2. 已归档场次（status=3）禁止修改
        if ("3".equals(dbSession.getStatus())) {
            throw new com.ruoyi.common.exception.ServiceException("已归档的培训场次禁止修改！");
        }

        // 3. 状态转换规则校验
        String newStatus = bizTrainSession.getStatus();
        if (newStatus != null && !isValidStatusTransition(dbSession.getStatus(), newStatus)) {
            throw new com.ruoyi.common.exception.ServiceException(
                "状态转换不允许：从【" + getStatusName(dbSession.getStatus()) + "】不能转换为【" + getStatusName(newStatus) + "】");
        }

        bizTrainSession.setUpdateTime(DateUtils.getNowDate());
        return bizTrainSessionMapper.updateBizTrainSession(bizTrainSession);
    }

    /**
     * 状态转换规则校验
     * 0=未开始, 1=进行中, 2=待审归档, 3=已归档
     */
    private boolean isValidStatusTransition(String from, String to) {
        if (from == null || to == null) return true;
        if ("0".equals(from)) {
            return to.equals("1");  // 未开始 → 进行中
        } else if ("1".equals(from)) {
            return to.equals("2");  // 进行中 → 待审归档
        } else if ("2".equals(from)) {
            return to.equals("1") || to.equals("3");  // 待审归档 → 进行中 或 已归档
        } else if ("3".equals(from)) {
            return false;  // 已归档不允许任何转换
        }
        return false;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(String status) {
        if ("0".equals(status)) return "未开始";
        if ("1".equals(status)) return "进行中";
        if ("2".equals(status)) return "待审归档";
        if ("3".equals(status)) return "已归档";
        return "未知";
    }

    /**
     * 批量删除培训执行场次
     * 
     * @param sessionIds 需要删除的培训执行场次主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainSessionBySessionIds(Long[] sessionIds)
    {
        return bizTrainSessionMapper.deleteBizTrainSessionBySessionIds(sessionIds);
    }

    /**
     * 删除培训执行场次信息
     * 
     * @param sessionId 培训执行场次主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainSessionBySessionId(Long sessionId)
    {
        return bizTrainSessionMapper.deleteBizTrainSessionBySessionId(sessionId);
    }
}
