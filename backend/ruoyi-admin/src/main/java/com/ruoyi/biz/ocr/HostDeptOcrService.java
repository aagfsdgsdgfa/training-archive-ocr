package com.ruoyi.biz.ocr;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.biz.ocr.OcrHostDeptResult.DeptCandidate;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;

/**
 * 主办部门 OCR 识别服务。
 *
 * <p>设计要点：
 * <ul>
 *   <li>复用 BaiduOcrService.recognizeTable()（4 步降级，最终走 v1/general 兜底拿 words_result）</li>
 *   <li>不依赖几何位置（图片"培训部门"在左上角还是右下角都鲁棒）</li>
 *   <li>三层匹配：精确 → 包含 → Levenshtein ≤ 1 模糊</li>
 *   <li>不持久化图片，调用方负责传入临时文件路径并自清理</li>
 * </ul>
 */
@Service
public class HostDeptOcrService {

    private static final Logger log = LoggerFactory.getLogger(HostDeptOcrService.class);

    /** 主匹配：培训部门：xxx / 培训部门:xxx（允许可选空白，长 2-20，含常见标点） */
    private static final Pattern P_TRAIN_DEPT = Pattern.compile(
        "培训部门[：:：]?\\s*([\\u4e00-\\u9fa5A-Za-z0-9（）()·、，．\\-]{2,20})");

    /** 兜底匹配：部门：xxx（要求前面不是"培训"，避免双匹配） */
    private static final Pattern P_DEPT_FALLBACK = Pattern.compile(
        "(?<!培训)部门[：:：]?\\s*([\\u4e00-\\u9fa5A-Za-z0-9（）()·、，．\\-]{2,20})");

    @Autowired
    private BaiduOcrService baiduOcrService;

    @Autowired
    private SysDeptMapper deptMapper;

    public OcrHostDeptResult recognize(String imagePath) {
        log.info("HostDeptOcrService 开始识别: {}", imagePath);
        File img = new File(imagePath);
        if (img.exists()) {
            log.info("OCR 入参图片: {} 大小={} bytes", imagePath, img.length());
        } else {
            log.warn("OCR 入参图片不存在: {}", imagePath);
        }

        // 主路径：直接走通用 OCR（拿全图所有文字，含表格外的"培训部门：xxx"）
        // 不走 recognizeTable() 是因为它会在 table_vinner 成功后提前返回，
        // 只返回表格内容，遗漏纸张左上角游离的字段。
        JSONObject ocr;
        try {
            ocr = baiduOcrService.recognizeGeneralFromFile(imagePath);
        } catch (Exception e) {
            log.error("百度OCR调用失败: {}", e.getMessage());
            return OcrHostDeptResult.none("OCR调用失败: " + e.getMessage());
        }

        // 百度接口业务错误（如未开通 OCR 服务/配额用尽），不再静默，包裹成 DTO 返回
        if (ocr.has("error_code") && ocr.optInt("error_code") != 0) {
            int errCode = ocr.optInt("error_code");
            String errMsg = ocr.optString("error_msg", "百度接口错误");
            log.error("OCR 百度接口错误: code={} msg={}", errCode, errMsg);
            return OcrHostDeptResult.baiduError(errCode, errMsg);
        }

        String rawText = extractAllText(ocr);
        log.info("OCR 抽取文本（去空白后，长度={}）: {}", rawText.length(), rawText);

        String deptText = matchFirst(P_TRAIN_DEPT, rawText);
        if (deptText == null) {
            deptText = matchFirst(P_DEPT_FALLBACK, rawText);
        }
        if (deptText == null) {
            log.info("未在 OCR 文本中匹配到 '培训部门：' / '部门：' 字段（正则字符类：中文+全/半角字母数字+常见标点）");
            return OcrHostDeptResult.none(rawText.isEmpty() ? null : rawText);
        }

        log.info("正则识别到部门原文: '{}'", deptText);
        return matchAgainstDeptTree(deptText);
    }

    /** 从 OCR 返回的 JSON 中抽出所有 words_result[*].words + tables_result[*].body[*].words 拼成一行字符串 */
    private String extractAllText(JSONObject ocr) {
        if (ocr == null) return "";
        StringBuilder sb = new StringBuilder();
        try {
            JSONObject result = ocr.has("result") ? ocr.getJSONObject("result") : ocr;
            // 优先 words_result（v1/general 路径）
            JSONArray wordsArr = result.optJSONArray("words_result");
            if (wordsArr != null) {
                for (int i = 0; i < wordsArr.length(); i++) {
                    JSONObject w = wordsArr.optJSONObject(i);
                    if (w != null) sb.append(w.optString("words", "")).append(' ');
                }
            }
            // 兜底：从 tables_result[*].body[*].words 抽取（v1/table 路径）
            JSONArray tables = result.optJSONArray("tables_result");
            if (tables != null) {
                for (int i = 0; i < tables.length(); i++) {
                    JSONObject t = tables.optJSONObject(i);
                    if (t == null) continue;
                    JSONArray body = t.optJSONArray("body");
                    if (body == null) continue;
                    for (int j = 0; j < body.length(); j++) {
                        JSONObject cell = body.optJSONObject(j);
                        if (cell != null) sb.append(cell.optString("words", "")).append(' ');
                    }
                }
            }
            JSONArray forms = result.optJSONArray("forms_result");
            if (forms != null) {
                for (int i = 0; i < forms.length(); i++) {
                    JSONObject f = forms.optJSONObject(i);
                    if (f == null) continue;
                    sb.append(f.optString("text", "")).append(' ');
                }
            }
        } catch (Exception e) {
            log.warn("OCR 文本拼接异常: {}", e.getMessage());
        }
        // 去空白便于正则匹配
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private String matchFirst(Pattern p, String text) {
        if (text == null || text.isEmpty()) return null;
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    /** 在 sys_dept 表中三层匹配：精确 → 包含 → 编辑距离 ≤ 1 */
    private OcrHostDeptResult matchAgainstDeptTree(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            return OcrHostDeptResult.none(null);
        }

        SysDept query = new SysDept();
        List<SysDept> all;
        try {
            all = deptMapper.selectDeptList(query);
        } catch (Exception e) {
            log.error("查询 sys_dept 失败: {}", e.getMessage());
            return OcrHostDeptResult.none(trimmed);
        }
        if (all == null || all.isEmpty()) {
            log.warn("sys_dept 表为空，无法做匹配");
            return OcrHostDeptResult.none(trimmed);
        }

        // 1) 精确匹配（trim 后相等）
        Optional<SysDept> exact = all.stream()
            .filter(d -> d.getDeptName() != null && trimmed.equals(d.getDeptName().trim()))
            .findFirst();
        if (exact.isPresent()) {
            log.info("精确匹配命中: {} -> deptId={}", exact.get().getDeptName(), exact.get().getDeptId());
            return OcrHostDeptResult.exact(exact.get(), trimmed);
        }

        // 2) 包含匹配（OCR 可能带"财务部门"，实际部门是"财务部"）
        Optional<SysDept> contains = all.stream()
            .filter(d -> d.getDeptName() != null)
            .filter(d -> d.getDeptName().contains(trimmed) || trimmed.contains(d.getDeptName()))
            .findFirst();
        if (contains.isPresent()) {
            log.info("包含匹配命中: OCR='{}' 实际部门='{}'", trimmed, contains.get().getDeptName());
            return OcrHostDeptResult.exact(contains.get(), trimmed);
        }

        // 3) 模糊匹配：编辑距离 ≤ 1
        List<DeptCandidate> fuzzy = all.stream()
            .filter(d -> d.getDeptName() != null && !d.getDeptName().trim().isEmpty())
            .map(d -> new DeptCandidate(d, levenshtein(trimmed, d.getDeptName().trim())))
            .filter(c -> c.getScore() <= 1)
            .sorted(Comparator.comparingInt(DeptCandidate::getScore))
            .limit(3)
            .collect(Collectors.toList());
        if (!fuzzy.isEmpty()) {
            log.info("模糊匹配命中 {} 条候选: {}", fuzzy.size(),
                fuzzy.stream().map(DeptCandidate::getDeptName).collect(Collectors.joining(", ")));
            return OcrHostDeptResult.fuzzy(trimmed, fuzzy);
        }

        log.info("三层匹配均未命中，原文='{}'", trimmed);
        return OcrHostDeptResult.none(trimmed);
    }

    /** Levenshtein 编辑距离（短字符串场景够用，未做空间优化） */
    private int levenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[m];
    }
}