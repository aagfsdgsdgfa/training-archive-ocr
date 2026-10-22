package com.ruoyi.biz.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;

import com.ruoyi.biz.domain.BizTrainOcrBatch;
import com.ruoyi.biz.domain.BizTrainOcrLog;
import com.ruoyi.biz.domain.BizTrainOcrResult;
import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.domain.BizTrainSession;
import com.ruoyi.biz.domain.vo.OcrBatchUploadReq;
import com.ruoyi.biz.event.OcrBatchReadyEvent;
import com.ruoyi.biz.mapper.BizTrainArchiveMapper;
import com.ruoyi.biz.mapper.BizTrainAttendanceMapper;
import com.ruoyi.biz.mapper.BizTrainLedgerMapper;
import com.ruoyi.biz.mapper.BizTrainOcrBatchMapper;
import com.ruoyi.biz.mapper.BizTrainOcrLogMapper;
import com.ruoyi.biz.mapper.BizTrainOcrResultMapper;
import com.ruoyi.biz.mapper.BizTrainPlanMapper;
import com.ruoyi.biz.mapper.BizTrainSessionMapper;
import com.ruoyi.biz.ocr.AsyncOcrProcessService;
import com.ruoyi.biz.service.IBizTrainOcrBatchService;
import com.ruoyi.biz.service.IBizTrainSessionService;
import com.ruoyi.biz.util.OcrDateNormalizer;
import com.ruoyi.biz.util.SerialNoGenerator;
import com.ruoyi.biz.util.SerialNoResult;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.SysUserHistory;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserHistoryService;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.util.FileCopyUtils;

/**
 * OCR扫描批次主Service业务层处理
 * * @author admin
 * @date 2026-03-04
 */
@Service
public class BizTrainOcrBatchServiceImpl implements IBizTrainOcrBatchService
{
    private static final Logger logger = LoggerFactory.getLogger(BizTrainOcrBatchServiceImpl.class);
    @Autowired
    private BizTrainOcrBatchMapper bizTrainOcrBatchMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AsyncOcrProcessService asyncOcrService;

    @Autowired
    private BizTrainOcrLogMapper bizTrainOcrLogMapper;

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysDeptMapper deptMapper;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private BizTrainAttendanceMapper attendanceMapper;
    @Autowired
    private BizTrainArchiveMapper archiveMapper;
    @Autowired
    private BizTrainLedgerMapper ledgerMapper;
    @Autowired
    private BizTrainOcrResultMapper ocrResultMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private BizTrainSessionMapper bizTrainSessionMapper;
    @Autowired
    private BizTrainPlanMapper bizTrainPlanMapper;
    @Autowired
    private IBizTrainSessionService bizTrainSessionService;
    @Autowired
    private ISysUserHistoryService sysUserHistoryService;
    @Autowired
    private SerialNoGenerator serialNoGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAndGenerate(Long batchId, List<BizTrainOcrResult> editedList) {
        // 1. 基础校验与数据准备
        BizTrainOcrBatch batch = bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(batchId);
        if (batch == null || !"1".equals(batch.getStatus())) {
            throw new ServiceException("批次状态异常或已结算，无法执行入库操作");
        }

        String operName = SecurityUtils.getUsername();

        // 溯源年度计划，提取默认部门
        BizTrainPlan plan = null;
        if (batch.getPlanId() != null) {
            plan = bizTrainPlanMapper.selectBizTrainPlanByPlanId(batch.getPlanId());
        }
        String defaultDeptName = (plan != null && StringUtils.isNotEmpty(plan.getHostDeptName())) ? plan.getHostDeptName() : "综合管理部";

        // 提取 OCR 核心数据
        String subject = StringUtils.isNotEmpty(batch.getOcrSubject()) ? batch.getOcrSubject() : "OCR扫表补录培训";
        String hostDept = StringUtils.isNotEmpty(batch.getOcrHostDept()) ? batch.getOcrHostDept() : defaultDeptName;
        String speaker = StringUtils.isNotEmpty(batch.getOcrSpeaker()) ? batch.getOcrSpeaker() : "详见纸质表单";
        String location = StringUtils.isNotEmpty(batch.getOcrLocation()) ? batch.getOcrLocation() : "培训室";
        String hoursStr = StringUtils.isNotEmpty(batch.getOcrTrainHours()) ? batch.getOcrTrainHours() : "2.0";
        String content = StringUtils.isNotEmpty(batch.getOcrTrainContent()) ? batch.getOcrTrainContent() : subject;

        // OCR 日期归一化:区间时整段塞 train_date(start — end),失败原值落 remark 并 WARN
        Optional<OcrDateNormalizer.DateRange> dateRange = OcrDateNormalizer.normalizeRange(batch.getOcrDate());
        if (!dateRange.isPresent() && StringUtils.isNotEmpty(batch.getOcrDate())) {
            logger.warn("[OCR日期归一化失败] batchId={}, raw='{}'", batchId, batch.getOcrDate());
        }
        String normalizedTrainDate = dateRange.map(OcrDateNormalizer.DateRange::format).orElse(null);
        String dateRemark = !dateRange.isPresent() && StringUtils.isNotEmpty(batch.getOcrDate())
                ? "日期无法识别: " + batch.getOcrDate() : null;

        BigDecimal parsedHours = new BigDecimal("2.0");
        try {
            String cleanHours = hoursStr.replaceAll("[^0-9.]", "");
            if (StringUtils.isNotEmpty(cleanHours)) {
                parsedHours = new BigDecimal(cleanHours);
            }
        } catch (Exception e) {}

        // 2. 生成培训执行场次 BizTrainSession（status=1进行中，等考勤审核页确认归档后改为3）
        BizTrainSession session = new BizTrainSession();
        session.setPlanId(batch.getPlanId());
        session.setSubject(subject);
        session.setTrainContent(content);
        session.setHostDeptName(hostDept);
        session.setSpeaker(speaker);
        session.setLocationName(location);
        session.setTrainHours(parsedHours);
        session.setSignType("1"); // 纸质扫表
        session.setSource("ocr");  // 新增：标识来源
        session.setBatchId(batchId); // 新增：关联OCR批次
        session.setStatus("1");   // 进行中（不是已归档，由考勤审核页归档）
        session.setTrainDate(DateUtils.getNowDate());
        session.setStartTime(DateUtils.getNowDate());
        session.setCreateBy(operName);
        session.setCreateTime(DateUtils.getNowDate());
        session.setRemark("OCR原始提取日期文本：" + batch.getOcrDate());;

        // 绑定高拍仪凭证图到场次
        BizTrainOcrLog queryLog = new BizTrainOcrLog();
        queryLog.setBatchId(batchId);
        List<BizTrainOcrLog> logs = bizTrainOcrLogMapper.selectBizTrainOcrLogList(queryLog);
        if (logs != null && !logs.isEmpty()) {
            String virtualUrls = logs.stream().map(log -> {
                String absolute = log.getFileUrl();
                if (absolute == null || StringUtils.isBlank(absolute)) return "";
                int ocrIndex = absolute.indexOf("ocr");
                if (ocrIndex != -1) {
                    return "/profile/" + absolute.substring(ocrIndex).replace("\\", "/");
                }
                return absolute.replace("\\", "/");
            }).collect(Collectors.joining(","));
            session.setAttachmentUrl(virtualUrls);
        }

        bizTrainSessionMapper.insertBizTrainSession(session);
        Long newSessionId = session.getSessionId();

        // 3. 循环处理人员：生成考勤记录（档案和台账留到考勤审核页归档）
        List<SysUser> allSystemUsers = userMapper.selectUserList(new SysUser());
        int actualCount = 0;
        // 保存用户ID到用户的映射,用于后续生成档案
        java.util.Map<Long, SysUser> userMap = new java.util.HashMap<>();
        // 收集非缺席 OCR 行的「备注」原文,3.6 建档案时按 userId 回填到 archive.remark
        // [关键]用 userId 而非 signSeq 作 key:培训考核记录表.xls 是左右分栏,同一行 signSeq="1" 会有两人,不重写会丢
        java.util.Map<Long, String> remarkByUserId = new java.util.HashMap<>();
        // 收集每个人对应的 signSeq(考勤表里缺席人员没记录,3.6 还得从 OCR 行回填)
        java.util.Map<Long, String> signSeqByUserId = new java.util.HashMap<>();

        for (BizTrainOcrResult item : editedList) {
            // 过滤前端管理员手动作废剔除的数据
            if ("1".equals(item.getStatus())) {
                continue;
            }

            // [缺席标记] OCR 标记为缺席的人员(授课人/未参加/缺席/缺考):
            // 不进 attendance / 不计入 actualCount,但仍继续往下走,
            // 进入 userMap / remarkByUserId / archive 流程,让 /train/archive 看得到备注
            boolean isAbsent = isAbsentRemark(item.getParsedRemark());
            if (isAbsent) {
                logger.info("【OCR缺席(仍建档案)】姓名={}, 备注={}, 不进考勤/不计入实到,但保留档案行让 /train/archive 看得到",
                        item.getParsedName(), item.getParsedRemark());
                item.setStatus("2");
                ocrResultMapper.updateBizTrainOcrResult(item);
                // 不 continue!让它继续往下走,进 userMap / remarkByUserId / archive 流程
            }

            // 精准匹配老员工
            SysUser matchedUser = allSystemUsers.stream()
                    .filter(u -> item.getParsedName().equals(u.getNickName()) || item.getParsedName().equals(u.getUserName()))
                    .findFirst()
                    .orElse(null);

            Long userId;
            SysUser savedUser;
            if (matchedUser != null) {
                userId = matchedUser.getUserId();
                savedUser = matchedUser;
                // 如果识别到的部门与员工当前部门不同，更新员工部门
                if (StringUtils.isNotEmpty(item.getParsedDept())) {
                    Long targetDeptId = resolveDeptId(item.getParsedDept());
                    if (targetDeptId != null && !targetDeptId.equals(matchedUser.getDeptId())) {
                        matchedUser.setDeptId(targetDeptId);
                        userMapper.updateUser(matchedUser);
                        logger.info("【部门更新】员工'{}'部门从{}更新为{}", item.getParsedName(), matchedUser.getDeptId(), targetDeptId);
                    }
                }
            } else {
                // 新成员：自动建号（部门已智能匹配）
                SysUser newUser = new SysUser();
                newUser.setUserName(item.getParsedName() + "_" + IdUtils.fastSimpleUUID().substring(0, 4).toUpperCase());
                newUser.setNickName(item.getParsedName());
                newUser.setPassword(SecurityUtils.encryptPassword("123456"));
                newUser.setDeptId(resolveDeptId(item.getParsedDept()));
                newUser.setRoleIds(new Long[]{2L});
                userService.insertUser(newUser);
                userId = newUser.getUserId();
                savedUser = newUser;
            }

            // [核心修复]按 userId 存 remark,3.6 建档案时按 userId 取(避免左右分栏 signSeq 冲突)
            remarkByUserId.put(userId, item.getParsedRemark());
            // 同步记 signSeq,3.6 缺席人员没考勤,这里兜底,保证 archive.sign_seq 也有值
            if (StringUtils.isNotEmpty(item.getSignSeq())) {
                signSeqByUserId.put(userId, item.getSignSeq());
            }
            logger.info("【OCR备注入库】userId={}, name={}, remark='{}', signSeq={}",
                    userId, item.getParsedName(), item.getParsedRemark(), item.getSignSeq());

            // 保存用户信息到map（用于后续生成档案）
            userMap.put(userId, savedUser);
            // 考勤与台账的"实到人数":只统计非缺席人员
            if (!isAbsent) {
                actualCount++;
            }

            // 调试日志：打印OCR识别结果
            logger.info("【OCR处理】姓名={}, 部门={}, 分数={}, 匹配到userId={}, nickName={}, isAbsent={}",
                    item.getParsedName(), item.getParsedDept(), item.getParsedScore(), userId, savedUser.getNickName(), isAbsent);

            // 生成考勤记录（source=ocr，标识来源）— 缺席人员不进 attendance
            if (!isAbsent) {
                com.ruoyi.biz.domain.BizTrainAttendance attendance = new com.ruoyi.biz.domain.BizTrainAttendance();
                attendance.setSessionId(newSessionId);
                attendance.setUserId(userId);
                attendance.setSignStatus("0");           // 正常
                attendance.setSignTime(DateUtils.getNowDate());
                attendance.setSignSeq(item.getSignSeq());
                attendance.setScore(item.getParsedScore());
                attendance.setSource("ocr");            // 标识来源：OCR扫表
                attendance.setCreateBy(operName);
                attendance.setCreateTime(DateUtils.getNowDate());
                attendanceMapper.insertBizTrainAttendance(attendance);
            }

            // 回写明细表状态：已处理
            item.setStatus("2");
            ocrResultMapper.updateBizTrainOcrResult(item);
        }

        // 3.5 生成培训台账总表 (BizTrainLedger)
        String attendeeNames = editedList.stream()
                .filter(item -> !"1".equals(item.getStatus()))
                .map(BizTrainOcrResult::getParsedName)
                .collect(Collectors.joining(","));

        com.ruoyi.biz.domain.BizTrainLedger ledger = new com.ruoyi.biz.domain.BizTrainLedger();
        ledger.setPlanId(batch.getPlanId());
        ledger.setSessionId(newSessionId);
        ledger.setSubject(subject);
        ledger.setHostDeptName(hostDept);
        ledger.setTrainDate(normalizedTrainDate);
        ledger.setTrainHours(hoursStr);
        ledger.setSpeaker(speaker);
        ledger.setLocationName(location);
        ledger.setAttendeeNames(attendeeNames);
        ledger.setActualCount(actualCount);
        // 应到人数：优先用 OCR 提取的 Excel 应到人数，否则用实到兜底
        ledger.setTargetCount(batch.getOcrTargetCount() != null ? batch.getOcrTargetCount() : actualCount);
        // 从年度计划获取项目分类、计划类型、培训级别（plan 为空时使用兜底字符串，避免字段 NULL 导致首页饼图失真）
        String projectCategory = (plan != null && StringUtils.isNotEmpty(plan.getProjectCategory()))
                ? plan.getProjectCategory() : "常规业务培训";
        String planType = (plan != null)
                ? (plan.getPlanType().equals("1") ? "计划内" : "计划外") : "计划外";
        String trainLevel = (plan != null && StringUtils.isNotEmpty(plan.getTrainLevel()))
                ? plan.getTrainLevel() : "公司级";
        ledger.setProjectCategory(projectCategory);
        ledger.setPlanType(planType);
        ledger.setTrainLevel(trainLevel);
        ledger.setCreateBy(operName);
        ledger.setCreateTime(DateUtils.getNowDate());
        // 直接走 mapper 不会触发 Service 层自动生成流水号，这里手动生成（参照 archiveSession）
        SerialNoResult sn = serialNoGenerator.generate("biz_train_ledger", ledger.getTrainDate());
        ledger.setSerialNo(sn.getSerialNo());
        ledger.setSerialDate(sn.getSerialDate());
        ledgerMapper.insertBizTrainLedger(ledger);

        // 3.6 生成个人培训档案 (BizTrainArchive) — 遍历 userMap,缺席人员(no attendance)也会建档案
        List<com.ruoyi.biz.domain.BizTrainAttendance> atts =
                attendanceMapper.selectBizTrainAttendanceBySessionId(newSessionId);
        // userId → attendance 映射,缺席人员查不到(返回 null)
        java.util.Map<Long, com.ruoyi.biz.domain.BizTrainAttendance> attByUserId = new java.util.HashMap<>();
        for (com.ruoyi.biz.domain.BizTrainAttendance a : atts) {
            attByUserId.put(a.getUserId(), a);
        }

        for (Long uid : userMap.keySet()) {
            // 使用已缓存的用户信息（只用于获取userId和deptId）
            SysUser cachedUser = userMap.get(uid);
            if (cachedUser == null) continue;

            // 重新查询用户，确保dept关联被正确填充
            SysUser user = userMapper.selectUserById(cachedUser.getUserId());
            if (user == null) continue;

            // 考勤行(可能 null — 缺席人员不进 attendance)
            com.ruoyi.biz.domain.BizTrainAttendance att = attByUserId.get(uid);
            // 有考勤但签到状态不是"正常/迟到",跳过(保持原行为)
            if (att != null && !"0".equals(att.getSignStatus()) && !"1".equals(att.getSignStatus())) {
                continue;
            }

            // 获取部门名称
            String deptName = "";
            if (user.getDept() != null) {
                deptName = user.getDept().getDeptName() != null ? user.getDept().getDeptName() : "";
            }

            // 调试日志
            logger.info("【OCR建档案】userId={}, userName={}, deptId={}, deptName={}, isAbsent={}",
                    user.getUserId(), user.getNickName(), user.getDeptId(), deptName, att == null);

            com.ruoyi.biz.domain.BizTrainArchive archive = new com.ruoyi.biz.domain.BizTrainArchive();
            archive.setUserId(user.getUserId());
            archive.setUserName(user.getNickName());
            archive.setDeptId(user.getDeptId());
            archive.setDeptName(deptName);
            archive.setTrainDept(hostDept);
            archive.setTrainLocation(location);
            archive.setPlanId(batch.getPlanId());
            archive.setLedgerId(ledger.getLedgerId());
            archive.setTrainDate(normalizedTrainDate);
            // [核心修复]OCR 备注(每人各异)优先,日期兜底(批次级一次性)次之,两者都有时拼接
            //   旧逻辑 dateRemark != null 直接覆盖,会让 dateRemark 一来所有人的真备注全丢 → /train/archive 全空白
            String ocrRemark = remarkByUserId.get(user.getUserId());
            String finalRemark = composeArchiveRemark(ocrRemark, dateRemark);
            if (finalRemark != null) {
                archive.setRemark(finalRemark);
                logger.info("【档案remark写入】userId={}, name={}, remark='{}'",
                        user.getUserId(), user.getNickName(), finalRemark);
            } else {
                logger.warn("【档案remark缺失】userId={}, name={}, OCR未识别到备注,日期也未识别",
                        user.getUserId(), user.getNickName());
            }
            archive.setSubject(subject);
            archive.setTrainContent(content);
            archive.setSpeaker(speaker);
            archive.setTrainHours(hoursStr);
            // 缺席人员无 attendance,score=null、signSeq 取 OCR 行兜底值
            archive.setScore(att != null ? att.getScore() : null);
            archive.setSignSeq(att != null ? att.getSignSeq() : signSeqByUserId.get(uid));
            archive.setProjectCategory(plan != null ? plan.getProjectCategory() : "");
            archive.setCreateBy(operName);
            archive.setCreateTime(DateUtils.getNowDate());
            archiveMapper.insertBizTrainArchive(archive);

            // 3.6.1 同步员工历史快照（确保 /train/archive 页面能正确显示员工姓名和归属部门）
            // 解析培训日期
            java.util.Date trainDate = null;
            String trainDateStr = batch.getOcrDate();
            if (StringUtils.isNotEmpty(trainDateStr)) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    trainDate = sdf.parse(trainDateStr);
                } catch (Exception e) {
                    // 解析失败，使用当前日期
                    trainDate = DateUtils.getNowDate();
                }
            } else {
                trainDate = DateUtils.getNowDate();
            }
            syncUserHistoryForArchive(user, deptName, trainDate);
        }

        // 3.7 更新场次状态为已归档(3)，因为台账和档案已生成
        session.setStatus("3");
        session.setUpdateBy(operName);
        session.setUpdateTime(DateUtils.getNowDate());
        bizTrainSessionMapper.updateBizTrainSession(session);

        // 3.8 若本次归档涉及年度计划，且该计划所有场次都已归档，自动标记计划为"已完成"
        // (路径与 BizTrainSessionServiceImpl.archiveSession 一致,避免 OCR 归档漏更新计划状态)
        bizTrainSessionService.checkAndMarkPlanCompleted(batch.getPlanId(), operName);

        // 4. 更新批次主表状态
        batch.setStatus("2");
        batch.setOcrActualCount(actualCount);
        batch.setRemark("已提交到考勤审核页，待管理员确认归档。实到有效人员：" + actualCount + "人");
        batch.setUpdateTime(DateUtils.getNowDate());
        bizTrainOcrBatchMapper.updateBizTrainOcrBatch(batch);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadAndCreateBatch(OcrBatchUploadReq req) {
        List<String> base64List = req.getBase64Images();
        if (base64List == null || base64List.isEmpty()) {
            throw new ServiceException("未接收到扫描图片数据，请检查高拍仪传输");
        }

        // 1. 准备文件存储目录（共用）
        String basePath = RuoYiConfig.getProfile() + "/ocr/" + DateUtils.datePath() + "/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 检测是否已有当天同 planId 且未结算的 batch（追加模式）
        BizTrainOcrBatch query = new BizTrainOcrBatch();
        query.setPlanId(req.getPlanId());
        List<BizTrainOcrBatch> openBatches = bizTrainOcrBatchMapper.selectOpenBatchByKey(query);
        BizTrainOcrBatch existing = openBatches.isEmpty() ? null : openBatches.get(0);

        boolean hasImageFound;
        Long finalBatchId;

        if (existing != null) {
            // ===== 追加模式：高拍仪分多次拍后自动合并到同一 batch =====
            logger.info("【高拍仪追加】命中当天未结算批次 batchId={}，新 {} 页作为追加 log 入库",
                    existing.getBatchId(), base64List.size());
            finalBatchId = existing.getBatchId();
            hasImageFound = appendPagesToBatch(finalBatchId, base64List, basePath, existing);
            // totalPages 累加
            existing.setTotalPages(existing.getTotalPages() + base64List.size());
            existing.setUpdateTime(DateUtils.getNowDate());
            bizTrainOcrBatchMapper.updateBizTrainOcrBatch(existing);
        } else {
            // ===== 新建模式（原逻辑保留） =====
            logger.info("【高拍仪追加】未命中当天未结算批次，创建新 batch");
            BizTrainOcrBatch batch = new BizTrainOcrBatch();
            batch.setPlanId(req.getPlanId());
            batch.setBatchNo("OCR" + DateUtils.dateTimeNow() + String.format("%03d", new Random().nextInt(1000)));
            batch.setTotalPages(base64List.size());
            batch.setStatus("0");
            batch.setCreateBy(SecurityUtils.getUsername());
            batch.setCreateTime(DateUtils.getNowDate());
            bizTrainOcrBatchMapper.insertBizTrainOcrBatch(batch);
            finalBatchId = batch.getBatchId();
            hasImageFound = appendPagesToBatch(finalBatchId, base64List, basePath, batch);
            // 新建时把 batch 对象回填（追加模式不需要，因为 existing 已经是 DB 里的对象）
            existing = batch;
        }

        // 3. 触发异步 OCR 或更新纯 Excel 状态（共用）
        if (hasImageFound) {
            eventPublisher.publishEvent(new OcrBatchReadyEvent(finalBatchId));
        } else {
            // 纯 Excel 批次：直接置为"待校对"状态，不触发异步 OCR
            BizTrainOcrBatch update = new BizTrainOcrBatch();
            update.setBatchId(finalBatchId);
            update.setStatus("1");
            update.setUpdateTime(DateUtils.getNowDate());
            bizTrainOcrBatchMapper.updateBizTrainOcrBatch(update);
        }

        return finalBatchId;
    }

    /**
     * 把 base64 列表解析后作为 log 追加到指定 batch
     * 新建模式和追加模式共用，避免代码重复
     * @return true=包含图片（需触发异步 OCR），false=纯 Excel
     */
    private boolean appendPagesToBatch(Long batchId, List<String> base64List, String basePath, BizTrainOcrBatch batch) {
        boolean hasImageFound = false;
        for (int i = 0; i < base64List.size(); i++) {
            String base64 = base64List.get(i);
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }

            // 安全解码：格式不对时给明确提示
            byte[] fileBytes;
            try {
                fileBytes = Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException e) {
                throw new ServiceException("第 " + (i + 1) + " 页数据格式异常，无法解码，请重新拍照上传");
            }

            if (fileBytes.length < 4) {
                throw new ServiceException("第 " + (i + 1) + " 页数据异常，文件太短，请重新上传");
            }

            int b0 = fileBytes[0] & 0xFF, b1 = fileBytes[1] & 0xFF,
                b2 = fileBytes[2] & 0xFF, b3 = fileBytes[3] & 0xFF;

            // XLSX (ZIP格式) 或 XLS (OLE2格式)：直接解析入库
            boolean isExcelXlsx = (b0 == 0x50 && b1 == 0x4B);
            boolean isExcelXls = (b0 == 0xD0 && b1 == 0xCF && b2 == 0x11 && b3 == 0xE0);
            if (isExcelXlsx || isExcelXls) {
                String fileName = IdUtils.fastUUID() + (isExcelXls ? ".xls" : ".xlsx");
                String absolutePath = basePath + fileName;
                try {
                    FileCopyUtils.copy(fileBytes, new File(absolutePath));
                } catch (IOException e) {
                    throw new ServiceException("第 " + (i + 1) + " 个 Excel 文件保存失败，请检查服务器磁盘权限");
                }

                // [修复 OCR Excel 重复插入] Excel 走"整张表全量替换"语义,不是"追加新页"。
                // 自动追加模式下(同 planId 当天未结算),重传同一个 Excel 会让 result 表 row 数翻倍。
                // 在插入新 log/result 前,先把该 batch 下旧 Excel log 与 result 清掉。
                BizTrainOcrLog excelLogQuery = new BizTrainOcrLog();
                excelLogQuery.setBatchId(batchId);
                List<BizTrainOcrLog> oldLogs = bizTrainOcrLogMapper.selectBizTrainOcrLogList(excelLogQuery);
                List<Long> oldExcelLogIds = new ArrayList<>();
                for (BizTrainOcrLog old : oldLogs) {
                    if ("Excel 直接解析入库".equals(old.getRemark())) {
                        oldExcelLogIds.add(old.getLogId());
                    }
                }
                if (!oldExcelLogIds.isEmpty()) {
                    int deletedResult = ocrResultMapper.deleteBizTrainOcrResultByBatchId(batchId);
                    int deletedLog = bizTrainOcrLogMapper.deleteBizTrainOcrLogByLogIds(oldExcelLogIds.toArray(new Long[0]));
                    logger.info("【Excel重传清理】batchId={} 清理旧 Excel 明细 {} 条,旧 log {} 条",
                            batchId, deletedResult, deletedLog);
                }

                BizTrainOcrLog log = new BizTrainOcrLog();
                log.setBatchId(batchId);
                log.setFileMd5(null);
                log.setFileUrl(absolutePath);
                log.setStatus("1");
                log.setRemark("Excel 直接解析入库");
                log.setCreateBy(SecurityUtils.getUsername());
                log.setCreateTime(DateUtils.getNowDate());
                bizTrainOcrLogMapper.insertBizTrainOcrLog(log);

                try {
                    parseExcelFile(absolutePath, batchId, log.getLogId());
                } catch (ServiceException e) {
                    throw e; // 业务异常直接上抛
                } catch (Exception e) {
                    throw new ServiceException("第 " + (i + 1) + " 个 Excel 解析失败: " + e.getMessage());
                }
                continue;
            }

            // PNG / JPEG / GIF：走 OCR 识别
            boolean isImage = (b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47)
                           || (b0 == 0xFF && b1 == 0xD8 && b2 == 0xFF)
                           || (b0 == 0x47 && b1 == 0x49 && b2 == 0x46);
            if (!isImage) {
                throw new ServiceException("第 " + (i + 1) + " 页不是有效的图片或 Excel 格式，请重新上传");
            }

            hasImageFound = true;
            String md5 = DigestUtils.md5DigestAsHex(fileBytes);

            // MD5 去重改为"仅提醒，不阻止"——允许重复上传但给出警告
            BizTrainOcrLog checkExist = bizTrainOcrLogMapper.checkMd5Unique(md5);
            if (checkExist != null) {
                logger.warn("检测到 MD5 重复文件，已放行继续上传，用户可通过批次号追溯: {}", batch.getBatchNo());
            }

            String fileName = IdUtils.fastUUID() + ".jpg";
            String absolutePath = basePath + fileName;
            try {
                FileCopyUtils.copy(fileBytes, new File(absolutePath));
            } catch (IOException e) {
                throw new ServiceException("第 " + (i + 1) + " 页图片保存失败，请检查服务器磁盘空间");
            }

            BizTrainOcrLog log = new BizTrainOcrLog();
            log.setBatchId(batchId);
            log.setFileMd5(md5);
            log.setFileUrl(absolutePath);
            log.setStatus("0");
            log.setCreateBy(SecurityUtils.getUsername());
            log.setCreateTime(DateUtils.getNowDate());
            try {
                bizTrainOcrLogMapper.insertBizTrainOcrLog(log);
            } catch (DataIntegrityViolationException e) {
                // 唯一约束冲突时跳过该页，继续处理其他文件
                logger.warn("第 {} 页文件已存在，已跳过", i + 1);
            }
        }
        return hasImageFound;
    }

    @Override
    public BizTrainOcrBatch selectBizTrainOcrBatchByBatchId(Long batchId) {
        return bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(batchId);
    }

    @Override
    public List<BizTrainOcrBatch> selectBizTrainOcrBatchList(BizTrainOcrBatch bizTrainOcrBatch) {
        return bizTrainOcrBatchMapper.selectBizTrainOcrBatchList(bizTrainOcrBatch);
    }

    @Override
    public int insertBizTrainOcrBatch(BizTrainOcrBatch bizTrainOcrBatch) {
        bizTrainOcrBatch.setCreateTime(DateUtils.getNowDate());
        return bizTrainOcrBatchMapper.insertBizTrainOcrBatch(bizTrainOcrBatch);
    }

    @Override
    public int updateBizTrainOcrBatch(BizTrainOcrBatch bizTrainOcrBatch) {
        bizTrainOcrBatch.setUpdateTime(DateUtils.getNowDate());
        return bizTrainOcrBatchMapper.updateBizTrainOcrBatch(bizTrainOcrBatch);
    }

    @Override
    public int deleteBizTrainOcrBatchByBatchIds(Long[] batchIds) {
        return bizTrainOcrBatchMapper.deleteBizTrainOcrBatchByBatchIds(batchIds);
    }

    @Override
    public int deleteBizTrainOcrBatchByBatchId(Long batchId) {
        return bizTrainOcrBatchMapper.deleteBizTrainOcrBatchByBatchId(batchId);
    }

    /**
     * 根据OCR提取的部门名称，智能匹配系统部门表
     * 如果找不到匹配的部门，自动创建新部门
     * @param parsedDept OCR提取的部门名称（可能含空格/特殊字符）
     * @return 匹配到的部门ID
     */
    private Long resolveDeptId(String parsedDept) {
        if (StringUtils.isEmpty(parsedDept)) {
            return 100L;
        }
        // 清洗OCR部门名称：去首尾空格、移除多余空白
        String cleanedDept = parsedDept.trim().replaceAll("\\s+", "");

        // 查询系统所有正常部门
        SysDept query = new SysDept();
        query.setStatus("0"); // 只查正常状态的部门
        List<SysDept> allDepts = deptMapper.selectDeptList(query);

        // 模糊匹配：清洗后的名称互相包含即认为匹配
        for (SysDept dept : allDepts) {
            String sysDeptClean = dept.getDeptName().trim().replaceAll("\\s+", "");
            if (cleanedDept.contains(sysDeptClean) || sysDeptClean.contains(cleanedDept)) {
                return dept.getDeptId();
            }
        }

        // 未匹配到任何部门，自动创建新部门
        return createDeptIfNotExists(cleanedDept, parsedDept.trim());
    }

    /**
     * 自动创建不存在的部门
     * @param cleanedDeptName 清洗后的部门名称（用于唯一性检查）
     * @param originalDeptName 原始部门名称（用于显示）
     * @return 新创建的部门ID
     */
    private Long createDeptIfNotExists(String cleanedDeptName, String originalDeptName) {
        logger.info("【智能建部】检测到新部门'{}'，正在自动创建...", originalDeptName);

        // 再次确认部门不存在（避免并发问题）
        SysDept checkDept = deptMapper.checkDeptNameUnique(cleanedDeptName, 100L);
        if (checkDept != null) {
            return checkDept.getDeptId();
        }

        // 创建新部门，默认挂在"综合管理部"下（parentId=100）
        SysDept newDept = new SysDept();
        newDept.setParentId(100L);      // 父部门：综合管理部
        newDept.setDeptName(originalDeptName);
        newDept.setAncestors("0,100");  // 祖级列表
        newDept.setOrderNum(100);        // 默认排序
        newDept.setStatus("0");          // 正常状态
        newDept.setCreateBy(SecurityUtils.getUsername());
        newDept.setCreateTime(DateUtils.getNowDate());

        try {
            deptMapper.insertDept(newDept);
            logger.info("【智能建部】部门'{}'创建成功，deptId={}", originalDeptName, newDept.getDeptId());
            return newDept.getDeptId();
        } catch (Exception e) {
            logger.error("【智能建部】部门创建失败: {}", e.getMessage());
            // 创建失败则使用默认部门
            return 100L;
        }
    }

    /**
     * 解析 Excel 文件，提取元数据 + 人员数据
     * 元数据提取：扫描前10行，匹配"培训主题"、"培训部门"等关键字
     * 人员数据：自动识别列结构（序号, 姓名, 分数, 部门等）
     *
     * 只处理第一个 sheet；sheet 内支持左右分栏的多组列识别（培训考核记录表.xls 等模板）
     */
    private void parseExcelFile(String excelPath, Long batchId, Long logId) {
        // 获取批次主表引用（用于回写元数据）
        BizTrainOcrBatch batch = bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(batchId);
        if (batch == null) {
            throw new ServiceException("批次不存在：" + batchId);
        }

        try (InputStream is = new FileInputStream(excelPath);
             Workbook workbook = WorkbookFactory.create(is)) {

            // 只扫第一个 sheet
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                logger.warn("【Excel解析】文件 {} 第一个 sheet 为空，无数据可解析", excelPath);
                return;
            }

            logger.info("【Excel解析】文件 {} 只处理第 1 个 sheet: {}",
                    excelPath, sheet.getSheetName());

            // Step 1: 提取顶部元数据（培训主题/日期/部门/授课人等）
            extractExcelMetadata(sheet, batch);

            // Step 2: 智能识别列结构（支持左右分栏多组）
            int headerRow = findHeaderRow(sheet);
            List<int[]> colGroups = identifyColumnGroups(sheet, headerRow);

            logger.info("【Excel解析】sheet={} 表头行={}, 列组数={}",
                    sheet.getSheetName(), headerRow, colGroups.size());
            for (int g = 0; g < colGroups.size(); g++) {
                int[] m = colGroups.get(g);
                logger.info("【Excel解析】  第{}组: 序号=列{}, 姓名=列{}, 分数=列{}, 部门=列{}",
                        g + 1, m[0], m[1], m[2], m[3]);
            }

            // Step 3: 逐行 × 逐组提取人员数据
            int lastRow = sheet.getLastRowNum();
            for (int i = headerRow + 1; i <= lastRow; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                int rowLastCell = row.getLastCellNum();

                for (int g = 0; g < colGroups.size(); g++) {
                    int[] colMap = colGroups.get(g);
                    int seqCol = colMap[0];
                    int nameCol = colMap[1];
                    int scoreCol = colMap[2];
                    int deptCol = colMap[3];
                    int remarkCol = colMap[4]; // [新增] 备注列(授课人/未参加/缺席/缺考)

                    String seq = (seqCol >= 0 && seqCol < rowLastCell)
                            ? getCellString(row.getCell(seqCol)) : null;
                    String name = (nameCol >= 0 && nameCol < rowLastCell)
                            ? getCellString(row.getCell(nameCol)) : null;
                    String score = (scoreCol >= 0 && scoreCol < rowLastCell)
                            ? getCellString(row.getCell(scoreCol)) : null;
                    String dept = (deptCol >= 0 && deptCol < rowLastCell)
                            ? getCellString(row.getCell(deptCol)) : null;
                    String remark = (remarkCol >= 0 && remarkCol < rowLastCell)
                            ? getCellString(row.getCell(remarkCol)) : null;

                    // 调试日志：每一行 × 每一组都打
                    logger.info("【Excel行解析】行{} 第{}组: seq={}, name={}, score={}, dept={}, remark={}",
                            i, g + 1, seq, name, score, dept, remark);

                    // 跳过空行（姓名为空）
                    if (StringUtils.isEmpty(name)) continue;
                    // 跳过表头行（如果没找到则返回1，则headerRow+1 >= 2）
                    if (name.contains("姓名") || name.contains("序号") || name.contains("签到")) continue;

                    // 规范化考核成绩（null表示没有成绩，不跳过该行）
                    String normalizedScore = normalizeExcelScore(score, seq);

                    BizTrainOcrResult result = new BizTrainOcrResult();
                    result.setBatchId(batchId);
                    result.setLogId(logId);
                    result.setSignSeq(seq);
                    result.setParsedName(name.trim());
                    result.setParsedDept(dept != null ? dept.trim() : null);
                    result.setParsedScore(normalizedScore); // 可能为null
                    // [新增] 备注列(缺席人员不计入实到)
                    result.setParsedRemark(remark != null ? remark.trim() : null);
                    result.setIsNewUser("1");
                    result.setStatus("0");
                    result.setCreateBy(SecurityUtils.getUsername());
                    result.setCreateTime(DateUtils.getNowDate());
                    ocrResultMapper.insertBizTrainOcrResult(result);
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Excel 文件解析失败：" + e.getMessage());
        }
    }

    /**
     * 查找表头行（包含"姓名"关键字的行，姓名是最重要的标识）
     */
    private int findHeaderRow(Sheet sheet) {
        int scanLimit = Math.min(sheet.getLastRowNum(), 30); // 增加扫描范围
        for (int i = 0; i <= scanLimit; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row == null) continue;

            // 扫描所有列，找"姓名"关键字（最重要的标识）
            for (int c = 0; c < row.getLastCellNum(); c++) {
                String val = getCellString(row.getCell(c));
                if (val != null && val.contains("姓名")) {
                    // 打印找到的表头行
                    StringBuilder headerInfo = new StringBuilder("【表头行】行" + i + ": ");
                    for (int ci = 0; ci < row.getLastCellNum(); ci++) {
                        String cellVal = getCellString(row.getCell(ci));
                        headerInfo.append("[").append(ci).append(":").append(cellVal).append("] ");
                    }
                    logger.info(headerInfo.toString());
                    return i;
                }
            }
        }
        // 默认返回第7行（培训考核记录表的标准格式）
        logger.warn("【表头查找】未找到包含'姓名'的行，默认使用行7");
        return 7;
    }

    /**
     * 智能识别列映射
     * @return int[4] = {seqCol, nameCol, scoreCol, deptCol}
     * 关键：姓名列优先识别，因为它是定位其他列的关键
     */
    private int[] identifyColumnMapping(Sheet sheet, int headerRow) {
        int[] colMap = {-1, -1, -1, -1};
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(headerRow);
        if (row == null) {
            // 默认映射：姓名=0, 序号=1, 分数=-1(未找到), 部门=2
            colMap = new int[]{1, 0, -1, 2};
            return colMap;
        }

        // 第一次扫描：优先找姓名列（最关键）
        for (int c = 0; c < row.getLastCellNum(); c++) {
            String val = getCellString(row.getCell(c));
            if (val == null) continue;
            val = val.replaceAll("\\s+", "");

            // 姓名列优先识别
            if (val.contains("姓名") || val.contains("培训人")) {
                if (colMap[1] == -1) colMap[1] = c; // 姓名是 colMap[1]
            }
        }

        // 第二次扫描：找其他列
        for (int c = 0; c < row.getLastCellNum(); c++) {
            // 跳过已识别的姓名列
            if (c == colMap[1]) continue;

            String val = getCellString(row.getCell(c));
            if (val == null) continue;
            val = val.replaceAll("\\s+", "");

            // 序号列（colMap[0]）
            if (colMap[0] == -1 && (val.contains("序号") || val.contains("签到"))) {
                colMap[0] = c;
            }
            // 考核成绩列 (colMap[2])
            else if (colMap[2] == -1 && (val.contains("考核") || val.contains("分数") || val.contains("成绩"))) {
                colMap[2] = c;
            }
            // 部门列 (colMap[3])
            else if (colMap[3] == -1 && (val.contains("部门") || val.contains("所属"))) {
                colMap[3] = c;
            }
        }

        // 如果没找到所有列，使用默认值（成绩列默认-1表示未启用）
        if (colMap[1] == -1) colMap[1] = 0;  // 默认姓名在A列
        if (colMap[0] == -1) colMap[0] = 1;  // 默认序号在B列
        // colMap[2] 成绩列默认-1，不设置默认值（避免空表格被错误识别）
        if (colMap[3] == -1) colMap[3] = 2;  // 默认部门在C列

        return colMap;
    }

    /**
     * 识别表头行内所有的「姓名」列，每组独立返回一组列映射
     * 支持左右分栏布局（如 培训考核记录表.xls：左半 1-26 + 右半 27-36）
     *
     * @return List&lt;int[5]&gt;: 每组 {seqCol, nameCol, scoreCol, deptCol, remarkCol}
     *         如果表头行不含「姓名」列，返回空 list（调用方应跳过整个 sheet）
     * 关键：表头行可能含多个「姓名」列（左右分栏），需为每组独立识别
     *       - 「序号」列：姓名左侧最近的「序号/签到」
     *       - 「部门」列：双向扫，距离 nameCol 最近的「部门/所属」
     *       - 「考核」列：双向扫，距离 nameCol 最近的「考核/分数/成绩」
     *       - 「备注」列：双向扫，距离 nameCol 最近的「备注/说明」(缺席人员识别用)
     */
    private List<int[]> identifyColumnGroups(Sheet sheet, int headerRow) {
        List<int[]> groups = new ArrayList<>();
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(headerRow);
        if (row == null) {
            // 表头行为空 → 视为非数据 sheet（返回空 list 让上游跳过）
            return groups;
        }

        // 第一遍：找出所有「姓名」列位置
        List<Integer> nameCols = new ArrayList<>();
        for (int c = 0; c < row.getLastCellNum(); c++) {
            String val = getCellString(row.getCell(c));
            if (val == null) continue;
            val = val.replaceAll("\\s+", "");
            if (val.contains("姓名") || val.contains("培训人")) {
                nameCols.add(c);
            }
        }
        if (nameCols.isEmpty()) {
            // 没找到任何「姓名」列 → 非培训考核 sheet，返回空 list 让上游跳过
            return groups;
        }

        // 第二遍：对每个姓名列，双向找最近的「序号/部门/考核/备注」
        for (int nameCol : nameCols) {
            int seqCol = -1, deptCol = -1, scoreCol = -1, remarkCol = -1;

            // 1. 「序号/签到」：业务上一定在姓名左侧（标准布局）
            //    向左找最近的「序号/签到」
            for (int c = nameCol - 1; c >= 0; c--) {
                String val = getCellString(row.getCell(c));
                if (val == null) continue;
                val = val.replaceAll("\\s+", "");
                if (val.contains("序号") || val.contains("签到")) {
                    seqCol = c;
                    break;
                }
            }

            // 2. 「部门/所属」：双向扫，取距离 nameCol 最近的列
            //    （左半组部门在姓名左侧 [部门, 姓名]；右半组部门在姓名右侧 [姓名, 部门]）
            deptCol = findNearestColumn(row, nameCol, "部门", "所属");

            // 3. 「考核/分数/成绩」：双向扫，取距离 nameCol 最近的列
            //    （左半组考核在姓名右侧 [姓名, 考核]；右半组考核在姓名右侧 [姓名, 考核]）
            scoreCol = findNearestColumn(row, nameCol, "考核", "分数", "成绩");

            // 4. 「备注/说明」：双向扫，取距离 nameCol 最近的列
            //    用于识别"授课人/未参加/缺席/缺考"等缺席标记
            // [扩展]兼容更多表头:备注 / 附注 / 说明 / 其它 / 其他 / note / notes / remark / remarks
            // [禁用]不能用 "情况" 关键字:培训考核记录表.xls 的"考核情况"会同时命中分数列
            remarkCol = findNearestColumn(row, nameCol,
                    "备注", "附注", "说明", "其它", "其他",
                    "note", "notes", "remark", "remarks");

            // 兜底默认值
            if (seqCol == -1) seqCol = Math.max(0, nameCol - 2);
            if (deptCol == -1) deptCol = nameCol + 1;
            // scoreCol 允许 -1（无考核列）
            // remarkCol 允许 -1（无备注列）

            // [防御]禁止 remark 列和其他已识别列撞到同一列(历史上"情况"撞过"考核情况")
            //    否则会把分数复制到备注里
            if (remarkCol != -1 && (remarkCol == seqCol || remarkCol == nameCol
                    || remarkCol == scoreCol || remarkCol == deptCol)) {
                logger.warn("【列识别】姓名列={} 备注列={} 与其他列冲突,放弃当作备注列", nameCol, remarkCol);
                remarkCol = -1;
            }

            groups.add(new int[]{seqCol, nameCol, scoreCol, deptCol, remarkCol});
        }
        return groups;
    }

    /**
     * 在表头行中找距离 nameCol 最近、且单元格文本包含任一关键字的列
     * 双向扫描，取 |col - nameCol| 最小的命中列
     * 用于「部门/考核」等左右两侧都可能出现的列识别
     */
    private int findNearestColumn(org.apache.poi.ss.usermodel.Row row, int nameCol, String... keywords) {
        int bestCol = -1;
        int bestDist = Integer.MAX_VALUE;
        int last = row.getLastCellNum();
        for (int c = 0; c < last; c++) {
            if (c == nameCol) continue;
            String val = getCellString(row.getCell(c));
            if (val == null) continue;
            String trimmed = val.replaceAll("\\s+", "");
            boolean match = false;
            for (String kw : keywords) {
                if (trimmed.contains(kw)) { match = true; break; }
            }
            if (!match) continue;
            int dist = Math.abs(c - nameCol);
            if (dist < bestDist) {
                bestDist = dist;
                bestCol = c;
            }
        }
        return bestCol;
    }

    /**
     * 从 Excel 前15行提取培训元数据
     * 支持多种格式：
     * 1. 横向布局：标签在A列，值在B列（如"培训主题" | "安全生产培训"）
     * 2. 键值对布局：标签和值在同一单元格（如"培训主题：安全生产培训"）
     * 3. 多种关键字变体（如"主题"、"培训名称"、"培训人"等）
     */
    private void extractExcelMetadata(Sheet sheet, BizTrainOcrBatch batch) {
        int scanLimit = Math.min(sheet.getLastRowNum(), 25);

        logger.info("【Excel元数据提取】开始扫描，共{}行", scanLimit + 1);

        for (int i = 0; i <= scanLimit; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row == null) continue;

            // 安全获取列数
            int physicalCells = row.getPhysicalNumberOfCells();
            if (physicalCells <= 0) continue;
            int lastCell = Math.min(physicalCells, 12);

            for (int col = 0; col < lastCell; col++) {
                String cellVal = getCellString(row.getCell(col));
                if (StringUtils.isEmpty(cellVal)) continue;

                String val = cellVal.trim();
                String nextVal = (col + 1 < lastCell) ? getCellString(row.getCell(col + 1)) : "";

                // 调试：打印每个单元格
                logger.info("【元数据扫描】行{}列{}: val='{}', nextVal='{}'", i, col, val, nextVal);

                // ========== 培训主题 ==========
                // 横向布局：标签在A列，值在C列（标签在列0，值在列2）
                if (val.equals("培训主题") || val.equals("培训名称") || val.equals("课程名称")) {
                    // 使用新方法：从标签列之后查找第一个非空值（跳过B列的合并单元格空白）
                    String subject = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(subject) && StringUtils.isEmpty(batch.getOcrSubject())) {
                        batch.setOcrSubject(subject);
                    }
                } else if (val.contains("培训主题") || val.contains("课程名称")) {
                    String subject = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(subject) && StringUtils.isEmpty(batch.getOcrSubject())) {
                        batch.setOcrSubject(subject);
                    }
                }

                // ========== 培训内容 ==========
                if (val.equals("培训内容") || val.equals("课程内容") || val.equals("内容大纲")) {
                    String content = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(content) && StringUtils.isEmpty(batch.getOcrTrainContent())) {
                        batch.setOcrTrainContent(content);
                    }
                } else if (val.contains("培训内容")) {
                    String content = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(content) && StringUtils.isEmpty(batch.getOcrTrainContent())) {
                        batch.setOcrTrainContent(content);
                    }
                }

                // ========== 培训部门 / 主办部门 ==========
                if (val.equals("培训部门") || val.equals("主办部门")) {
                    String dept = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(dept) && StringUtils.isEmpty(batch.getOcrHostDept())) {
                        batch.setOcrHostDept(dept);
                    }
                } else if (val.contains("培训部门") || val.contains("主办部门")) {
                    String dept = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(dept) && StringUtils.isEmpty(batch.getOcrHostDept())) {
                        batch.setOcrHostDept(dept);
                    }
                }

                // ========== 原始日期 / 培训日期 ==========
                if (val.equals("原始日期") || val.equals("培训日期") || val.equals("日期")) {
                    String date = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(date) && StringUtils.isEmpty(batch.getOcrDate())) {
                        batch.setOcrDate(date);
                    }
                } else if (val.contains("原始日期") || val.contains("培训日期")) {
                    String date = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(date) && StringUtils.isEmpty(batch.getOcrDate())) {
                        batch.setOcrDate(date);
                    }
                }

                // ========== 培训学时 ==========
                if (val.equals("培训学时") || val.equals("学时")) {
                    String hours = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(hours) && StringUtils.isEmpty(batch.getOcrTrainHours())) {
                        batch.setOcrTrainHours(hours);
                    }
                } else if (val.contains("培训学时") || val.contains("学时")) {
                    String hours = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(hours) && StringUtils.isEmpty(batch.getOcrTrainHours())) {
                        batch.setOcrTrainHours(hours);
                    }
                }

                // ========== 培训地点 ==========
                if (val.equals("培训地点") || val.equals("培训地址") || val.equals("地点")) {
                    String location = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(location) && StringUtils.isEmpty(batch.getOcrLocation())) {
                        batch.setOcrLocation(location);
                    }
                } else if (val.contains("培训地点") || val.contains("培训地址")) {
                    String location = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(location) && StringUtils.isEmpty(batch.getOcrLocation())) {
                        batch.setOcrLocation(location);
                    }
                }

                // ========== 授课人 / 培训人 ==========
                if (val.equals("授课人") || val.equals("讲师")) {
                    String speaker = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(speaker) && StringUtils.isEmpty(batch.getOcrSpeaker())) {
                        batch.setOcrSpeaker(speaker);
                    }
                } else if (val.contains("授课人") || val.contains("讲师")) {
                    String speaker = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(speaker) && StringUtils.isEmpty(batch.getOcrSpeaker())) {
                        batch.setOcrSpeaker(speaker);
                    }
                }

                // ========== 应到人数 / 应培训人数 ==========
                // 兼容"应到人数"和"应培训人数"两种标签
                String valNoSpace = val.replaceAll("\\s+", "");
                if (valNoSpace.equals("应到人数") || valNoSpace.equals("应培训人数")) {
                    String count = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(count) && batch.getOcrTargetCount() == null) {
                        try {
                            batch.setOcrTargetCount(Integer.parseInt(count.replaceAll("[^0-9]", "")));
                            logger.info("【Excel元数据提取】应到人数={}", batch.getOcrTargetCount());
                        } catch (NumberFormatException ignore) { /* 非数字则忽略 */ }
                    }
                } else if (valNoSpace.contains("应到人数") || valNoSpace.contains("应培训人数")) {
                    String count = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(count) && batch.getOcrTargetCount() == null) {
                        try {
                            batch.setOcrTargetCount(Integer.parseInt(count.replaceAll("[^0-9]", "")));
                            logger.info("【Excel元数据提取】应到人数={}", batch.getOcrTargetCount());
                        } catch (NumberFormatException ignore) { /* 非数字则忽略 */ }
                    }
                }

                // ========== 实到人数 / 实际培训人数 ==========
                // 兼容"实到人数"和"实际培训人数"(后者原表中含不规则空格"实际培   训人数")
                if (valNoSpace.equals("实到人数") || valNoSpace.equals("实际培训人数")) {
                    String count = extractFirstValueAfterLabel(row, col);
                    if (StringUtils.isNotEmpty(count) && batch.getOcrActualCount() == null) {
                        try {
                            batch.setOcrActualCount(Integer.parseInt(count.replaceAll("[^0-9]", "")));
                            logger.info("【Excel元数据提取】实到人数={}", batch.getOcrActualCount());
                        } catch (NumberFormatException ignore) { /* 非数字则忽略 */ }
                    }
                } else if (valNoSpace.contains("实到人数") || valNoSpace.contains("实际培训人数")) {
                    String count = extractKeyValue(val);
                    if (StringUtils.isNotEmpty(count) && batch.getOcrActualCount() == null) {
                        try {
                            batch.setOcrActualCount(Integer.parseInt(count.replaceAll("[^0-9]", "")));
                            logger.info("【Excel元数据提取】实到人数={}", batch.getOcrActualCount());
                        } catch (NumberFormatException ignore) { /* 非数字则忽略 */ }
                    }
                }
            }
        }

        // 如果培训内容为空，用主题填充
        if (StringUtils.isEmpty(batch.getOcrTrainContent()) && StringUtils.isNotEmpty(batch.getOcrSubject())) {
            batch.setOcrTrainContent(batch.getOcrSubject());
        }

        // 打印最终结果
        logger.info("【Excel元数据提取】结果: subject='{}', content='{}', date='{}', location='{}', hours='{}', dept='{}', speaker='{}'",
            batch.getOcrSubject(), batch.getOcrTrainContent(), batch.getOcrDate(), batch.getOcrLocation(),
            batch.getOcrTrainHours(), batch.getOcrHostDept(), batch.getOcrSpeaker());

        // 回写批次主表
        bizTrainOcrBatchMapper.updateBizTrainOcrBatch(batch);
    }

    /**
     * 基于源文件重新 OCR 提取元数据
     * 用于历史批次补提应到/实到/培训日期等字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reextractBatch(Long batchId) {
        BizTrainOcrBatch batch = bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(batchId);
        if (batch == null) {
            logger.warn("【重新提取】批次不存在: batchId={}", batchId);
            return 0;
        }
        if (!"1".equals(batch.getStatus())) {
            logger.warn("【重新提取】批次状态非待校对，跳过: batchId={}, status={}", batchId, batch.getStatus());
            return 0;
        }

        // 查找该批次的源文件（取第一个 log 记录的绝对路径）
        BizTrainOcrLog query = new BizTrainOcrLog();
        query.setBatchId(batchId);
        List<BizTrainOcrLog> logs = bizTrainOcrLogMapper.selectBizTrainOcrLogList(query);
        if (logs == null || logs.isEmpty()) {
            logger.warn("【重新提取】批次无源文件记录: batchId={}", batchId);
            return 0;
        }
        String filePath = logs.get(0).getFileUrl();
        if (StringUtils.isEmpty(filePath)) {
            logger.warn("【重新提取】源文件路径为空: batchId={}", batchId);
            return 0;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            logger.warn("【重新提取】源文件不存在: path={}", filePath);
            return 0;
        }
        // 仅处理 Excel
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".xls") && !name.endsWith(".xlsx")) {
            logger.warn("【重新提取】源文件非 Excel 格式: path={}", filePath);
            return 0;
        }

        logger.info("【重新提取】开始处理 batchId={}, file={}", batchId, filePath);
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            // 复用现有提取方法（已含 应到/实到/培训日期 修复）
            extractExcelMetadata(sheet, batch);
            logger.info("【重新提取】完成 batchId={}, targetCount={}, actualCount={}, ocrDate={}",
                        batchId, batch.getOcrTargetCount(), batch.getOcrActualCount(), batch.getOcrDate());
            return 1;
        } catch (Exception e) {
            logger.error("【重新提取】异常 batchId={}", batchId, e);
            throw new ServiceException("重新提取失败: " + e.getMessage());
        }
    }

    /**
     * 提取键值对格式的值（如 "培训主题：安全生产培训"）
     */
    private String extractKeyValue(String cellVal) {
        for (String sep : new String[]{"：", ":", "｜", "|"}) {
            int idx = cellVal.indexOf(sep);
            if (idx != -1 && idx < cellVal.length() - 1) {
                String value = cellVal.substring(idx + 1).trim();
                logger.info("【extractKeyValue】分隔符='{}', 原值='{}', 提取='{}'", sep, cellVal, value);
                return cleanExtractedValue(value);
            }
        }
        logger.info("【extractKeyValue】未找到分隔符，返回空");
        return "";
    }

    /**
     * 提取标签右侧单元格的值
     * 改进：跳过空单元格，找到该行中标签之后的第一非空值
     */
    private String extractRightCellValue(String nextVal) {
        if (StringUtils.isEmpty(nextVal)) {
            return "";
        }
        String result = cleanExtractedValue(nextVal.trim());
        return result;
    }

    /**
     * 从整行中查找标签之后的第一个非空值
     * 改进：使用 getLastCellNum() 而非 getPhysicalNumberOfCells()，
     * 因为合并单元格会导致后者只返回1（只有标签单元格有物理数据）
     */
    private String extractFirstValueAfterLabel(Row row, int labelCol) {
        if (row == null) return "";
        // 使用 getLastCellNum() 而非 getPhysicalNumberOfCells()
        // 前者返回实际列数（含空单元格），后者只统计有数据的单元格
        int lastCell = row.getLastCellNum();

        // 从标签列的下一列开始查找，直到遇到下一个标签为止
        for (int col = labelCol + 1; col < lastCell; col++) {
            String cellVal = getCellString(row.getCell(col));
            if (StringUtils.isEmpty(cellVal)) continue;

            // 跳过可能的后续标签（如"培训部门"行的"培训主题"列可能是空的）
            String trimmed = cellVal.trim();
            if (trimmed.contains("主题") || trimmed.contains("部门") || trimmed.contains("日期")
                || trimmed.contains("学时") || trimmed.contains("地点") || trimmed.contains("方式")
                || trimmed.contains("内容") || trimmed.contains("人数") || trimmed.contains("授课")
                || trimmed.equals("序号") || trimmed.equals("姓名") || trimmed.equals("签到")
                || trimmed.equals("备注") || trimmed.equals("部门")) {
                // 遇到下一个标签，停止
                break;
            }

            String result = cleanExtractedValue(trimmed);
            if (StringUtils.isNotEmpty(result)) {
                logger.info("【extractFirstValueAfterLabel】标签列={}, 找到值='{}' at 列{}", labelCol, result, col);
                return result;
            }
        }
        logger.info("【extractFirstValueAfterLabel】标签列={}, 未找到值", labelCol);
        return "";
    }

    /**
     * 清洗提取的值，去除无意义的前缀
     */
    private String cleanExtractedValue(String value) {
        if (StringUtils.isEmpty(value)) return "";
        String v = value.trim();
        // 去除常见前缀符号
        v = v.replaceAll("^[：:：\\s]+", "");
        // 如果值太短但可能是纯数字（如"2"、"1.5"），保留它
        if (v.length() < 2 && !v.matches("^\\d+\\.?\\d*$")) return "";
        return v;
    }

    /**
     * 智能识别签到表数据起始行
     * 策略：扫描前20行，找姓名字段（包含"姓名"关键字）或序号（纯数字）所在的行
     */
    private int findFirstDataRow(Sheet sheet) {
        int lastRow = Math.min(sheet.getLastRowNum(), 20);
        for (int i = 0; i <= lastRow; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row == null) continue;

            String cell0 = getCellString(row.getCell(0));
            String cell1 = getCellString(row.getCell(1));

            // 策略1：序号列是纯数字（如 1, 2, 3...）→ 这就是数据起始行
            if (cell0 != null && cell0.matches("\\d{1,4}")) {
                return i;
            }

            // 策略2：第0列包含"姓名"关键字 → 跳过表头，取下一行
            if (cell0 != null && cell0.contains("姓名")) {
                return i + 1;
            }

            // 策略3：第1列包含"姓名"关键字（常见于横向布局）
            if (cell1 != null && cell1.contains("姓名")) {
                return i + 1;
            }
        }
        // 默认跳过第1行（传统做法）
        return 1;
    }

    /**
     * 规范化 Excel 中的考核成绩
     * 纯数字 → 直接返回（如 "85"）
     * √/✓/空白 → 有序号则存"空白"，否则跳过
     * 缺席/缺考 → "空白"
     * 其他文字评价 → 保留原文
     * @return 标准化后的成绩，null 表示跳过该行
     */
    private String normalizeExcelScore(String score, String seq) {
        if (StringUtils.isEmpty(score)) {
            return null; // 成绩为空则不填，保持null
        }
        // 纯数字 → 直接返回（实际分数）
        if (score.trim().matches("\\d+(\\.\\d+)?")) {
            return score.trim();
        }
        // 签到标记（各种√变体）→ null（无成绩）
        if (score.trim().matches("^[√✓✔☑＋+.。／/\\\\\\s]+$")) {
            return null;
        }
        // 缺考/缺席/未参加 → null（无成绩）
        String t = score.trim();
        if (t.equals("缺席") || t.equals("缺考") || t.equals("未参加")) {
            return null;
        }
        // 其他文字评价保留原文
        return score.trim();
    }

    /**
     * 判断 OCR 明细是否标记为"缺席"(不计入实到人数)
     * 关键字:未参加 / 缺席 / 缺考 / 授课人
     * 注:授课人虽然没"缺勤",但同样不签到不进档案,业务上视同缺席
     * @param remark parsedRemark 字段值(可为 null)
     * @return true=该人员不计入实到
     */
    public static boolean isAbsentRemark(String remark) {
        if (StringUtils.isEmpty(remark)) {
            return false;
        }
        String r = remark.trim();
        return r.contains("未参加")
                || r.contains("缺席")
                || r.contains("缺考")
                || r.contains("授课人");
    }

    /**
     * 编排档案备注的最终值。
     * 优先级:OCR 备注(每人各异) > 日期归一化失败的兜底信息(批次级一次性,所有人共享)
     * 两者都有时用 " | " 拼接,保留两段信息方便前端/审计核对。
     * 为什么不能是覆盖:旧实现 dateRemark 一来就把所有人覆盖,导致 /train/archive 整页空白;
     *   现在哪怕批次日期解析挂了,每个人的真备注照样能落进去。
     *
     * @param ocrRemark  单人 OCR 识别出的备注(可空)
     * @param dateRemark 批次级日期解析失败的兜底文案(可空,所有人共享)
     * @return 最终写入 archive.remark 的字符串;两者都空时返回 null(让 SQL 走 NULL)
     */
    public static String composeArchiveRemark(String ocrRemark, String dateRemark) {
        boolean hasOcr = StringUtils.isNotEmpty(ocrRemark);
        boolean hasDate = StringUtils.isNotEmpty(dateRemark);
        if (hasOcr && hasDate) {
            return dateRemark + " | " + ocrRemark.trim();
        }
        if (hasOcr) {
            return ocrRemark.trim();
        }
        if (hasDate) {
            return dateRemark.trim();
        }
        return null;
    }

    /** 安全读取单元格字符串值 */
    private String getCellString(Cell cell) {
        if (cell == null) return null;

        // 先判断单元格的数据类型（兼容 POI 3.15+ 的 API）
        CellType cellType = cell.getCellType();
        if (cellType == CellType.STRING) {
            String v = cell.getStringCellValue();
            return (v != null && !v.trim().isEmpty()) ? v.trim() : null;
        }
        if (cellType == CellType.NUMERIC) {
            // 检查是否是日期类型
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                java.util.Date date = cell.getDateCellValue();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(date);
            }
            // 纯数字
            double v = cell.getNumericCellValue();

            // 探测 Excel 日期序列号（1900 日期系统）
            // 范围 [10000, 100000] 覆盖 1927-2173 年，
            // 避免把 1.0(学时) / 36.0(应到人数) 等小整数误转为日期
            if (v >= 10000 && v <= 100000 && v == Math.floor(v)) {
                try {
                    java.util.Date d = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(v);
                    if (d != null) {
                        logger.debug("【getCellString】将数值 {} 解析为 Excel 日期序列号", v);
                        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
                    }
                } catch (Exception ignore) { /* 解析失败走数字分支 */ }
            }

            if (v == Math.floor(v) && v <= Integer.MAX_VALUE) {
                return String.valueOf((int) v);
            }
            return String.valueOf(v);
        }
        if (cellType == CellType.BLANK) {
            return null;
        }
        // 处理公式类型的单元格（公式返回字符串结果）
        if (cellType == CellType.FORMULA) {
            try {
                return cell.getStringCellValue();
            } catch (Exception e) {
                // 公式返回数字（如日期公式）
                try {
                    double v = cell.getNumericCellValue();
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        java.util.Date date = cell.getDateCellValue();
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        return sdf.format(date);
                    }
                    // 公式返回的日期序列号同样需要探测
                    if (v >= 10000 && v <= 100000 && v == Math.floor(v)) {
                        try {
                            java.util.Date d = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(v);
                            if (d != null) {
                                logger.debug("【getCellString】将公式数值 {} 解析为 Excel 日期序列号", v);
                                return new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
                            }
                        } catch (Exception ignore) { /* 解析失败走数字分支 */ }
                    }
                    if (v == Math.floor(v)) {
                        return String.valueOf((int) v);
                    }
                    return String.valueOf(v);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 同步员工历史快照到 sys_user_history 表
     * 确保 /train/archive 页面通过 LEFT JOIN sys_user_history 能正确获取员工姓名和归属部门
     * 使用 recordUserChange 方法：自动检查是否已有记录，如需要则关闭旧记录并插入新记录
     */
    private void syncUserHistoryForArchive(SysUser user, String deptName, java.util.Date trainDate) {
        if (user == null) return;

        try {
            // 先检查该员工是否已有历史记录
            SysUserHistory existingHistory = sysUserHistoryService.selectUserSnapshot(user.getUserId(), trainDate);

            if (existingHistory == null) {
                // 首次：初始化历史记录（不关闭旧记录）
                sysUserHistoryService.initUserHistory(
                    user.getUserId(),
                    user.getNickName(),
                    user.getDeptId(),
                    deptName,
                    null,  // postId
                    "",    // postName
                    trainDate
                );
                logger.info("【OCR建档案-历史快照】初始化: userId={}, userName={}, deptName={}, trainDate={}",
                    user.getUserId(), user.getNickName(), deptName, trainDate);
            } else {
                // 非首次：记录变更（关闭旧记录，插入新记录）
                sysUserHistoryService.recordUserChange(
                    user.getUserId(),
                    user.getNickName(),
                    user.getDeptId(),
                    deptName,
                    null,  // postId
                    "",    // postName
                    trainDate
                );
                logger.info("【OCR建档案-历史快照】变更: userId={}, userName={}, deptName={}, trainDate={}",
                    user.getUserId(), user.getNickName(), deptName, trainDate);
            }
        } catch (Exception e) {
            logger.error("【OCR建档案-历史快照】同步失败: userId={}, error={}", user.getUserId(), e.getMessage());
        }
    }
}