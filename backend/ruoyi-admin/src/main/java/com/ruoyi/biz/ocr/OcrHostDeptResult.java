package com.ruoyi.biz.ocr;

import java.util.Collections;
import java.util.List;

import com.ruoyi.common.core.domain.entity.SysDept;

/**
 * 主办部门 OCR 识别结果 DTO。
 *
 * <p>matchType 三种取值：
 * <ul>
 *   <li>exact：识别原文与 sys_dept 中某条 deptName 完全一致或互为包含关系</li>
 *   <li>fuzzy：未精确匹配，但有编辑距离 ≤ 1 的候选部门</li>
 *   <li>none：未识别到部门字段，或无任何候选</li>
 * </ul>
 */
public class OcrHostDeptResult {

    public static final String MATCH_EXACT = "exact";
    public static final String MATCH_FUZZY = "fuzzy";
    public static final String MATCH_NONE  = "none";

    /** OCR 识别出的部门原文（未匹配也返回，便于前端兜底展示） */
    private String recognizedText;

    /** 匹配类型：exact / fuzzy / none */
    private String matchType;

    /** exact 时填写 */
    private Long deptId;
    private String deptName;

    /** fuzzy 时填写 Top-N 候选（编辑距离升序） */
    private List<DeptCandidate> candidates;

    /** 仅当百度接口报错时填写（如 error_code=6 未开通服务） */
    private Integer errorCode;
    private String errorMsg;

    public String getRecognizedText() { return recognizedText; }
    public void setRecognizedText(String recognizedText) { this.recognizedText = recognizedText; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public List<DeptCandidate> getCandidates() { return candidates; }
    public void setCandidates(List<DeptCandidate> candidates) { this.candidates = candidates; }

    public Integer getErrorCode() { return errorCode; }
    public void setErrorCode(Integer errorCode) { this.errorCode = errorCode; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public static OcrHostDeptResult exact(SysDept dept, String recognizedText) {
        OcrHostDeptResult r = new OcrHostDeptResult();
        r.recognizedText = recognizedText;
        r.matchType = MATCH_EXACT;
        r.deptId = dept.getDeptId();
        r.deptName = dept.getDeptName();
        r.candidates = Collections.emptyList();
        return r;
    }

    public static OcrHostDeptResult fuzzy(String recognizedText, List<DeptCandidate> candidates) {
        OcrHostDeptResult r = new OcrHostDeptResult();
        r.recognizedText = recognizedText;
        r.matchType = MATCH_FUZZY;
        r.candidates = candidates;
        return r;
    }

    public static OcrHostDeptResult none(String recognizedText) {
        OcrHostDeptResult r = new OcrHostDeptResult();
        r.recognizedText = recognizedText;
        r.matchType = MATCH_NONE;
        r.candidates = Collections.emptyList();
        return r;
    }

    /** 百度接口业务错误（如未开通 OCR、配额用尽）。matchType 置为 none，并附带 errorCode/errorMsg。 */
    public static OcrHostDeptResult baiduError(int errorCode, String errorMsg) {
        OcrHostDeptResult r = new OcrHostDeptResult();
        r.matchType = MATCH_NONE;
        r.errorCode = errorCode;
        r.errorMsg = errorMsg;
        r.candidates = Collections.emptyList();
        return r;
    }

    /** 候选部门（仅 fuzzy 时填充） */
    public static class DeptCandidate {
        private Long deptId;
        private String deptName;
        /** 与 OCR 原文的编辑距离（Levenshtein） */
        private int score;

        public DeptCandidate() {}

        public DeptCandidate(SysDept dept, int score) {
            this.deptId = dept.getDeptId();
            this.deptName = dept.getDeptName();
            this.score = score;
        }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }

        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
    }
}