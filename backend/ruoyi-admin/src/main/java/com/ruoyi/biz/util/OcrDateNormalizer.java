package com.ruoyi.biz.util;

import org.apache.poi.ss.usermodel.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR/Excel 日期字符串归一化工具
 *
 * 输入: OCR 识别出来的原始日期字符串(可能含全角字符、中文日期、时间后缀等)
 * 输出: 规范化的 yyyy-MM-dd 字符串,失败时 Optional.empty()
 *
 * 支持的形态:
 *  - yyyy-MM-dd / yyyy-MM-dd HH:mm[:ss]
 *  - yyyy.MM.dd / yyyy/MM/dd(月日可不补零)
 *  - yyyy年M月d日(可后缀 HH:mm 或 HH:mm-HH:mm 时间区间,只取起始日)
 *  - yyyy年M月d日-d日 / yyyy年M月d日-d日 → DateRange(start, end),格式化 "start — end"
 *  - Excel 日期序列号(纯数字,范围 [10000, 100000] 覆盖 1927-2173 年)
 *
 * 全角字符会在匹配前先转半角。无法解析返回 Optional.empty(),
 * 由调用方决定是否把原值落到 remark 并打 WARN 日志。
 *
 * Why: 旧 normalizeOcrDate 只支持 yyyy-MM-dd 和 Excel 序列号,
 *      导致 2023.01.15 / 2023年1月15日 等日期落库时
 *      被静默 fallback 到 DateUtils.getNowDate(),造成脏数据。
 */
public final class OcrDateNormalizer {

    private OcrDateNormalizer() {
    }

    /**
     * 日期区间值对象。end == null 表示单日期(非区间)。
     * 用法: BizTrainOcrBatchServiceImpl 落 biz_train_ledger.train_date 时
     *       直接调 format() 拿到 "2026-01-22 — 2026-01-24" 拼接串
     *       (train_date 是 varchar,SQL 注释明确允许这种合并文本)。
     */
    public static final class DateRange {
        private final String start;
        private final String end;

        public DateRange(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public String getStart() { return start; }
        public String getEnd() { return end; }
        public boolean isRange() { return end != null; }
        public String format() { return isRange() ? start + " — " + end : start; }
        public static DateRange single(String date) { return new DateRange(date, null); }
    }

    private static final Pattern P_ISO_DATE = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})");
    private static final Pattern P_DOT_OR_SLASH = Pattern.compile("^(\\d{4})[./](\\d{1,2})[./](\\d{1,2})");
    private static final Pattern P_CN_DATE = Pattern.compile("^(\\d{4})\\s*年\\s*(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日?");

    /**
     * 中文日期区间正则,匹配:
     *   2026年01月22-24日   /  2026年01月22日-24日   /  2026年1月5日-8日
     * 起止月份共用起始月(用户明确"只会有这两种写法",不要求跨月支持)。
     */
    private static final Pattern P_CN_RANGE = Pattern.compile(
            "^(\\d{4})\\s*年\\s*(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日?\\s*[-~至到]\\s*(\\d{1,2})\\s*日?");

    /**
     * 向后兼容:返回单值(只取起始日)。区间输入时也只返 start。
     * 新代码请用 {@link #normalizeRange(String)}。
     */
    public static Optional<String> normalize(String raw) {
        return normalizeRange(raw).map(DateRange::getStart);
    }

    /**
     * 区间感知的日期归一化。区间命中返回 start/end 都非空的 DateRange,
     * 单日期返回 end == null 的 DateRange。
     */
    public static Optional<DateRange> normalizeRange(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String s = fullWidthToHalfWidth(raw).trim();
        if (s.isEmpty()) {
            return Optional.empty();
        }

        Matcher m = P_ISO_DATE.matcher(s);
        if (m.find()) {
            return validateAndFormat(m.group(1), m.group(2), m.group(3)).map(DateRange::single);
        }

        if (s.matches("^\\d+$")) {
            try {
                long n = Long.parseLong(s);
                if (n >= 10000 && n <= 100000) {
                    Date d = DateUtil.getJavaDate(n);
                    if (d != null) {
                        return Optional.of(DateRange.single(new SimpleDateFormat("yyyy-MM-dd").format(d)));
                    }
                }
            } catch (Exception ignore) {
                // 序列号解析失败,继续尝试下面的格式
            }
        }

        m = P_DOT_OR_SLASH.matcher(s);
        if (m.find()) {
            return validateAndFormat(m.group(1), m.group(2), m.group(3)).map(DateRange::single);
        }

        // 中文区间必须在中文单日期前匹配,否则 P_CN_DATE 会吞掉起始日
        m = P_CN_RANGE.matcher(s);
        if (m.find()) {
            int year = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            String startStr = String.format("%04d-%02d-%02d", year, month, Integer.parseInt(m.group(3)));
            String endStr = String.format("%04d-%02d-%02d", year, month, Integer.parseInt(m.group(4)));
            Optional<String> start = validateAndFormat(startStr);
            Optional<String> end = validateAndFormat(endStr);
            if (!start.isPresent() || !end.isPresent()) {
                return Optional.empty();
            }
            return Optional.of(new DateRange(start.get(), end.get()));
        }

        m = P_CN_DATE.matcher(s);
        if (m.find()) {
            return validateAndFormat(m.group(1), m.group(2), m.group(3)).map(DateRange::single);
        }

        return Optional.empty();
    }

    /**
     * 校验月份 1-12,日期 1-31 是否合法,然后返回 yyyy-MM-dd。
     * 校验失败的(如 2023.13.45 或 2023.02.30)返回 Optional.empty()。
     *
     * 重载:接受已补零的 yyyy-MM-dd 字符串,内部按 - 拆分。
     * 让 normalizeRange 区间分支不用反复构造 3 个独立参数。
     */
    private static Optional<String> validateAndFormat(String yyyyMmDd) {
        if (yyyyMmDd == null) return Optional.empty();
        String[] parts = yyyyMmDd.split("-");
        if (parts.length != 3) return Optional.empty();
        return validateAndFormat(parts[0], parts[1], parts[2]);
    }

    private static Optional<String> validateAndFormat(String y, String mo, String d) {
        try {
            int year = Integer.parseInt(y);
            int month = Integer.parseInt(mo);
            int day = Integer.parseInt(d);
            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return Optional.empty();
            }
            // 进一步用 SimpleDateFormat 严格校验,防止 2023.02.30 这类
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(String.format("%04d-%02d-%02d", year, month, day));
            return Optional.of(String.format("%04d-%02d-%02d", year, month, day));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String fullWidthToHalfWidth(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '　') {
                sb.append(' ');
            } else if (c >= '０' && c <= '９') {
                sb.append((char) (c - '０' + '0'));
            } else if (c == '．' || c == '。') {
                sb.append('.');
            } else if (c == '／') {
                sb.append('/');
            } else if (c == '：') {
                sb.append(':');
            } else if (c == '－' || c == '—') {
                sb.append('-');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}