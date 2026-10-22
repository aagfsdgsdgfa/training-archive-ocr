package com.ruoyi.biz.service;

import com.ruoyi.biz.service.impl.BizTrainOcrBatchServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 回归测试:档案备注编排优先级
 *
 * 历史 bug:BizTrainOcrBatchServiceImpl.confirmAndGenerate() 中
 *   if (dateRemark != null) { archive.setRemark(dateRemark); }
 * 直接覆盖所有人的真实 OCR 备注,导致 /train/archive 整页备注空白。
 *
 * 修复后由 composeArchiveRemark(ocrRemark, dateRemark) 静态方法统一编排,
 * 优先级:OCR 备注 > 日期兜底;两者都有则拼接。
 *
 * 本类单测该静态方法以防再次回归。
 */
class BizTrainArchiveRemarkComposeTest {

    @Test
    @DisplayName("只有 OCR 备注时,返回原 OCR 备注(空白被 trim)")
    void ocrOnly() {
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark("员工请假半天", null))
                .isEqualTo("员工请假半天");
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark("  学员请教假  ", null))
                .isEqualTo("学员请教假");
    }

    @Test
    @DisplayName("只有日期兜底时,返回日期兜底本身")
    void dateOnly() {
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark(null, "日期无法识别: 2024.06.15-06.16"))
                .isEqualTo("日期无法识别: 2024.06.15-06.16");
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark("", "日期无法识别: 2024.06.15-06.16"))
                .isEqualTo("日期无法识别: 2024.06.15-06.16");
    }

    @Test
    @DisplayName("两者都有时,拼接为 'dateRemark | ocrRemark'(这是关键回归保护)")
    void bothPresent_concatenate() {
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark(
                "学员请教假", "日期无法识别: 2024.06.15-06.16"))
                .isEqualTo("日期无法识别: 2024.06.15-06.16 | 学员请教假");
    }

    @Test
    @DisplayName("两者都有 + OCR 含前后空白:OCR 会被 trim")
    void bothPresent_ocrTrimmed() {
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark(
                "  员工请假半天  ", "日期无法识别: 2024.06.15-06.16"))
                .isEqualTo("日期无法识别: 2024.06.15-06.16 | 员工请假半天");
    }

    @Test
    @DisplayName("两者都为空/null:返回 null(让 SQL 走 NULL,不写入 remark 列)")
    void bothEmpty_returnsNull() {
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark(null, null)).isNull();
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark("", "")).isNull();
        assertThat(BizTrainOcrBatchServiceImpl.composeArchiveRemark("   ", "\t")).isNull();
    }

    @Test
    @DisplayName("拼接串前端 '日期无法识别' tag 正则仍能命中(确保拼接策略不破坏现有 UI)")
    void concatenationPreservesDateTagRegex() {
        String composed = BizTrainOcrBatchServiceImpl.composeArchiveRemark(
                "学员请教假", "日期无法识别: 2024.06.15-06.16");
        assertThat(composed)
                .matches(s -> s != null && s.contains("日期无法识别"))
                .as("拼接后 dateRemark 在头部,前端 /日期无法识别/ 正则仍能命中");
    }
}
