package com.ruoyi.biz.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OcrDateNormalizerTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "　"})
    @DisplayName("空值 / 纯空白 → Optional.empty()")
    void empty(String input) {
        assertThat(OcrDateNormalizer.normalize(input)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "2023-01-15,        2023-01-15",
        "2023-01-15 14:00,  2023-01-15",
        "2023-01-15 14:00:30, 2023-01-15"
    })
    @DisplayName("ISO 日期格式 → 原样返回日期部分")
    void iso(String input, String expected) {
        assertThat(OcrDateNormalizer.normalize(input)).contains(expected);
    }

    @Test
    @DisplayName("Excel 序列号 46153(Excel 1900 系统)= 2026-05-11")
    void excelSerial() {
        Optional<String> result = OcrDateNormalizer.normalize("46153");
        assertThat(result).contains("2026-05-11");
    }

    @Test
    @DisplayName("Excel 序列号越界(>100000) → empty")
    void excelSerialOutOfRange() {
        assertThat(OcrDateNormalizer.normalize("999999")).isEmpty();
    }

    @Test
    @DisplayName("Excel 序列号小整数(<10000,可能是学时) → empty")
    void excelSerialTooSmall() {
        assertThat(OcrDateNormalizer.normalize("36")).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "2023.01.15,  2023-01-15",
        "2023.1.5,    2023-01-05",
        "2023/01/15,  2023-01-15",
        "2023/1/5,    2023-01-05",
        "2023.12.31,  2023-12-31"
    })
    @DisplayName("点/斜杠分隔 → yyyy-MM-dd")
    void dotSlash(String input, String expected) {
        assertThat(OcrDateNormalizer.normalize(input)).contains(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "2023年1月15日,         2023-01-15",
        "2023年01月15日,        2023-01-15",
        "2023年3月5日14:00-16:00, 2023-03-05",
        "2026年3月5日14：00-16：00, 2026-03-05",
        "2023年3月5日 14:00,    2023-03-05"
    })
    @DisplayName("中文日期(含时间后缀) → yyyy-MM-dd")
    void chinese(String input, String expected) {
        assertThat(OcrDateNormalizer.normalize(input)).contains(expected);
    }

    @Test
    @DisplayName("全角字符自动转半角")
    void fullWidth() {
        assertThat(OcrDateNormalizer.normalize("２０２３．０１．１５")).contains("2023-01-15");
        assertThat(OcrDateNormalizer.normalize("２０２３／０１／１５")).contains("2023-01-15");
        assertThat(OcrDateNormalizer.normalize("２０２３年０１月１５日")).contains("2023-01-15");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "??乱码??",
        "hello world",
        "abc.def.ghi",
        "2023",
        "2023.13.45",
        "2023.02.30",
        "2023.00.15",
        "2023.15.01",
        "12345abc"
    })
    @DisplayName("无法解析或月日越界 → Optional.empty()")
    void unparseable(String input) {
        assertThat(OcrDateNormalizer.normalize(input)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "2026年01月22-24日,    2026-01-22, 2026-01-24",   // 用户要求格式1:中间无"日"
        "2026年01月22日-24日,  2026-01-22, 2026-01-24",   // 用户要求格式2:中间有"日"
        "2026年1月5日-8日,     2026-01-05, 2026-01-08",   // 月/日不补零
        "2026年03月05-05日,    2026-03-05, 2026-03-05"    // 边界:起止同日(等价单日)
    })
    @DisplayName("中文日期区间 → DateRange(start, end)")
    void cnRange(String input, String expectedStart, String expectedEnd) {
        OcrDateNormalizer.DateRange r = OcrDateNormalizer.normalizeRange(input).orElseThrow();
        assertThat(r.getStart()).isEqualTo(expectedStart);
        assertThat(r.getEnd()).isEqualTo(expectedEnd);
        assertThat(r.format()).isEqualTo(expectedStart + " — " + expectedEnd);
        assertThat(r.isRange()).isTrue();
    }

    @Test
    @DisplayName("单日期经 normalizeRange 返回 end=null")
    void singleDateEndIsNull() {
        OcrDateNormalizer.DateRange r = OcrDateNormalizer.normalizeRange("2026年01月22日").orElseThrow();
        assertThat(r.getStart()).isEqualTo("2026-01-22");
        assertThat(r.getEnd()).isNull();
        assertThat(r.format()).isEqualTo("2026-01-22");
        assertThat(r.isRange()).isFalse();
    }

    @Test
    @DisplayName("向后兼容:旧 normalize() 对区间输入仍只返回起始日")
    void backwardCompat() {
        // 区间输入时,旧 API 只返回 start,调用方不会感知 end
        assertThat(OcrDateNormalizer.normalize("2026年01月22-24日")).contains("2026-01-22");
        assertThat(OcrDateNormalizer.normalize("2026年01月22日-24日")).contains("2026-01-22");
        // 单日期行为完全不变
        assertThat(OcrDateNormalizer.normalize("2026年01月22日")).contains("2026-01-22");
    }
}