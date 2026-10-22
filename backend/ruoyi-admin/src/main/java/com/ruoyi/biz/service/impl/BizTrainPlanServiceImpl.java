package com.ruoyi.biz.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.mapper.BizTrainPlanMapper;
import com.ruoyi.biz.service.IBizTrainPlanService;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.mapper.SysDeptMapper;

/**
 * 年度培训计划Service业务层处理
 *
 * @author admin
 * @date 2026-03-03
 */
@Service
public class BizTrainPlanServiceImpl implements IBizTrainPlanService
{
    @Autowired
    private BizTrainPlanMapper bizTrainPlanMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    /**
     * 查询年度培训计划
     *
     * @param planId 年度培训计划主键
     * @return 年度培训计划
     */
    @Override
    public BizTrainPlan selectBizTrainPlanByPlanId(Long planId)
    {
        return bizTrainPlanMapper.selectBizTrainPlanByPlanId(planId);
    }

    /**
     * 查询年度培训计划列表
     *
     * @param bizTrainPlan 年度培训计划
     * @return 年度培训计划
     */
    @Override
    public List<BizTrainPlan> selectBizTrainPlanList(BizTrainPlan bizTrainPlan)
    {
        // planMonth 可能为数字字符串（前端 el-select 1-12）或 "2026年6月"。
        // 归一化成纯月份数字（1-12 字符串），Mapper 用 OR 同时兼容新格式 "2026年X月" 和旧格式 "X月"。
        String pm = bizTrainPlan.getPlanMonth();
        if (pm != null && !pm.isEmpty()) {
            String trimmed = pm.trim();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2})月").matcher(trimmed);
            if (m.find()) {
                bizTrainPlan.setPlanMonth(m.group(1));
            }
        }
        return bizTrainPlanMapper.selectBizTrainPlanList(bizTrainPlan);
    }

    /**
     * 新增年度培训计划
     *
     * @param bizTrainPlan 年度培训计划
     * @return 结果
     */
    @Override
    public int insertBizTrainPlan(BizTrainPlan bizTrainPlan)
    {
        bizTrainPlan.setCreateTime(DateUtils.getNowDate());
        return bizTrainPlanMapper.insertBizTrainPlan(bizTrainPlan);
    }

    /**
     * 修改年度培训计划
     *
     * @param bizTrainPlan 年度培训计划
     * @return 结果
     */
    @Override
    public int updateBizTrainPlan(BizTrainPlan bizTrainPlan)
    {
        bizTrainPlan.setUpdateTime(DateUtils.getNowDate());
        return bizTrainPlanMapper.updateBizTrainPlan(bizTrainPlan);
    }

    /**
     * 批量删除年度培训计划
     *
     * @param planIds 需要删除的年度培训计划主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainPlanByPlanIds(Long[] planIds)
    {
        return bizTrainPlanMapper.deleteBizTrainPlanByPlanIds(planIds);
    }

    /**
     * 删除年度培训计划信息
     *
     * @param planId 年度培训计划主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainPlanByPlanId(Long planId)
    {
        return bizTrainPlanMapper.deleteBizTrainPlanByPlanId(planId);
    }

    /**
     * 从 Workbook 解析年度培训计划（不入库，仅预览）。
     * 兼容 Excel 上传和 OCR 图片识别（图片转 Workbook 后调用）。
     * sheet 名约定：
     *   - 含 "部门" → 部门级（列：序号/部门/培训内容/培训月份/培训课时/培训对象/完成情况/备注）
     *   - 含 "公司" 或 "总"（且非部门级）→ 公司级（列：序号/培训内容/培训月份/培训课时/负责授课部门/培训对象）
     */
    @Override
    public Map<String, Object> previewPlansFromWorkbook(Workbook wb) {
        List<SysDept> allDepts = sysDeptMapper.selectDeptList(new SysDept());
        Map<String, Long> deptNameToId = allDepts.stream()
                .collect(Collectors.toMap(
                        d -> d.getDeptName().trim(),
                        SysDept::getDeptId,
                        (a, b) -> a
                ));

        int companyCount = 0;
        int deptCount = 0;
        List<BizTrainPlan> allPlans = new ArrayList<>();

        int sheetNum = wb.getNumberOfSheets();
        for (int i = 0; i < sheetNum; i++) {
            Sheet sheet = wb.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            if (sheetName == null || sheet.getLastRowNum() < 1) continue;

            boolean isDeptSheet = sheetName.contains("部门");
            boolean isCompanySheet = !isDeptSheet
                    && (sheetName.contains("公司") || sheetName.contains("总"));
            if (!isDeptSheet && !isCompanySheet) continue;

            int headerRowIdx = findHeaderRow(sheet);
            if (headerRowIdx == -1) continue;

            String sheetDeptName = isDeptSheet ? findSheetDeptName(sheet, headerRowIdx) : null;
            Map<String, Integer> colMap = buildColMap(sheet.getRow(headerRowIdx));
            Integer deptColIdx = isDeptSheet ? findDeptColumnIdx(colMap) : null;

            for (int r = headerRowIdx + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String col0 = getCellStr(row.getCell(0)).trim();
                if (col0.isEmpty() || !col0.matches("\\d+")) continue;

                BizTrainPlan plan = new BizTrainPlan();
                plan.setPlanYear(2026);
                plan.setPlanType("1");
                plan.setStatus("0");

                if (isDeptSheet) {
                    plan.setTrainLevel("部门级");
                    String deptName = null;
                    if (deptColIdx != null) {
                        deptName = getCellStr(row.getCell(deptColIdx)).trim();
                    }
                    if (isBlank(deptName)) {
                        deptName = sheetDeptName;
                    }
                    if (isNotBlank(deptName)) {
                        plan.setHostDeptName(deptName);
                        plan.setHostDeptId(lookupDeptId(deptNameToId, deptName));
                    }
                    plan.setSubject(getCellStr(getCellByMap(row, colMap, "培训内容")).trim());
                    plan.setPlanMonth(parseMonth(getCellStr(getCellByMap(row, colMap, "培训月份", "月份")).trim()));
                    plan.setProjectCategory(getCellStr(getCellByMap(row, colMap, "培训课时", "课时")).trim());
                    String deptAudienceText = getCellStr(getCellByMap(row, colMap, "培训对象", "对象")).trim();
                    AudienceInfo deptAud = parseAudience(deptAudienceText, deptNameToId);
                    plan.setAudienceScope(deptAud.scope);
                    plan.setAudienceIds(deptAud.audienceIds);
                    plan.setAudienceText(deptAud.audienceText);
                    plan.setRemark(getCellStr(getCellByMap(row, colMap, "备注")).trim());
                    deptCount++;
                } else {
                    plan.setTrainLevel("公司级");
                    String hostDept = getCellStr(getCellByMap(row, colMap, "主办部门", "负责授课部门", "负责授课", "授课部门", "授课")).trim();
                    plan.setHostDeptName(isNotBlank(hostDept) ? hostDept : null);
                    plan.setSubject(getCellStr(getCellByMap(row, colMap, "培训内容")).trim());
                    plan.setPlanMonth(parseMonth(getCellStr(getCellByMap(row, colMap, "培训月份", "月份")).trim()));
                    plan.setProjectCategory(getCellStr(getCellByMap(row, colMap, "培训课时", "课时")).trim());
                    String compAudienceText = getCellStr(getCellByMap(row, colMap, "培训对象", "对象")).trim();
                    AudienceInfo compAud = parseAudience(compAudienceText, deptNameToId);
                    plan.setAudienceScope(compAud.scope);
                    plan.setAudienceIds(compAud.audienceIds);
                    plan.setAudienceText(compAud.audienceText);
                    plan.setRemark(getCellStr(getCellByMap(row, colMap, "备注")).trim());
                    companyCount++;
                }

                if (plan.getSubject() == null || plan.getSubject().trim().isEmpty()) continue;
                allPlans.add(plan);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("plans", allPlans);
        result.put("companyCount", companyCount);
        result.put("deptCount", deptCount);
        return result;
    }

    /**
     * 找表头行：包含 "序号" + ("培训内容" + "培训月份")  OR  ("部门" + "培训内容")
     */
    private int findHeaderRow(Sheet sheet) {
        int last = Math.min(10, sheet.getLastRowNum());
        for (int r = 0; r <= last; r++) {
            Row headerRow = sheet.getRow(r);
            if (headerRow == null) continue;
            String h0 = getCellStr(headerRow.getCell(0));
            String h1 = getCellStr(headerRow.getCell(1));
            String h2 = getCellStr(headerRow.getCell(2));

            if (h0.contains("序号") && h1.contains("培训内容") && h2.contains("培训月份")) {
                return r;
            }
            if (h0.contains("序号") && h1.contains("部门") && h2.contains("培训内容")) {
                return r;
            }
        }
        return -1;
    }

    /**
     * 从表头行之前的行里抽取"培训部门：xxx"形式的部门名
     */
    private String findSheetDeptName(Sheet sheet, int headerRowIdx) {
        for (int r = 0; r < headerRowIdx; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            int lastCell = row.getLastCellNum();
            for (int c = 0; c < lastCell; c++) {
                String cellVal = getCellStr(row.getCell(c));
                if (cellVal == null) continue;
                String trimmed = cellVal.trim();
                boolean isDeptLabel = trimmed.startsWith("培训部门") || trimmed.equals("部门");
                if (!isDeptLabel) continue;

                if (c + 1 < lastCell) {
                    String next = getCellStr(row.getCell(c + 1)).trim();
                    String stripped = stripColon(next);
                    if (!stripped.isEmpty()) {
                        return stripped;
                    }
                }
                String rest = trimmed;
                if (rest.startsWith("培训部门")) {
                    rest = rest.substring("培训部门".length());
                } else if (rest.startsWith("部门")) {
                    rest = rest.substring("部门".length());
                }
                String stripped = stripColon(rest);
                if (!stripped.isEmpty()) {
                    return stripped;
                }
            }
        }
        return null;
    }

    private String stripColon(String s) {
        if (s == null) return "";
        String t = s.trim();
        t = t.replaceAll("^[：:]+", "").replaceAll("[：:]+$", "").trim();
        return t;
    }

    /**
     * 找部门列（精确 "部门" > 其它含"部门"的列）
     */
    private Integer findDeptColumnIdx(Map<String, Integer> colMap) {
        if (colMap == null) return null;
        Integer idx = colMap.get("部门");
        if (idx != null) return idx;
        for (Map.Entry<String, Integer> e : colMap.entrySet()) {
            if (e.getKey().contains("部门")) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * 部门名查 deptId（容错：去掉"部"再查一次）
     */
    private Long lookupDeptId(Map<String, Long> deptNameToId, String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        Long id = deptNameToId.get(trimmed);
        if (id != null) return id;
        if (trimmed.endsWith("部")) {
            id = deptNameToId.get(trimmed.substring(0, trimmed.length() - 1));
            if (id != null) return id;
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /**
     * 根据表头行构建列名→列索引映射
     */
    private Map<String, Integer> buildColMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            String header = getCellStr(cell).trim();
            if (header.isEmpty()) continue;
            if (!map.containsKey(header)) {
                map.put(header, i);
            }
        }
        return map;
    }

    /**
     * 根据列标题别名，从列映射中查找单元格（兼容列顺序不固定）
     */
    private Cell getCellByMap(Row row, Map<String, Integer> colMap, String... keywords) {
        if (row == null || colMap == null) return null;
        for (String kw : keywords) {
            for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
                if (entry.getKey().contains(kw)) {
                    return row.getCell(entry.getValue());
                }
            }
        }
        return null;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStr(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue();
            case NUMERIC: return org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:  try { return String.valueOf((long) cell.getNumericCellValue()); } catch (Exception e) { return cell.getCellFormula(); }
            default:      return "";
        }
    }

    /**
     * 解析月份：保留原文存进 plan_month（"2026年6月"/"6月"/"5"/"不定时"/"待定"）。
     * 返回 null 表示这行没有可用的月份信息。
     */
    private String parseMonth(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.contains("不定时") || trimmed.contains("待定")) return null;
        return trimmed;
    }

    /**
     * 受众解析结果
     */
    private static class AudienceInfo {
        final String scope;
        final String audienceIds;
        final String audienceText;
        AudienceInfo(String scope, String audienceIds, String audienceText) {
            this.scope = scope;
            this.audienceIds = audienceIds;
            this.audienceText = audienceText;
        }
    }

    /**
     * 受众解析：扫到什么文本直接存什么，scope/ids 留空由 attendance 阶段处理。
     */
    private AudienceInfo parseAudience(String text, Map<String, Long> deptNameToId) {
        if (text == null) {
            return new AudienceInfo(null, null, null);
        }
        return new AudienceInfo(null, null, text.trim());
    }
}
