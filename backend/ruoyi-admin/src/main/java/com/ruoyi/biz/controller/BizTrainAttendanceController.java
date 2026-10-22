package com.ruoyi.biz.controller;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.biz.domain.BizTrainArchive;
import com.ruoyi.biz.domain.BizTrainLedger;
import com.ruoyi.biz.domain.BizTrainSession;
import com.ruoyi.biz.service.IBizTrainArchiveService;
import com.ruoyi.biz.service.IBizTrainSessionService;
import com.ruoyi.biz.util.SerialNoGenerator;
import com.ruoyi.biz.util.SerialNoResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.BizTrainAttendance;
import com.ruoyi.biz.service.IBizTrainAttendanceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 线上考勤与申诉明细Controller
 * 
 * @author admin
 * @date 2026-03-03
 */
@RestController
@RequestMapping("/biz/attendance")
public class BizTrainAttendanceController extends BaseController
{
    @Autowired
    private IBizTrainAttendanceService bizTrainAttendanceService;

    @Autowired
    private IBizTrainArchiveService bizTrainArchiveService;

    @Autowired
    private IBizTrainSessionService bizTrainSessionService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private com.ruoyi.biz.mapper.BizTrainArchiveMapper archiveMapper;

    @Autowired
    private com.ruoyi.biz.mapper.BizTrainLedgerMapper ledgerMapper;

    @Autowired
    private com.ruoyi.biz.mapper.BizTrainPlanMapper planMapper;

    @Autowired
    private SerialNoGenerator serialNoGenerator;

    /**
     * 员工线上扫码/主动打卡接口
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:add')")
    @Log(title = "线上主动打卡", businessType = BusinessType.INSERT)
    @PostMapping("/checkIn")
    public AjaxResult checkIn(@RequestBody BizTrainAttendance attendance)
    {
        Long sessionId = attendance.getSessionId();
        Long userId = getUserId(); // 获取当前登录员工的ID (BaseController自带方法)

        // 1. 基础校验：场次是否存在
        BizTrainSession session = bizTrainSessionService.selectBizTrainSessionBySessionId(sessionId);
        if (session == null) {
            return error("打卡失败：培训场次不存在！");
        }

        // 2. 防重校验：是否已经打过卡
        BizTrainAttendance query = new BizTrainAttendance();
        query.setSessionId(sessionId);
        query.setUserId(userId);
        List<BizTrainAttendance> existList = bizTrainAttendanceService.selectBizTrainAttendanceList(query);
        if (existList != null && !existList.isEmpty()) {
            return error("您已经完成过本次培训的打卡，请勿重复操作！");
        }

        // 3. 提取时间准备核心逻辑计算
        java.util.Date now = DateUtils.getNowDate();
        java.util.Date startTime = session.getStartTime();
        java.util.Date endTime = session.getEndTime();

        if (startTime == null || endTime == null) {
            return error("该培训场次未设置规范的起止时间，无法启用线上考勤！");
        }

        // 4. 考勤规则引擎计算
        // 计算当前时间距离培训开始时间差了多少分钟
        long diffMinutes = (now.getTime() - startTime.getTime()) / (1000 * 60);
        String signStatus = "0"; // 默认为 0-正常

        if (now.after(endTime)) {
            return error("打卡失败：培训已经结束，打卡通道已关闭！如需补签请走申诉流程。");
        }

        if (diffMinutes > 30) {
            // 超过30分钟算作严重迟到/缺卡，直接拦截并引导申诉
            return error("您已迟到超过 30 分钟，系统已关闭您的正常打卡权限，请提交申诉说明原因！");
        } else if (diffMinutes > 15) {
            // 迟到 15~30 分钟之间
            signStatus = "1"; // 1-迟到
        } else {
            // 提前打卡 (diffMinutes为负数) 或 迟到15分钟以内
            signStatus = "0"; // 0-正常
        }

        // 5. 组装数据并入库
        BizTrainAttendance newRecord = new BizTrainAttendance();
        newRecord.setSessionId(sessionId);
        newRecord.setUserId(userId);
        newRecord.setSignTime(now);
        newRecord.setSignStatus(signStatus);
        newRecord.setAppealStatus("0"); // 默认无申诉
        newRecord.setCreateBy(getUsername());

        bizTrainAttendanceService.insertBizTrainAttendance(newRecord);

        // 6. 友好提示返回
        if ("1".equals(signStatus)) {
            return success("打卡成功，但系统记录您已【迟到】" + diffMinutes + " 分钟！");
        }
        return success("打卡成功！");
    }



    /**
     * 管理员处理考勤/补卡申诉，并自动同步个人档案
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:appeal')")
    @Log(title = "处理考勤申诉", businessType = BusinessType.UPDATE)
    @PutMapping("/appeal")
    public AjaxResult handleAppeal(@RequestBody BizTrainAttendance attendance)
    {
        //  1. 校验与更新考勤状态
        BizTrainAttendance dbRecord = bizTrainAttendanceService.selectBizTrainAttendanceByRecordId(attendance.getRecordId());
        if (dbRecord == null) {
            return error("考勤记录不存在");
        }

        // 假设 0 是正常，如果已经是正常就拦截
        if ("0".equals(dbRecord.getSignStatus())) {
            return error("该记录考勤状态已为正常，无需重复处理");
        }

        // 更新状态为：0-正常
        dbRecord.setSignStatus("0");
        // 记录管理员的处理意见
        dbRecord.setRemark(StringUtils.isNotEmpty(attendance.getRemark()) ? attendance.getRemark() : "管理员审核同意补卡");
        dbRecord.setUpdateBy(getUsername());
        bizTrainAttendanceService.updateBizTrainAttendance(dbRecord);

        //  2. 跨表提取组装档案所需的数据

        // 查出该考勤对应的具体培训场次 (为了获取培训名称、时间、讲师等)
        BizTrainSession session = bizTrainSessionService.selectBizTrainSessionBySessionId(dbRecord.getSessionId());
        if (session == null) {
            return success("申诉通过！(但未找到关联的场次信息，无法同步至个人档案)");
        }

        // 查出该员工最新的身份信息 (部门、姓名)
        SysUser user = userService.selectUserById(dbRecord.getUserId());

        //  3. 生成并插入个人培训档案
        BizTrainArchive archive = new BizTrainArchive();
        archive.setUserId(dbRecord.getUserId());
        archive.setUserName(user != null ? user.getNickName() : "未知姓名");
        archive.setDeptName(user != null && user.getDept() != null ? user.getDept().getDeptName() : "未分配部门");

        // 场次关联信息赋值
        archive.setSubject(session.getSubject());
        archive.setTrainContent("补卡同步：" + session.getSubject());

        // 格式化培训日期
        if (session.getStartTime() != null) {
            archive.setTrainDate(DateUtils.parseDateToStr("yyyy-MM-dd", session.getStartTime()));
        }

        // 提取讲师和学时
        archive.setSpeaker(session.getSpeaker());
        archive.setTrainHours(session.getTrainHours() != null ? session.getTrainHours().toString() : "未统计");

        // 补卡标识设定
        archive.setSignSeq("系统补卡"); // 明显标识这不是扫表来的，而是后台申诉通过的
        archive.setScore("免考/补卡");
        archive.setRemark("员工申诉补卡，系统自动同步入档。操作人：" + getUsername());
        archive.setCreateBy(getUsername());

        // 正式插入档案表
        bizTrainArchiveService.insertBizTrainArchive(archive);

        return success("申诉处理成功！考勤状态已恢复正常，并已同步生成该员工的个人培训档案。");
    }

    /**
     * OCR考勤确认归档：将该场次下所有考勤记录正式归档生成档案和台账
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:edit')")
    @Log(title = "OCR考勤确认归档", businessType = BusinessType.UPDATE)
    @PostMapping("/archiveSession/{sessionId}")
    public AjaxResult archiveSession(@PathVariable("sessionId") Long sessionId) {
        BizTrainSession session = bizTrainSessionService.selectBizTrainSessionBySessionId(sessionId);
        if (session == null) {
            return error("培训场次不存在");
        }
        if (!"1".equals(session.getStatus())) {
            return error("该场次不是进行中状态，无法归档");
        }

        // 查询该场次下所有考勤记录
        BizTrainAttendance query = new BizTrainAttendance();
        query.setSessionId(sessionId);
        List<BizTrainAttendance> attendanceList = bizTrainAttendanceService.selectBizTrainAttendanceList(query);
        if (attendanceList == null || attendanceList.isEmpty()) {
            return error("该场次下无考勤记录，无法归档");
        }

        String operName = getUsername();
        String subject = session.getSubject();
        String hostDept = session.getHostDeptName();
        String speaker = session.getSpeaker();
        String location = session.getLocationName();
        String hoursStr = session.getTrainHours() != null ? session.getTrainHours().toString() : "2.0";
        String content = session.getTrainContent();
        if (StringUtils.isEmpty(content)) content = subject;

        // 溯源年度计划
        com.ruoyi.biz.domain.BizTrainPlan plan = null;
        if (session.getPlanId() != null) {
            plan = planMapper.selectBizTrainPlanByPlanId(session.getPlanId());
        }
        String projectCategory = (plan != null && StringUtils.isNotEmpty(plan.getProjectCategory())) ? plan.getProjectCategory() : "常规业务培训";

        StringBuilder attendeeNames = new StringBuilder();
        int actualCount = 0;

        // 生成个人档案（每人一条）
        for (BizTrainAttendance att : attendanceList) {
            if (att.getDelFlag() != null && !"0".equals(att.getDelFlag())) {
                continue;
            }
            SysUser user = userService.selectUserById(att.getUserId());
            String userName = user != null ? user.getNickName() : (att.getUserId() != null ? "ID:" + att.getUserId() : "未知");
            String deptName = (user != null && user.getDept() != null) ? user.getDept().getDeptName() : "未分配部门";

            BizTrainArchive archive = new BizTrainArchive();
            archive.setUserId(att.getUserId());
            archive.setUserName(userName);
            archive.setDeptName(deptName);
            archive.setPlanId(session.getPlanId());
            archive.setProjectCategory(projectCategory);
            archive.setSubject(subject);
            archive.setTrainContent(content);
            archive.setTrainDept(hostDept);
            archive.setSpeaker(speaker);
            archive.setTrainLocation(location);
            archive.setTrainHours(hoursStr);
            archive.setTrainDate(session.getStartTime() != null ? DateUtils.parseDateToStr("yyyy-MM-dd", session.getStartTime()) : DateUtils.dateTimeNow());
            archive.setSignSeq(att.getSignSeq());
            archive.setScore(att.getScore());
            archive.setCreateBy(operName);
            archive.setCreateTime(DateUtils.getNowDate());
            archive.setRemark("OCR扫表归档生成");
            archiveMapper.insertBizTrainArchive(archive);

            actualCount++;
            attendeeNames.append(userName).append(",");
        }

        // 生成汇总台账（一场一条）
        if (attendeeNames.length() > 0) {
            attendeeNames.deleteCharAt(attendeeNames.length() - 1);
        }

        BizTrainLedger ledger = new BizTrainLedger();
        ledger.setPlanId(session.getPlanId());
        ledger.setSessionId(sessionId);
        ledger.setSubject(subject);
        ledger.setHostDeptName(hostDept);
        ledger.setSpeaker(speaker);
        ledger.setLocationName(location);
        ledger.setProjectCategory(projectCategory);
        ledger.setTrainHours(hoursStr);
        ledger.setTrainDate(session.getStartTime() != null ? DateUtils.parseDateToStr("yyyy-MM-dd", session.getStartTime()) : DateUtils.dateTimeNow());
        ledger.setActualCount(actualCount);
        ledger.setAttendeeNames(attendeeNames.toString());
        ledger.setPlanType(session.getPlanId() != null ? "计划内" : "计划外");
        ledger.setTrainLevel(plan != null && StringUtils.isNotEmpty(plan.getTrainLevel()) ? plan.getTrainLevel() : "公司级");
        ledger.setAttachmentUrl(session.getAttachmentUrl());
        ledger.setRemark("OCR扫表归档生成，凭证原日期：" + session.getRemark());
        ledger.setCreateBy(operName);
        ledger.setCreateTime(DateUtils.getNowDate());
        // 同步生成 serialNo / serialDate,与 BizTrainSessionServiceImpl.archiveSession
        // / BizTrainOcrBatchServiceImpl.confirmAndGenerate 行为一致,否则排序会错位。
        SerialNoResult sn = serialNoGenerator.generate("biz_train_ledger", ledger.getTrainDate());
        ledger.setSerialNo(sn.getSerialNo());
        ledger.setSerialDate(sn.getSerialDate());
        ledgerMapper.insertBizTrainLedger(ledger);

        // 更新场次状态为已归档
        session.setStatus("3");
        session.setUpdateBy(operName);
        session.setUpdateTime(DateUtils.getNowDate());
        bizTrainSessionService.updateBizTrainSession(session);

        // 若本次归档涉及年度计划，且该计划所有场次都已归档，自动标记计划为"已完成"
        bizTrainSessionService.checkAndMarkPlanCompleted(session.getPlanId(), operName);

        return success("归档成功！共归档 " + actualCount + " 人，已生成个人培训档案和汇总台账。");
    }

    /**
     * 查询线上考勤与申诉明细列表
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainAttendance bizTrainAttendance)
    {
        startPage();
        List<BizTrainAttendance> list = bizTrainAttendanceService.selectBizTrainAttendanceList(bizTrainAttendance);
        return getDataTable(list);
    }

    /**
     * 导出线上考勤与申诉明细列表
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:export')")
    @Log(title = "线上考勤与申诉明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainAttendance bizTrainAttendance)
    {
        List<BizTrainAttendance> list = bizTrainAttendanceService.selectBizTrainAttendanceList(bizTrainAttendance);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无考勤数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainAttendance> util = new ExcelUtil<BizTrainAttendance>(BizTrainAttendance.class);
        util.exportExcel(response, list, "线上考勤与申诉明细数据");
    }

    /**
     * 获取线上考勤与申诉明细详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(bizTrainAttendanceService.selectBizTrainAttendanceByRecordId(recordId));
    }

    /**
     * 新增线上考勤与申诉明细
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:add')")
    @Log(title = "线上考勤与申诉明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainAttendance bizTrainAttendance)
    {
        return toAjax(bizTrainAttendanceService.insertBizTrainAttendance(bizTrainAttendance));
    }

    /**
     * 修改线上考勤与申诉明细
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:edit')")
    @Log(title = "线上考勤与申诉明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainAttendance bizTrainAttendance)
    {
        return toAjax(bizTrainAttendanceService.updateBizTrainAttendance(bizTrainAttendance));
    }

    /**
     * 按条件批量删除所有匹配数据（跨页全选删除）
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:remove')")
    @Log(title = "线上考勤与申诉明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainAttendance query) {
        List<BizTrainAttendance> list = bizTrainAttendanceService.selectBizTrainAttendanceList(query);
        if (list.isEmpty()) {
            return error("没有符合条件的数据");
        }
        Long[] ids = list.stream().map(BizTrainAttendance::getRecordId).toArray(Long[]::new);
        return toAjax(bizTrainAttendanceService.deleteBizTrainAttendanceByRecordIds(ids));
    }

    /**
     * 删除线上考勤与申诉明细
     */
    @PreAuthorize("@ss.hasPermi('biz:attendance:remove')")
    @Log(title = "线上考勤与申诉明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(bizTrainAttendanceService.deleteBizTrainAttendanceByRecordIds(recordIds));
    }
}
