package com.ruoyi.biz.ocr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.biz.event.OcrBatchReadyEvent;
import com.ruoyi.biz.domain.BizTrainOcrBatch;
import com.ruoyi.biz.domain.BizTrainOcrLog;
import com.ruoyi.biz.domain.BizTrainOcrResult;
import com.ruoyi.biz.mapper.BizTrainOcrBatchMapper;
import com.ruoyi.biz.mapper.BizTrainOcrLogMapper;
import com.ruoyi.biz.mapper.BizTrainOcrResultMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * 异步 OCR 批次处理引擎
 */
@Service
public class AsyncOcrProcessService {

    private static final Logger log = LoggerFactory.getLogger(AsyncOcrProcessService.class);

    @Autowired
    private BizTrainOcrBatchMapper batchMapper;
    @Autowired
    private BizTrainOcrLogMapper logMapper;
    @Autowired
    private BizTrainOcrResultMapper resultMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private BaiduOcrService baiduOcrService;

    /**
     * 事务提交后触发异步 OCR 处理。
     * 解决 @Async 直接调用时父事务未提交、日志记录查不到的问题。
     */
    @Async("threadPoolTaskExecutor")
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBatchReady(OcrBatchReadyEvent event) {
        processBatchOcr(event.getBatchId());
    }

    public void processBatchOcr(Long batchId) {
        log.info("========== 开始异步解析 OCR 批次: {} ==========", batchId);

        BizTrainOcrBatch batch = batchMapper.selectBizTrainOcrBatchByBatchId(batchId);
        if (batch == null) {
            log.warn("批次 {} 不存在，跳过处理", batchId);
            return;
        }
        // 保护：已结算/作废的 batch 不触发重新 OCR，避免覆盖状态
        if ("2".equals(batch.getStatus()) || "3".equals(batch.getStatus())) {
            log.warn("批次 {} 状态为 {}（已结算/作废），跳过重新 OCR", batchId, batch.getStatus());
            return;
        }

        BizTrainOcrLog queryLog = new BizTrainOcrLog();
        queryLog.setBatchId(batchId);
        List<BizTrainOcrLog> logs = logMapper.selectBizTrainOcrLogList(queryLog);

        if (logs == null || logs.isEmpty()) {
            log.warn("批次 {} 没有文件记录，标记为异常", batchId);
            batch.setStatus("3");
            batch.setRemark("批次无文件记录");
            batchMapper.updateBizTrainOcrBatch(batch);
            return;
        }

        List<SysUser> allSystemUsers = userMapper.selectUserList(new SysUser());

        for (int i = 0; i < logs.size(); i++) {
            BizTrainOcrLog currentLog = logs.get(i);
            // 跳过已成功处理的 log（高拍仪分次上传场景下，旧 log 不需要重复 OCR）
            if ("1".equals(currentLog.getStatus())) {
                log.debug("logId={} 已处理成功，跳过", currentLog.getLogId());
                continue;
            }
            try {
                processSinglePage(currentLog, batch, allSystemUsers);
            } catch (Exception e) {
                // 单页失败只记录，不中断整批
                log.error("第 {}/{} 页处理异常: {}", i + 1, logs.size(), e.getMessage());
                // 仅在原状态不是"1"（已成功）时才覆盖为"2"（异常），防止 race condition 误覆盖
                if (!"1".equals(currentLog.getStatus())) {
                    currentLog.setStatus("2");
                    currentLog.setRemark("解析异常: " + e.getMessage());
                    try {
                        logMapper.updateBizTrainOcrLog(currentLog);
                    } catch (Exception updateEx) {
                        log.error("更新日志状态失败: {}", updateEx.getMessage());
                    }
                }
            }
        }

        // 汇总所有页的有效记录数
        List<BizTrainOcrLog> updatedLogs = logMapper.selectBizTrainOcrLogList(queryLog);
        int totalActualCount = updatedLogs.stream()
                .filter(l -> "1".equals(l.getStatus()))
                .mapToInt(l -> l.getOcrActualCount() != null ? l.getOcrActualCount() : 0)
                .sum();

        if (totalActualCount == 0) {
            batch.setStatus("3");
            batch.setRemark("OCR 解析失败：所有 " + updatedLogs.size() + " 页均未识别到有效人员明细。常见原因：图片模糊、表格无表头行、EXIF 方向异常。请重新拍照或人工补录。");
        } else {
            batch.setStatus("1");
            batch.setRemark("共解析到 " + totalActualCount + " 条记录（部分页面可能解析失败，已跳过）");
        }
        batchMapper.updateBizTrainOcrBatch(batch);
        log.info("========== OCR 批次 {} 解析完成，共 {} 条记录 ==========", batchId, totalActualCount);
    }

    /**
     * 处理单页（每页独立事务，避免单页失败污染整批）
     */
    @Transactional(rollbackFor = Exception.class)
    public void processSinglePage(BizTrainOcrLog currentLog, BizTrainOcrBatch batch, List<SysUser> allSystemUsers) {
        String localPath = currentLog.getFileUrl();
        if (localPath.startsWith("/profile")) {
            localPath = localPath.replaceFirst("/profile", RuoYiConfig.getProfile());
        }
        log.info("正在解析页 logId={}, 路径: {}", currentLog.getLogId(), localPath);

        // 1. 调用 OCR（内置超时 + 重试 + Token 缓存）
        JSONObject ocrResult = baiduOcrService.recognizeTable(localPath);

        // DEBUG: 记录完整 OCR 返回结构
        log.info("OCR返回原始JSON: {}", ocrResult.toString());
        if (ocrResult.has("error_code")) {
            log.error("OCR API返回错误: code={}, msg={}", ocrResult.optInt("error_code"), ocrResult.optString("error_msg"));
        }

        // 2. 无表格结构时标记失败，不抛异常
        // 检查 result 包裹格式或直接返回格式
        JSONArray tablesArray = null;
        try {
            if (ocrResult.has("result")) {
                tablesArray = ocrResult.getJSONObject("result").getJSONArray("tables_result");
            } else if (ocrResult.has("tables_result")) {
                tablesArray = ocrResult.getJSONArray("tables_result");
            }
        } catch (Exception e) {
            log.warn("解析表格结果异常: {}", e.getMessage());
        }

        if (tablesArray == null || tablesArray.length() == 0) {
            String errMsg = ocrResult.has("error_msg") ? ocrResult.optString("error_msg") : "无表格结构";
            currentLog.setStatus("2");
            currentLog.setRemark("未识别到表格结构：" + errMsg + "，请确认上传的是签到表图片");
            logMapper.updateBizTrainOcrLog(currentLog);
            return;
        }

        JSONArray body = tablesArray.getJSONObject(0).getJSONArray("body");
        List<OcrCell> cellList = new ArrayList<>();
        for (int j = 0; j < body.length(); j++) {
            JSONObject item = body.getJSONObject(j);
            cellList.add(new OcrCell(item.getInt("row_start"), item.getInt("col_start"), item.optString("words", "")));
        }

        // DEBUG: 记录 cell 列表的行列范围和样本
        int debugMaxRow = cellList.stream().mapToInt(c -> c.row).max().orElse(0);
        int maxCol = cellList.stream().mapToInt(c -> c.col).max().orElse(0);
        log.debug("OCR识别到表格: {}行 x {}列, 共{}个单元格", debugMaxRow + 1, maxCol + 1, cellList.size());
        // 打印前20个cell样本
        StringBuilder sample = new StringBuilder("前20个单元格: ");
        for (int i = 0; i < Math.min(20, cellList.size()); i++) {
            OcrCell c = cellList.get(i);
            sample.append(String.format("[%d,%d:'%s'] ", c.row, c.col, c.text.length() > 10 ? c.text.substring(0, 10) + "..." : c.text));
        }
        log.info(sample.toString());

        // 3. 从第一页提取培训元数据
        // 仅当 batch 还没有 ocrSubject 时才提取，避免左右分栏分两页扫时重复提取并覆盖
        boolean isFirstPage = cellList.stream().anyMatch(c -> c.text.replaceAll("\\s+", "").contains("培训主题"));
        if (isFirstPage && StringUtils.isEmpty(batch.getOcrSubject())) {
            String subject = findValueSmart(cellList, "培训主题");
            String date = findValueSmart(cellList, "培训日期");
            String hostDept = findValueSmart(cellList, "培训部门");
            String speaker = findValueSmart(cellList, "授课人");
            String location = findValueSmart(cellList, "培训地点");
            String hours = findValueSmart(cellList, "培训学时");
            String content = findValueSmart(cellList, "培训内容");

            if (StringUtils.isNotEmpty(subject)) batch.setOcrSubject(subject);
            // 日期归一化放到 confirmAndGenerate 统一处理,这里保留原文,
            // 这样无法解析时下游仍能拿到原始字符串落到 remark 并打 WARN
            if (StringUtils.isNotEmpty(date)) batch.setOcrDate(date);
            if (StringUtils.isNotEmpty(hostDept)) batch.setOcrHostDept(hostDept);
            if (StringUtils.isNotEmpty(speaker)) batch.setOcrSpeaker(speaker);
            if (StringUtils.isNotEmpty(location)) batch.setOcrLocation(location);
            if (StringUtils.isNotEmpty(hours)) batch.setOcrTrainHours(hours);
            batch.setOcrTrainContent(StringUtils.isNotEmpty(content) ? content : subject);

            // 应到人数(优先"应到人数",失败回退"应培训人数")— 与 Excel 路径行为对齐
            if (batch.getOcrTargetCount() == null) {
                String targetStr = findValueSmart(cellList, "应到人数");
                if (StringUtils.isEmpty(targetStr)) targetStr = findValueSmart(cellList, "应培训人数");
                if (StringUtils.isNotEmpty(targetStr)) {
                    try {
                        batch.setOcrTargetCount(Integer.parseInt(targetStr.replaceAll("[^0-9]", "")));
                        log.info("【OCR元数据提取】应到人数={}", batch.getOcrTargetCount());
                    } catch (NumberFormatException ignore) { /* 非数字忽略,保持 null 让下游 fallback */ }
                }
            }

            // 实到人数(优先"实到人数",失败回退"实际培训人数")— 与 Excel 路径行为对齐
            if (batch.getOcrActualCount() == null) {
                String actualStr = findValueSmart(cellList, "实到人数");
                if (StringUtils.isEmpty(actualStr)) actualStr = findValueSmart(cellList, "实际培训人数");
                if (StringUtils.isNotEmpty(actualStr)) {
                    try {
                        batch.setOcrActualCount(Integer.parseInt(actualStr.replaceAll("[^0-9]", "")));
                        log.info("【OCR元数据提取】实到人数={}", batch.getOcrActualCount());
                    } catch (NumberFormatException ignore) { /* 非数字忽略,保持 null 让下游 fallback */ }
                }
            }

            batchMapper.updateBizTrainOcrBatch(batch);
        }

        // 4. 找人员表头行
        int anchorRow = findAnchorRow(cellList);
        log.info("查找表头行结果: anchorRow={}", anchorRow);
        if (anchorRow == -1) {
            // DEBUG: 打印所有行首的cell文本，帮助定位问题
            Map<Integer, List<OcrCell>> rows = cellList.stream().collect(Collectors.groupingBy(c -> c.row));
            StringBuilder rowInfo = new StringBuilder("各行首列内容: ");
            for (Map.Entry<Integer, List<OcrCell>> entry : rows.entrySet()) {
                String firstCells = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(c -> c.col))
                    .limit(5)
                    .map(c -> "'" + c.text + "'")
                    .collect(Collectors.joining(", "));
                rowInfo.append(String.format("Row%d: [%s]; ", entry.getKey(), firstCells));
            }
            log.info(rowInfo.toString());

            currentLog.setStatus("2");
            currentLog.setRemark("未找到人员名单表头（缺少姓名或部门列）");
            logMapper.updateBizTrainOcrLog(currentLog);
            return;
        }

        int leftSeqCol = findColIndex(cellList, anchorRow, "序号", 0);
        int leftNameCol = findColIndex(cellList, anchorRow, "姓名", 0);
        int leftDeptCol = findColIndex(cellList, anchorRow, "部门", 0);
        // 优先找"考核成绩"/"分数"列（实际分数），再找"考核情况"（√ 或 空白），最后才用"签到"
        int leftMarkCol = findColIndex(cellList, anchorRow, "考核成绩", 0);
        if (leftMarkCol == -1) leftMarkCol = findColIndex(cellList, anchorRow, "分数", 0);
        if (leftMarkCol == -1) leftMarkCol = findColIndex(cellList, anchorRow, "考核情况", 0);
        if (leftMarkCol == -1) leftMarkCol = findColIndex(cellList, anchorRow, "签到", 0);

        // 【智能列交换】如果序号列内容是人名，姓名列内容是数字，则交换
        boolean seqIsName = hasChineseNameInColumn(cellList, anchorRow + 1, leftSeqCol, 3);
        boolean nameIsNum = hasNumericInColumn(cellList, anchorRow + 1, leftNameCol, 3);
        if (seqIsName && nameIsNum) {
            log.info("【智能校正】检测到序号列(col={})实际是人名，姓名列(col={})实际是数字，自动交换", leftSeqCol, leftNameCol);
            int temp = leftSeqCol;
            leftSeqCol = leftNameCol;
            leftNameCol = temp;
        }

        int rightStart = Math.max(Math.max(leftSeqCol, leftNameCol), Math.max(leftDeptCol, leftMarkCol)) + 1;
        int rightSeqCol = findColIndex(cellList, anchorRow, "序号", rightStart);
        int rightNameCol = findColIndex(cellList, anchorRow, "姓名", rightStart);
        int rightDeptCol = findColIndex(cellList, anchorRow, "部门", rightStart);
        int rightMarkCol = findColIndex(cellList, anchorRow, "考核成绩", rightStart);
        if (rightMarkCol == -1) rightMarkCol = findColIndex(cellList, anchorRow, "分数", rightStart);
        if (rightMarkCol == -1) rightMarkCol = findColIndex(cellList, anchorRow, "考核情况", rightStart);
        if (rightMarkCol == -1) rightMarkCol = findColIndex(cellList, anchorRow, "签到", rightStart);

        int maxRow = cellList.stream().mapToInt(c -> c.row).max().orElse(0);
        int pageCount = 0;

        for (int r = anchorRow + 1; r <= maxRow; r++) {
            pageCount += processAndSaveResult(batch.getBatchId(), currentLog.getLogId(), cellList, r, leftSeqCol, leftNameCol, leftDeptCol, leftMarkCol, allSystemUsers);
            if (rightNameCol != -1) {
                pageCount += processAndSaveResult(batch.getBatchId(), currentLog.getLogId(), cellList, r, rightSeqCol, rightNameCol, rightDeptCol, rightMarkCol, allSystemUsers);
            }
        }

        currentLog.setStatus("1");
        currentLog.setOcrActualCount(pageCount);
        logMapper.updateBizTrainOcrLog(currentLog);
    }

    private int processAndSaveResult(Long batchId, Long logId, List<OcrCell> cells, int row, int seqCol, int nameCol, int deptCol, int markCol, List<SysUser> allSystemUsers) {
        if (nameCol == -1) return 0;
        String seq = seqCol != -1 ? getCellValue(cells, row, seqCol).trim() : "";
        String nameRaw = getCellValue(cells, row, nameCol);
        // 只去首尾空白,不去数字/括号/标点 — 保留公司工号"官金梅(83)"原样入库
        // 之前 replaceAll("[\\s0-9.。,，]", "") 会把工号数字去掉,留下空括号 "官金梅()"
        String name = nameRaw.trim();
        String dept = getCellValue(cells, row, deptCol);
        String mark = markCol != -1 ? getCellValue(cells, row, markCol).trim() : "";

        // DEBUG: 记录每行解析情况
        log.debug("Row{} 解析: seq='{}', nameRaw='{}', name='{}', dept='{}', mark='{}'",
                 row, seq, nameRaw, name, dept, mark);

        // 姓名至少2个有效字符才入库
        if (name.length() < 2) return 0;

        // 规范化考核成绩：
        // 1. 有数字 → 直接存分数（如 "85"）
        // 2. "√" 或 "✓"（签到标记）→ 存 "空白"（签了名但没给分）
        // 3. 空字符串 → 有序号则存"空白"，否则跳过
        // 4. "缺席"/"缺考"/"未参加" → 存 "空白"
        String normalizedScore = normalizeScore(mark, seq);
        if (normalizedScore == null) return 0; // 无序号无标记 → 跳过

        BizTrainOcrResult result = new BizTrainOcrResult();
        result.setBatchId(batchId);
        result.setLogId(logId);
        result.setSignSeq(seq);
        result.setParsedName(name);
        result.setParsedDept(dept);
        result.setParsedScore(normalizedScore);
        result.setStatus("0");

        SysUser matchedUser = allSystemUsers.stream()
                .filter(u -> name.equals(u.getNickName()) || name.equals(u.getUserName()))
                .findFirst()
                .orElse(null);

        result.setIsNewUser(matchedUser == null ? "1" : "0");
        resultMapper.insertBizTrainOcrResult(result);
        return 1;
    }

    /**
     * 规范化考核成绩
     * @return 标准化后的成绩字符串，或 null 表示跳过（无序号且无任何标记）
     */
    private String normalizeScore(String mark, String seq) {
        // 空值处理
        if (StringUtils.isEmpty(mark)) {
            return seq.matches("\\d+") ? "空白" : null;
        }

        // 纯数字 → 直接返回（actual score from Excel）
        if (mark.matches("\\d+(\\.\\d+)?")) {
            return mark;
        }

        // 签到标记 → "空白"（没给考核成绩）
        if (mark.matches("[√✓✔☑＋＋＋]")) {
            return "空白";
        }

        // 缺考/缺席/未参加 → "空白"
        String trimmed = mark.trim();
        if (trimmed.matches("^[缺席未参加]+$")) {
            return "空白";
        }

        // 其他情况：返回原文（如 "良"、"及格" 等文字评价保留原文）
        return mark;
    }

    /**
     * 将 OCR 提取的日期字符串规范化为 yyyy-MM-dd
     * 与 Excel 路径 BizTrainOcrBatchServiceImpl.normalizeOcrDate 行为一致：
     *  - null/空 → 原样返回
     *  - 已是 yyyy-MM-dd 形式 → 原样返回
     *  - Excel 日期序列号（[10000, 100000] 的纯整数） → 转 yyyy-MM-dd
     *  - 其它 → 原样返回（兜底，避免吞用户输入）
     */
    private int findAnchorRow(List<OcrCell> cells) {
        Map<Integer, List<OcrCell>> rows = cells.stream().collect(Collectors.groupingBy(c -> c.row));
        for (Map.Entry<Integer, List<OcrCell>> entry : rows.entrySet()) {
            boolean hasName = entry.getValue().stream().anyMatch(c -> c.text.replaceAll("\\s+", "").contains("姓名"));
            boolean hasDept = entry.getValue().stream().anyMatch(c -> c.text.replaceAll("\\s+", "").contains("部门"));
            if (hasName && hasDept) return entry.getKey();
        }
        return -1;
    }

    private int findColIndex(List<OcrCell> cells, int row, String key, int minCol) {
        return cells.stream()
                .filter(c -> c.row == row && c.col >= minCol && c.text.replaceAll("\\s+", "").contains(key))
                .map(c -> c.col).findFirst().orElse(-1);
    }

    private String getCellValue(List<OcrCell> cells, int r, int c) {
        return cells.stream().filter(cell -> cell.row == r && cell.col == c)
                .findFirst().map(cell -> cell.text).orElse("");
    }

    private String findValueSmart(List<OcrCell> cells, String key) {
        String cleanKey = key.replaceAll("\\s+", "");
        for (OcrCell cell : cells) {
            String text = cell.text.replaceAll("\\s+", "");
            if (text.contains(cleanKey)) {
                int idx = text.indexOf("：");
                if (idx == -1) idx = text.indexOf(":");
                if (idx != -1 && idx < text.length() - 1) return text.substring(idx + 1);
                return cells.stream().filter(c -> c.row == cell.row && c.col > cell.col)
                        .sorted(Comparator.comparingInt(c -> c.col))
                        .map(c -> c.text).filter(StringUtils::isNotEmpty).findFirst().orElse("");
            }
        }
        return "";
    }

    private static class OcrCell {
        int row; int col; String text;
        public OcrCell(int r, int c, String t) { this.row = r; this.col = c; this.text = t; }
    }

    /**
     * 判断某列前N行是否包含中文姓名（2-4个汉字）
     */
    private boolean hasChineseNameInColumn(List<OcrCell> cells, int startRow, int col, int checkRows) {
        int count = 0;
        for (int r = startRow; r < startRow + checkRows && count < 2; r++) {
            String val = getCellValue(cells, r, col).trim();
            if (isChineseName(val)) count++;
        }
        return count >= 2; // 至少有2行是中文姓名才认为该列是姓名列
    }

    /**
     * 判断某列前N行是否包含数字序号
     */
    private boolean hasNumericInColumn(List<OcrCell> cells, int startRow, int col, int checkRows) {
        int count = 0;
        for (int r = startRow; r < startRow + checkRows && count < 2; r++) {
            String val = getCellValue(cells, r, col).trim();
            if (isNumericSequence(val)) count++;
        }
        return count >= 2; // 至少有2行是数字才认为该列是序号列
    }

    /**
     * 判断字符串是否为中文姓名（2-4个汉字，无其他字符）
     */
    private boolean isChineseName(String text) {
        if (StringUtils.isEmpty(text)) return false;
        String cleaned = text.replaceAll("[\\s0-9.。,，、]", "").trim();
        return cleaned.length() >= 2 && cleaned.length() <= 4 && cleaned.matches("[\\u4e00-\\u9fa5]+");
    }

    /**
     * 判断字符串是否为数字序号（如 1, 01, 001, 1. 等）
     */
    private boolean isNumericSequence(String text) {
        if (StringUtils.isEmpty(text)) return false;
        return text.trim().matches("^\\d+(\\.\\d+)?$");
    }
}
