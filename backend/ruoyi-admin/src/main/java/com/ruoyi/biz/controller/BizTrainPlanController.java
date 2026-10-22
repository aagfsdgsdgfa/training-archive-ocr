package com.ruoyi.biz.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.domain.BizTrainSession;
import com.ruoyi.biz.ocr.BaiduOcrService;
import com.ruoyi.biz.ocr.HostDeptOcrService;
import com.ruoyi.biz.ocr.OcrHostDeptResult;
import com.ruoyi.biz.service.IBizTrainPlanService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.util.FileCopyUtils;

/**
 * 年度培训计划Controller
 *
 * @author admin
 * @date 2026-03-03
 */
@RestController
@RequestMapping("/biz/plan")
public class BizTrainPlanController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(BizTrainPlanController.class);

    @Autowired
    private IBizTrainPlanService bizTrainPlanService;

    @Autowired
    private BaiduOcrService baiduOcrService;

    @Autowired
    private HostDeptOcrService hostDeptOcrService;

    /**
     * 从Excel导入年度培训计划（直接入库，用于兼容旧接口）
     * 支持 培训计划2.xlsx 的两个Sheet：
     * - Sheet1：部门培训计划（序号/部门/培训内容/培训月份/培训课时/培训对象/完成情况/备注）
     * - Sheet2：公司培训总计划（序号/培训内容/培训月份/培训课时/负责授课部门/培训对象）
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:add')")
    @Log(title = "年度培训计划", businessType = BusinessType.IMPORT)
    @PostMapping("/importExcel")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要导入的Excel文件");
        }
        String fname = file.getOriginalFilename();
        String lower = fname == null ? "" : fname.toLowerCase();
        if (!(lower.endsWith(".xlsx") || lower.endsWith(".xls"))) {
            return AjaxResult.error("仅支持 .xls / .xlsx 格式的Excel文件");
        }

        try {
            Map<String, Object> result = parseExcelPlans(file);
            @SuppressWarnings("unchecked")
            List<BizTrainPlan> allPlans = (List<BizTrainPlan>) result.get("plans");
            int companyCount = (int) result.get("companyCount");
            int deptCount = (int) result.get("deptCount");

            // 批量插入
            for (BizTrainPlan plan : allPlans) {
                plan.setCreateBy(SecurityUtils.getUsername());
                plan.setCreateTime(DateUtils.getNowDate());
                plan.setDelFlag("0");
                bizTrainPlanService.insertBizTrainPlan(plan);
            }

            return AjaxResult.success("导入完成！其中公司级 " + companyCount + " 条，部门级 " + deptCount + " 条，共 " + allPlans.size() + " 条计划");
        } catch (Exception e) {
            return AjaxResult.error("Excel解析失败：" + e.getMessage());
        }
    }

    /**
     * Excel 解析预览（不入库），返回前端供人工校对
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:add')")
    @PostMapping("/previewExcel")
    public AjaxResult previewExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要导入的Excel文件");
        }
        String fname = file.getOriginalFilename();
        String lower = fname == null ? "" : fname.toLowerCase();
        if (!(lower.endsWith(".xlsx") || lower.endsWith(".xls"))) {
            return AjaxResult.error("仅支持 .xls / .xlsx 格式的Excel文件");
        }

        try {
            Map<String, Object> result = parseExcelPlans(file);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("Excel解析失败：" + e.getMessage());
        }
    }

    /**
     * 确认入库：接收校对后的计划列表，批量插入数据库
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:add')")
    @Log(title = "年度培训计划", businessType = BusinessType.IMPORT)
    @PostMapping("/confirmExcelImport")
    public AjaxResult confirmExcelImport(@RequestBody List<BizTrainPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return AjaxResult.error("没有待导入的计划数据");
        }
        try {
            int count = 0;
            for (BizTrainPlan plan : plans) {
                plan.setCreateBy(SecurityUtils.getUsername());
                plan.setCreateTime(DateUtils.getNowDate());
                plan.setDelFlag("0");
                bizTrainPlanService.insertBizTrainPlan(plan);
                count++;
            }
            return AjaxResult.success("导入成功，共 " + count + " 条计划");
        } catch (Exception e) {
            return AjaxResult.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * Excel 解析包装：把 MultipartFile 转成 Workbook 后调用 service 解析。
     * 真正的解析逻辑全部下沉到 BizTrainPlanServiceImpl.previewPlansFromWorkbook。
     */
    private Map<String, Object> parseExcelPlans(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {
            return bizTrainPlanService.previewPlansFromWorkbook(wb);
        }
    }

    /**
     * 图片扫描预览：把 OCR 识别出的多张图片合并为内存 Workbook，复用 Excel 解析逻辑。
     * 解决了图片分支误走签到表 OCR（/biz/batch/uploadBatch）的问题。
     *
     * 请求体：{ base64Images: ["data:image/jpeg;base64,..."], scanLevel: "dept" | "company" }
     * 返回：与 previewExcel 一致：{ plans, companyCount, deptCount, warnings? }，
     *       附加 trainDept: { matchType, deptId, deptName, candidates, recognizedText }，便于前端把"培训部门"自动回填到每条计划。
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:add')")
    @PostMapping("/previewPlanImage")
    public AjaxResult previewPlanImage(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> base64Images = (List<String>) body.get("base64Images");
        Object lvlObj = body.get("scanLevel");
        String scanLevel = lvlObj == null ? "dept" : String.valueOf(lvlObj);
        if (!"company".equals(scanLevel)) scanLevel = "dept";

        if (base64Images == null || base64Images.isEmpty()) {
            return AjaxResult.error("未接收到扫描图片");
        }

        // sheet 名必须包含 "部门" 或 "公司"/"总"，否则 service 里 isDeptSheet/isCompanySheet 判错
        String sheetName = "dept".equals(scanLevel) ? "部门培训计划" : "公司培训总计划";

        List<String> warnings = new ArrayList<>();
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        int rowOffset = 0;

        // 缓存第一张图的临时文件路径，循环结束后再额外跑一次"培训部门"识别（解析表外的字段）
        File firstImageTmp = null;

        for (int i = 0; i < base64Images.size(); i++) {
            String b64 = base64Images.get(i);
            if (b64 == null || b64.isEmpty()) {
                warnings.add("第 " + (i + 1) + " 页为空，已跳过");
                continue;
            }
            if (b64.contains(",")) {
                b64 = b64.substring(b64.indexOf(",") + 1);
            }
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(b64);
            } catch (Exception e) {
                warnings.add("第 " + (i + 1) + " 页 base64 解码失败: " + e.getMessage());
                continue;
            }
            File tmp;
            try {
                tmp = File.createTempFile("plan_ocr_", ".jpg");
                FileCopyUtils.copy(bytes, tmp);
            } catch (Exception e) {
                warnings.add("第 " + (i + 1) + " 页临时文件创建失败: " + e.getMessage());
                continue;
            }
            if (i == 0) {
                firstImageTmp = tmp;
            }
            try {
                org.json.JSONObject ocr = baiduOcrService.recognizeTable(tmp.getAbsolutePath());
                if (ocr == null) {
                    warnings.add("第 " + (i + 1) + " 页 OCR 接口返回空");
                    continue;
                }
                if (ocr.has("error_code")) {
                    String errCode = ocr.optString("error_code", "");
                    String errMsg = ocr.optString("error_msg", "未知");
                    warnings.add("第 " + (i + 1) + " 页 OCR 接口异常: " + errCode + " " + errMsg);
                    continue;
                }
                if (!ocr.has("result")) {
                    warnings.add("第 " + (i + 1) + " 页 OCR 响应无 result 字段");
                    continue;
                }
                int written = writeOcrCellsToSheet(sheet, ocr, i == 0, rowOffset);
                if (written <= 0) {
                    warnings.add("第 " + (i + 1) + " 页未识别到表格或文字");
                } else if (i > 0) {
                    // 后续页：减去表头那 1 行，避免重复
                    rowOffset += Math.max(written - 1, 0);
                }
            } catch (Exception e) {
                warnings.add("第 " + (i + 1) + " 页 OCR 处理异常: " + e.getMessage());
            } finally {
                if (i != 0) {
                    // 第一张图的临时文件留给"培训部门"识别用，循环结束后再删
                    try { tmp.delete(); } catch (Exception ignore) {}
                }
            }
        }

        if (sheet.getLastRowNum() < 0) {
            if (firstImageTmp != null) {
                try { firstImageTmp.delete(); } catch (Exception ignore) {}
            }
            return AjaxResult.error("所有页都识别失败：" + String.join("；", warnings));
        }

        Map<String, Object> result = bizTrainPlanService.previewPlansFromWorkbook(wb);
        if (!warnings.isEmpty()) {
            result.put("warnings", warnings);
        }

        // 额外从第一张图识别"培训部门：xxx"（该字段在表外，recognizeTable 走表格路径会丢失）
        // 无论 OCR 是否命中真实部门，都把识别到的原文作为 hostDeptName 保留：
        //   - exact  → 命中 dept：设 hostDeptId + hostDeptName
        //   - fuzzy  → 多个候选：只预填 hostDeptName（原文），用户在部门树下拉里挑一个或保留
        //   - none   → 没有候选：只预填 hostDeptName（原文），用户可手动选 dept，或这就是个人姓名直接提交
        if (firstImageTmp != null && firstImageTmp.exists()) {
            try {
                OcrHostDeptResult trainDept = hostDeptOcrService.recognize(firstImageTmp.getAbsolutePath());
                result.put("trainDept", toTrainDeptMap(trainDept));
                String recognizedText = trainDept.getRecognizedText();
                if (recognizedText != null && !recognizedText.isEmpty()) {
                    Object plansObj = result.get("plans");
                    if (plansObj instanceof java.util.List) {
                        java.util.List<?> rawPlans = (java.util.List<?>) plansObj;
                        for (Object obj : rawPlans) {
                            if (obj instanceof com.ruoyi.biz.domain.BizTrainPlan) {
                                com.ruoyi.biz.domain.BizTrainPlan p = (com.ruoyi.biz.domain.BizTrainPlan) obj;
                                p.setHostDeptName(recognizedText);
                                if (OcrHostDeptResult.MATCH_EXACT.equals(trainDept.getMatchType())
                                        && trainDept.getDeptId() != null) {
                                    p.setHostDeptId(trainDept.getDeptId());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("培训部门字段识别失败（不影响主流程）: {}", e.getMessage());
            } finally {
                try { firstImageTmp.delete(); } catch (Exception ignore) {}
            }
        }
        return AjaxResult.success(result);
    }

    /** 把 OcrHostDeptResult 转成前端可消费的 Map（含 matchType/deptId/deptName/candidates/recognizedText） */
    private Map<String, Object> toTrainDeptMap(OcrHostDeptResult r) {
        if (r == null) return null;
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("matchType", r.getMatchType());
        m.put("recognizedText", r.getRecognizedText());
        m.put("deptId", r.getDeptId());
        m.put("deptName", r.getDeptName());
        if (r.getCandidates() != null) {
            List<Map<String, Object>> cands = new ArrayList<>();
            for (OcrHostDeptResult.DeptCandidate c : r.getCandidates()) {
                Map<String, Object> cm = new java.util.LinkedHashMap<>();
                cm.put("deptId", c.getDeptId());
                cm.put("deptName", c.getDeptName());
                cm.put("score", c.getScore());
                cands.add(cm);
            }
            m.put("candidates", cands);
        } else {
            m.put("candidates", new ArrayList<>());
        }
        if (r.getErrorCode() != null) {
            m.put("errorCode", r.getErrorCode());
            m.put("errorMsg", r.getErrorMsg());
        }
        return m;
    }

    /**
     * 把百度 OCR 单页识别结果写入 Sheet。
     * 第一张图：includeHeader=true，写入表头。
     * 后续图：includeHeader=false，跳过表头，row 偏移由 rowOffset 控制。
     * @return 实际写入的最大 row 数（含表头），0 表示本页无有效单元格
     */
    @SuppressWarnings("unchecked")
    private int writeOcrCellsToSheet(Sheet sheet, org.json.JSONObject ocr, boolean includeHeader, int rowOffset) {
        org.json.JSONObject res;
        org.json.JSONArray tables;
        try {
            res = ocr.getJSONObject("result");
            tables = res.optJSONArray("tables_result");
        } catch (Exception e) {
            return 0;
        }
        if (tables == null || tables.length() == 0) return 0;

        int maxRow = -1;
        for (int t = 0; t < tables.length(); t++) {
            org.json.JSONObject table = tables.optJSONObject(t);
            if (table == null) continue;
            org.json.JSONArray body = table.optJSONArray("body");
            if (body == null || body.length() == 0) continue;

            // 找表头行：包含 "序号" 或 "培训内容" 或 "培训月份" 任意关键字的行
            int headerIdx = -1;
            for (int i = 0; i < body.length(); i++) {
                org.json.JSONObject c = body.optJSONObject(i);
                if (c == null) continue;
                String words = c.optString("words", "");
                if (words.contains("序号") || words.contains("培训内容") || words.contains("培训月份") || words.contains("部门")) {
                    int rs = c.optInt("row_start", 0);
                    if (headerIdx == -1 || rs < headerIdx) headerIdx = rs;
                    break;
                }
            }

            for (int i = 0; i < body.length(); i++) {
                org.json.JSONObject c = body.optJSONObject(i);
                if (c == null) continue;
                int rs = c.optInt("row_start", 0);
                int cs = c.optInt("col_start", 0);
                String words = c.optString("words", "");
                if (words == null || words.trim().isEmpty()) continue;

                // 跳过表头（仅当 includeHeader=false 即非首页时）
                if (!includeHeader && headerIdx >= 0 && rs == headerIdx) continue;

                int targetRow = rs + rowOffset;
                if (!includeHeader && headerIdx >= 0 && rs > headerIdx) {
                    // 跳过表头那一行：原本表头占 1 行，所以数据行要 -1
                    targetRow = rs + rowOffset - 1;
                }

                Row row = sheet.getRow(targetRow);
                if (row == null) row = sheet.createRow(targetRow);
                Cell cell = row.getCell(cs);
                if (cell == null) cell = row.createCell(cs);
                cell.setCellValue(words.trim());

                if (targetRow > maxRow) maxRow = targetRow;
            }
        }
        return maxRow + 1;
    }

    /**
     * 查询年度培训计划列表
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:list')")
    @DataScope(deptAlias = "p", userAlias = "p")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainPlan bizTrainPlan) {
        startPage();
        List<BizTrainPlan> list = bizTrainPlanService.selectBizTrainPlanList(bizTrainPlan);
        return getDataTable(list);
    }

    /**
     * 导出年度培训计划列表
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:export')")
    @Log(title = "年度培训计划", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainPlan bizTrainPlan) {
        List<BizTrainPlan> list = bizTrainPlanService.selectBizTrainPlanList(bizTrainPlan);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无计划数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainPlan> util = new ExcelUtil<BizTrainPlan>(BizTrainPlan.class);
        util.exportExcel(response, list, "年度培训计划数据");
    }

    /**
     * 导出年度培训计划导入模板（支持任意年份）
     * @param year 年份，如 2026
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:export')")
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletResponse response,
                               @RequestParam(required = false) String year) {
        if (year == null || year.trim().isEmpty()) {
            year = String.valueOf(java.time.Year.now().getValue());
        }
        String finalYear = year;

        try (InputStream templateIs = new ClassPathResource("templates/plan_template.xlsx").getInputStream();
             Workbook workbook = new XSSFWorkbook(templateIs);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // 遍历所有 sheet
            for (int si = 0; si < workbook.getNumberOfSheets(); si++) {
                Sheet sheet = workbook.getSheetAt(si);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.STRING) {
                            String val = cell.getStringCellValue();
                            if (val != null && val.contains("{{YEAR}}")) {
                                cell.setCellValue(val.replace("{{YEAR}}", finalYear));
                            }
                        } else if (cell.getCellType() == CellType.FORMULA) {
                            // 公式单元格：先求值再替换
                            CellValue cv = evaluator.evaluate(cell);
                            if (cv.getCellType() == CellType.STRING) {
                                String val = cv.getStringValue();
                                if (val != null && val.contains("{{YEAR}}")) {
                                    cell.setCellValue(val.replace("{{YEAR}}", finalYear));
                                }
                            }
                        }
                    }
                }
            }

            workbook.write(out);
            byte[] data = out.toByteArray();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(("年度培训计划_" + finalYear + "年.xlsx").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"");
            response.setHeader("Content-Length", String.valueOf(data.length));
            OutputStream os = response.getOutputStream();
            os.write(data);
            os.flush();
        } catch (Exception e) {
            throw new RuntimeException("导出模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出部门级培训计划（POI 直接生成，不用模板填充）
     * 模板：templates/dept_plan_template.xlsx
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:export')")
    @PostMapping("/exportDeptPlan")
    public void exportDeptPlan(HttpServletResponse response,
                               @RequestParam(required = false) Integer year) {
        if (year == null) year = Year.now().getValue();

        try {
            // 1. 查询部门级数据（8列：序号,部门,培训内容,培训月份,培训课时,培训对象,完成情况,备注）
            BizTrainPlan query = new BizTrainPlan();
            query.setPlanYear(year);
            List<BizTrainPlan> rawList = bizTrainPlanService.selectBizTrainPlanList(query);
            List<PlanExportVo> dataList = new ArrayList<>();
            int idx = 1;
            for (BizTrainPlan p : rawList) {
                if (!"部门级".equals(p.getTrainLevel())) continue;
                PlanExportVo vo = new PlanExportVo();
                vo.setIndex(idx++);
                vo.setDeptName(p.getHostDeptName());
                vo.setTrainContent(p.getSubject());
                vo.setTrainMonth(p.getPlanMonth() != null ? p.getPlanMonth() : "");
                vo.setTrainHours(p.getProjectCategory());
                vo.setTrainTarget(resolveTrainTarget(p));
                vo.setFinishStatus(statusText(p.getStatus()));
                vo.setRemark(p.getRemark());
                dataList.add(vo);
            }

            // 2. 如果没有数据，返回错误信息而非空文件
            if (dataList.isEmpty()) {
                response.setContentType("application/json;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                PrintWriter writer = response.getWriter();
                writer.write("{\"code\":500,\"msg\":\"该年份（" + year + "年）暂无部门级培训计划数据，无法导出\"}");
                writer.flush();
                return;
            }

            // 3. 复制模板到临时文件
            ClassPathResource templateResource = new ClassPathResource("templates/dept_plan_template.xlsx");
            File tempFile = File.createTempFile("dept_plan_", ".xlsx");
            Files.copy(templateResource.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 3. 用 POI 打开，替换 {year} 占位符，再写入数据
            try (FileInputStream fis = new FileInputStream(tempFile);
                 Workbook wb = new XSSFWorkbook(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                // 替换 sheet 名称中的 {year}
                String sheetName = sheet.getSheetName().replace("{year}", String.valueOf(year));
                wb.setSheetName(0, sheetName);
                // 替换所有单元格文本中的 {year}
                replaceYearPlaceholder(sheet, year);
                // 取模板数据行（第5行）的样式
                Row tplRow = sheet.getRow(5);
                CellStyle[] styles = new CellStyle[8];
                for (int i = 0; i < 8; i++) styles[i] = tplRow.getCell(i).getCellStyle();
                sheet.shiftRows(6, sheet.getLastRowNum(), dataList.size()); // 腾出空间

                // 写入每条数据
                for (int ri = 0; ri < dataList.size(); ri++) {
                    PlanExportVo vo = dataList.get(ri);
                    int rowIdx = 5 + ri;
                    Row row = sheet.createRow(rowIdx);
                    row.setHeight((short) 400);
                    setCell(row, 0, vo.getIndex(), styles[0]);
                    setCell(row, 1, vo.getDeptName(), styles[1]);
                    setCell(row, 2, vo.getTrainContent(), styles[2]);
                    setCell(row, 3, vo.getTrainMonth(), styles[3]);
                    setCell(row, 4, vo.getTrainHours(), styles[4]);
                    setCell(row, 5, vo.getTrainTarget(), styles[5]);
                    setCell(row, 6, vo.getFinishStatus(), styles[6]);
                    setCell(row, 7, vo.getRemark(), styles[7]);
                }

                // 4. 写出到字节数组再写入 response
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                wb.write(bos);
                byte[] bytes = bos.toByteArray();

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                String fileName = URLEncoder.encode("年度部门培训计划_" + year + "年", "UTF-8").replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
                response.setHeader("Content-Length", String.valueOf(bytes.length));
                response.getOutputStream().write(bytes);
                response.getOutputStream().flush();
            } finally {
                Files.deleteIfExists(tempFile.toPath());
            }
        } catch (Exception e) {
            throw new RuntimeException("导出部门级计划失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出公司级培训计划（POI 直接生成，不用模板填充）
     * 模板：templates/company_plan_template.xlsx
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:export')")
    @PostMapping("/exportCompanyPlan")
    public void exportCompanyPlan(HttpServletResponse response,
                                 @RequestParam(required = false) Integer year) {
        if (year == null) year = Year.now().getValue();

        try {
            // 1. 查询公司级数据（6列：序号,培训内容,培训月份,培训课时,负责授课部门/人员,培训对象）
            BizTrainPlan query = new BizTrainPlan();
            query.setPlanYear(year);
            List<BizTrainPlan> rawList = bizTrainPlanService.selectBizTrainPlanList(query);
            List<PlanExportVo> dataList = new ArrayList<>();
            int idx = 1;
            for (BizTrainPlan p : rawList) {
                if (!"公司级".equals(p.getTrainLevel())) continue;
                PlanExportVo vo = new PlanExportVo();
                vo.setIndex(idx++);
                vo.setTrainContent(p.getSubject());
                vo.setTrainMonth(p.getPlanMonth() != null ? p.getPlanMonth() : "");
                vo.setTrainHours(p.getProjectCategory());
                vo.setInstructor(p.getHostDeptName());
                vo.setTrainTarget(resolveTrainTarget(p));
                dataList.add(vo);
            }

            // 2. 如果没有数据，返回错误信息而非空文件
            if (dataList.isEmpty()) {
                response.setContentType("application/json;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                PrintWriter writer = response.getWriter();
                writer.write("{\"code\":500,\"msg\":\"该年份（" + year + "年）暂无公司级培训计划数据，无法导出\"}");
                writer.flush();
                return;
            }

            // 3. 复制模板到临时文件
            ClassPathResource templateResource = new ClassPathResource("templates/company_plan_template.xlsx");
            File tempFile = File.createTempFile("company_plan_", ".xlsx");
            Files.copy(templateResource.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 3. 用 POI 打开，替换 {year} 占位符，再写入数据
            try (FileInputStream fis = new FileInputStream(tempFile);
                 Workbook wb = new XSSFWorkbook(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                // 替换 sheet 名称中的 {year}
                String sheetName = sheet.getSheetName().replace("{year}", String.valueOf(year));
                wb.setSheetName(0, sheetName);
                // 替换所有单元格文本中的 {year}
                replaceYearPlaceholder(sheet, year);
                // 取模板数据行样式
                Row tplRow = sheet.getRow(5);
                CellStyle[] styles = new CellStyle[6];
                for (int i = 0; i < 6; i++) styles[i] = tplRow.getCell(i).getCellStyle();
                sheet.shiftRows(6, sheet.getLastRowNum(), dataList.size());

                for (int ri = 0; ri < dataList.size(); ri++) {
                    PlanExportVo vo = dataList.get(ri);
                    int rowIdx = 5 + ri;
                    Row row = sheet.createRow(rowIdx);
                    row.setHeight((short) 400);
                    setCell(row, 0, vo.getIndex(), styles[0]);
                    setCell(row, 1, vo.getTrainContent(), styles[1]);
                    setCell(row, 2, vo.getTrainMonth(), styles[2]);
                    setCell(row, 3, vo.getTrainHours(), styles[3]);
                    setCell(row, 4, vo.getInstructor(), styles[4]);
                    setCell(row, 5, vo.getTrainTarget(), styles[5]);
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                wb.write(bos);
                byte[] bytes = bos.toByteArray();

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                String fileName = URLEncoder.encode("年度公司级培训计划_" + year + "年", "UTF-8").replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
                response.setHeader("Content-Length", String.valueOf(bytes.length));
                response.getOutputStream().write(bytes);
                response.getOutputStream().flush();
            } finally {
                Files.deleteIfExists(tempFile.toPath());
            }
        } catch (Exception e) {
            throw new RuntimeException("导出公司级计划失败: " + e.getMessage(), e);
        }
    }

    /** 工具方法：向 Row 的指定列写入值（数字或字符串） */
    private void setCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(style);
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    /**
     * 遍历 sheet 所有单元格，将文本中的 {year} 占位符替换为实际年份
     */
    private void replaceYearPlaceholder(Sheet sheet, Integer year) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String val = cell.getStringCellValue();
                    if (val != null && val.contains("{year}")) {
                        cell.setCellValue(val.replace("{year}", String.valueOf(year)));
                    }
                }
            }
        }
    }

    /** 计划状态转文本 */
    private String statusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "0": return "未完成";
            case "1": return "已完成";
            case "2": return "已取消";
            default: return status;
        }
    }

    /** 受众范围数字转文本 */
    private String audienceScopeText(String scope) {
        if (scope == null) return "";
        switch (scope) {
            case "0": return "全体员工";
            case "1": return "部门全员";
            case "2": return "指定人员";
            default: return scope;
        }
    }

    /**
     * 解析培训对象:优先返回 Excel 原始的"培训对象"文本(例如"研发部+QA组全员"),
     * 为空时回落到 audienceScope 通用 bucket 文案(全体员工/部门全员/指定人员)。
     */
    private String resolveTrainTarget(BizTrainPlan p) {
        String text = p.getAudienceText();
        if (text != null && !text.trim().isEmpty()) {
            return text.trim();
        }
        return audienceScopeText(p.getAudienceScope());
    }

    /**
     * 导出 VO：Sheet1(部门级) / Sheet2(公司级) 共用字段。
     * 字段名与模板占位符一一对应：{index}, {deptName}, {trainContent}, {trainMonth},
     * {trainHours}, {trainTarget}, {finishStatus}, {remark}, {instructor}
     */
    public static class PlanExportVo {
        private Integer index;
        /** 部门名称（对应 {deptName}） */
        private String deptName;
        /** 培训内容（对应 {trainContent}） */
        private String trainContent;
        /** 培训月份（对应 {trainMonth}） */
        private String trainMonth;
        /** 培训课时（对应 {trainHours}） */
        private String trainHours;
        /** 培训对象（对应 {trainTarget}） */
        private String trainTarget;
        /** 完成情况（对应 {finishStatus}） */
        private String finishStatus;
        /** 备注（对应 {remark}） */
        private String remark;
        /** 负责授课部门/人员（对应 {instructor}，仅公司级使用） */
        private String instructor;

        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        public String getTrainContent() { return trainContent; }
        public void setTrainContent(String trainContent) { this.trainContent = trainContent; }
        public String getTrainMonth() { return trainMonth; }
        public void setTrainMonth(String trainMonth) { this.trainMonth = trainMonth; }
        public String getTrainHours() { return trainHours; }
        public void setTrainHours(String trainHours) { this.trainHours = trainHours; }
        public String getTrainTarget() { return trainTarget; }
        public void setTrainTarget(String trainTarget) { this.trainTarget = trainTarget; }
        public String getFinishStatus() { return finishStatus; }
        public void setFinishStatus(String finishStatus) { this.finishStatus = finishStatus; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getInstructor() { return instructor; }
        public void setInstructor(String instructor) { this.instructor = instructor; }
    }

    /**
     * 获取年度培训计划详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:query')")
    @GetMapping(value = "/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId) {
        return success(bizTrainPlanService.selectBizTrainPlanByPlanId(planId));
    }

    /**
     * 新增年度培训计划
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:add')")
    @Log(title = "年度培训计划", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainPlan bizTrainPlan) {
        return toAjax(bizTrainPlanService.insertBizTrainPlan(bizTrainPlan));
    }

    /**
     * 修改年度培训计划
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:edit')")
    @Log(title = "年度培训计划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainPlan bizTrainPlan) {
        return toAjax(bizTrainPlanService.updateBizTrainPlan(bizTrainPlan));
    }

    /**
     * 按条件批量删除所有匹配数据（跨页全选删除）
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:remove')")
    @Log(title = "年度培训计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainPlan query) {
        List<BizTrainPlan> list = bizTrainPlanService.selectBizTrainPlanList(query);
        if (list.isEmpty()) {
            return error("没有符合条件的数据");
        }
        Long[] ids = list.stream().map(BizTrainPlan::getPlanId).toArray(Long[]::new);
        return toAjax(bizTrainPlanService.deleteBizTrainPlanByPlanIds(ids));
    }

    /**
     * 删除年度培训计划
     */
    @PreAuthorize("@ss.hasPermi('biz:plan:remove')")
    @Log(title = "年度培训计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds) {
        return toAjax(bizTrainPlanService.deleteBizTrainPlanByPlanIds(planIds));
    }
}
