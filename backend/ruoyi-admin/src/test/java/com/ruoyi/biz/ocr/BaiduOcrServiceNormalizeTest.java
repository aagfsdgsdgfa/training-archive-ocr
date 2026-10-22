package com.ruoyi.biz.ocr;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 单元测试：normalizeResponse / hasTableResult / hasWordsResult
 * 关键：覆盖真实百度 API 的两种响应格式，避免再次出现"Controller 拿到响应但用错格式"的 Bug
 */
class BaiduOcrServiceNormalizeTest {

    private BaiduOcrService newService() {
        BaiduOcrService s = new BaiduOcrService();
        try {
            java.lang.reflect.Field f = BaiduOcrService.class.getDeclaredField("apiKey");
            f.setAccessible(true);
            f.set(s, "test-ak");
            f = BaiduOcrService.class.getDeclaredField("secretKey");
            f.setAccessible(true);
            f.set(s, "test-sk");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    // ---- normalizeResponse ----

    @Test
    @DisplayName("normalize: 顶层 tables_result 格式（v1/table_vinner）→ 包到 result")
    void normalize_topLevelTablesResult() {
        BaiduOcrService s = newService();
        String raw = "{\"log_id\":12345,\"table_num\":1,\"tables_result\":[" +
                "{\"body\":[{\"words\":\"序号\",\"row_start\":0,\"col_start\":0}]}]}";
        JSONObject out = s.normalizeResponse(new JSONObject(raw));
        assertThat(out.has("result")).isTrue();
        assertThat(out.getJSONObject("result").getJSONArray("tables_result").length()).isEqualTo(1);
        assertThat(out.getJSONObject("result").optInt("table_num")).isEqualTo(1);
        assertThat(out.getJSONObject("result").optLong("log_id")).isEqualTo(12345L);
    }

    @Test
    @DisplayName("normalize: 顶层 forms_result 格式 → 包到 result")
    void normalize_topLevelFormsResult() {
        BaiduOcrService s = newService();
        String raw = "{\"log_id\":99,\"forms_result\":[{\"body\":[]}]}";
        JSONObject out = s.normalizeResponse(new JSONObject(raw));
        assertThat(out.getJSONObject("result").getJSONArray("forms_result").length()).isEqualTo(1);
    }

    @Test
    @DisplayName("normalize: 顶层 words_result 格式（v1/general）→ 包到 result")
    void normalize_topLevelWordsResult() {
        BaiduOcrService s = newService();
        String raw = "{\"log_id\":1,\"words_result\":[{\"words\":\"hello\",\"location\":{\"top\":0,\"left\":0,\"width\":10,\"height\":10}}]}";
        JSONObject out = s.normalizeResponse(new JSONObject(raw));
        assertThat(out.getJSONObject("result").getJSONArray("words_result").length()).isEqualTo(1);
    }

    @Test
    @DisplayName("normalize: 已嵌套 result 字段 → 原样返回")
    void normalize_alreadyNested() {
        BaiduOcrService s = newService();
        String raw = "{\"result\":{\"tables_result\":[{\"body\":[]}]}}";
        JSONObject out = s.normalizeResponse(new JSONObject(raw));
        assertThat(out.toString()).isEqualTo(raw);
    }

    @Test
    @DisplayName("normalize: 错误响应（error_code）→ 原样返回，不包 result")
    void normalize_errorResponse() {
        BaiduOcrService s = newService();
        String raw = "{\"error_code\":110,\"error_msg\":\"Access token invalid\"}";
        JSONObject out = s.normalizeResponse(new JSONObject(raw));
        assertThat(out.has("error_code")).isTrue();
        assertThat(out.has("result")).isFalse();
    }

    @Test
    @DisplayName("normalize: null 输入 → null")
    void normalize_null() {
        BaiduOcrService s = newService();
        assertThat(s.normalizeResponse(null)).isNull();
    }

    // ---- hasTableResult ----

    @Test
    @DisplayName("hasTableResult: 顶层 tables_result 非空 → true")
    void hasTable_topLevel() {
        BaiduOcrService s = newService();
        String raw = "{\"tables_result\":[{\"body\":[{\"words\":\"x\"}]}]}";
        assertThat(s.hasTableResult(new JSONObject(raw))).isTrue();
    }

    @Test
    @DisplayName("hasTableResult: 顶层 tables_result 空数组 → false")
    void hasTable_topLevelEmpty() {
        BaiduOcrService s = newService();
        String raw = "{\"tables_result\":[]}";
        assertThat(s.hasTableResult(new JSONObject(raw))).isFalse();
    }

    @Test
    @DisplayName("hasTableResult: 嵌套 result.tables_result 非空 → true")
    void hasTable_nested() {
        BaiduOcrService s = newService();
        String raw = "{\"result\":{\"tables_result\":[{\"body\":[{\"words\":\"x\"}]}]}}";
        assertThat(s.hasTableResult(new JSONObject(raw))).isTrue();
    }

    @Test
    @DisplayName("hasTableResult: 错误响应 → false")
    void hasTable_errorResponse() {
        BaiduOcrService s = newService();
        String raw = "{\"error_code\":110,\"error_msg\":\"x\"}";
        assertThat(s.hasTableResult(new JSONObject(raw))).isFalse();
    }

    @Test
    @DisplayName("hasTableResult: null → false")
    void hasTable_null() {
        BaiduOcrService s = newService();
        assertThat(s.hasTableResult(null)).isFalse();
    }

    // ---- hasWordsResult ----

    @Test
    @DisplayName("hasWordsResult: 顶层 words_result 非空 → true")
    void hasWords_topLevel() {
        BaiduOcrService s = newService();
        String raw = "{\"words_result\":[{\"words\":\"a\"}]}";
        assertThat(s.hasWordsResult(new JSONObject(raw))).isTrue();
    }

    @Test
    @DisplayName("hasWordsResult: 顶层 words_result 空数组 → false")
    void hasWords_topLevelEmpty() {
        BaiduOcrService s = newService();
        String raw = "{\"words_result\":[]}";
        assertThat(s.hasWordsResult(new JSONObject(raw))).isFalse();
    }
}