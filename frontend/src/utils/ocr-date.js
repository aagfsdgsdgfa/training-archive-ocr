/**
 * OCR/Excel 日期字符串归一化工具(纯函数,与后端 OcrDateNormalizer 规则一致)
 *
 * 用途: 把前端从后端拿到的原始 OCR 日期字符串规范化为 yyyy-MM-dd,
 *       失败返回 null,UI 层负责回退展示原值并标记"待复核"。
 *
 * 支持的形态:
 *  - yyyy-MM-dd / yyyy-MM-dd HH:mm[:ss]
 *  - yyyy.MM.dd / yyyy/MM/dd(月日可不补零)
 *  - yyyy年M月d日(可后缀 HH:mm 或 HH:mm-HH:mm 时间区间,只取起始日)
 *  - yyyy年M月d日-d日 / yyyy年M月d日-d日 → {start, end},格式化为 "start — end"
 *  - yyyy-MM-dd — yyyy-MM-dd(已归一化区间,DateRange.format() 输出,落库 ledger.train_date 形态)
 *  - Excel 日期序列号(纯数字,范围 [10000, 100000] 覆盖 1927-2173 年)
 */

const P_ISO = /^(\d{4})-(\d{2})-(\d{2})/
const P_DOT_SLASH = /^(\d{4})[./](\d{1,2})[./](\d{1,2})/
const P_CN = /^(\d{4})\s*年\s*(\d{1,2})\s*月\s*(\d{1,2})\s*日?/

// 中文日期区间:2026年01月22-24日 / 2026年01月22日-24日 / 2026年1月5日-8日
// 起止月份共用起始月(后端已确认"只会有这两种写法",不要求跨月支持)
const P_CN_RANGE = /^(\d{4})\s*年\s*(\d{1,2})\s*月\s*(\d{1,2})\s*日?\s*[-~至到]\s*(\d{1,2})\s*日?/

// 已归一化的区间字符串 "yyyy-MM-dd — yyyy-MM-dd"(DateRange.format() 的输出格式,
// ledger.train_date 落库后就是这个形态)。必须在 P_ISO 之前匹配,
// 否则 P_ISO 会只吞掉前半截,导致 displayTrainDate 展示成只剩首日。
const P_NORMALIZED_RANGE = /^(\d{4})-(\d{2})-(\d{2})\s*—\s*(\d{4})-(\d{2})-(\d{2})/

function full2half(s) {
  return s
    .replace(/[！-～]/g, ch => String.fromCharCode(ch.charCodeAt(0) - 0xFEE0))
    .replace(/　/g, ' ')
    .replace(/．/g, '.')
    .replace(/／/g, '/')
    .replace(/：/g, ':')
}

function pad(n) { return String(n).padStart(2, '0') }

/**
 * 校验月日是否合法(月 1-12,日 1-31,且能被 Date 解析)。
 * 主要拦截 2023.13.45 / 2023.02.30 这类"形式合法但日期非法"的输入。
 */
function validateAndFormat(y, mo, d) {
  const year = parseInt(y, 10)
  const month = parseInt(mo, 10)
  const day = parseInt(d, 10)
  if (month < 1 || month > 12 || day < 1 || day > 31) return null
  const dt = new Date(Date.UTC(year, month - 1, day))
  if (dt.getUTCFullYear() !== year || dt.getUTCMonth() !== month - 1 || dt.getUTCDate() !== day) {
    return null
  }
  return `${pad(year)}-${pad(month)}-${pad(day)}`
}

/**
 * 区间感知的日期归一化。返回 {start, end},单日期时 end = null。
 * 区间拼接显示请用 formatOcrDate(),避免各处散写" — "分隔符。
 */
export function normalizeOcrDateRange(raw) {
  if (raw === null || raw === undefined) return null
  const s = full2half(String(raw)).trim()
  if (!s) return null

  let m = P_NORMALIZED_RANGE.exec(s)
  if (m) {
    const start = validateAndFormat(m[1], m[2], m[3])
    const end = validateAndFormat(m[4], m[5], m[6])
    if (start && end) return { start, end }
    return null
  }

  m = P_ISO.exec(s)
  if (m) {
    const v = validateAndFormat(m[1], m[2], m[3])
    return v ? { start: v, end: null } : null
  }

  if (/^\d+$/.test(s)) {
    const n = parseInt(s, 10)
    if (n >= 10000 && n <= 100000) {
      const utcDays = n - 25569
      const d = new Date(utcDays * 86400 * 1000)
      return { start: `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}`, end: null }
    }
    return null
  }

  m = P_DOT_SLASH.exec(s)
  if (m) {
    const v = validateAndFormat(m[1], m[2], m[3])
    return v ? { start: v, end: null } : null
  }

  // 中文区间必须在中文单日期前匹配,否则 P_CN 会吞掉起始日
  m = P_CN_RANGE.exec(s)
  if (m) {
    const year = parseInt(m[1], 10)
    const month = parseInt(m[2], 10)
    const startV = validateAndFormat(m[1], m[2], m[3])
    const endV = validateAndFormat(String(year), String(month), m[4])
    if (!startV || !endV) return null
    return { start: startV, end: endV }
  }

  m = P_CN.exec(s)
  if (m) {
    const v = validateAndFormat(m[1], m[2], m[3])
    return v ? { start: v, end: null } : null
  }

  return null
}

/**
 * 向后兼容:只返回起始日(区间输入时也只返 start)。新代码请用 normalizeOcrDateRange。
 */
export function normalizeOcrDate(raw) {
  const r = normalizeOcrDateRange(raw)
  return r ? r.start : null
}

/**
 * 给前端展示用的格式化函数。区间拼接成 "start — end"(长破折号 U+2014),
 * 与后端 DateRange.format() 输出保持完全一致。
 * 失败返回 null,UI 层负责回退展示原值并标记"待复核"。
 */
export function formatOcrDate(raw) {
  const r = normalizeOcrDateRange(raw)
  if (!r) return null
  return r.end ? `${r.start} — ${r.end}` : r.start
}