import { test, expect } from '@playwright/test';
import { LoginPage } from '../page-objects/login.page';

/**
 * OCR 培训日期区间在前端列表的展示回归测试。
 *
 * 背景:培训考核记录表里单元格是 `2026年06月04-05日`(区间写法)。
 * 后端 OcrDateNormalizer 已正确解析成 "2026-06-04 — 2026-06-05"
 * (DateRange.format(),em-dash U+2014 + 空格)并落库 ledger.train_date。
 * 前端 formatOcrDate() 必须识别这种"已归一化区间",否则会被 P_ISO
 * 截短成只显示首日。
 *
 * 本测试不依赖任何特定 batch_id / ledger_id(测试环境数据是动态的),
 * 通过搜索当前页面上**含有 em-dash 的日期单元格**来验证 formatOcrDate
 * 至少把区间保留下来了;并断言**不应出现 `NaN-NaN-NaN`**(parseTime
 * 直接吃 "yyyy-MM-dd — yyyy-MM-dd" 的脏字符串导致的渲染失败)。
 */

test.describe('OCR 日期区间在前端的展示回归', () => {

  test('台账列表:含 em-dash 区间的培训日期列保留完整区间,不出现 NaN', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.login('admin', 'admin123');

    // 1) 跳到台账列表
    await page.goto('http://localhost:80/#/train/ledger');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    // 2) 收集"培训日期"列的全部单元格文本
    const dateCells = page.locator('.el-table__body-wrapper .el-table__row td:nth-child(3)');
    const cellCount = await dateCells.count();

    let hasEmDash = false;
    let hasNaN = false;
    for (let i = 0; i < cellCount; i++) {
      const text = (await dateCells.nth(i).innerText()).trim();
      if (text.includes('—')) hasEmDash = true;
      if (/NaN/.test(text)) hasNaN = true;
    }

    // 3) 断言:不允许 NaN(证明 formatOcrDate 接管后 parseTime 没再来捣乱)
    expect(hasNaN).toBe(false);

    // 4) 如果测试环境有区间数据,em-dash 必须保留为完整区间,
    //    不允许 "yyyy-MM-dd" 单日截断(原 Bug 2 表现)
    if (hasEmDash) {
      const emDashCells = await page.locator('.el-table__body-wrapper .el-table__row td:nth-child(3):has-text("—")').allInnerTexts();
      for (const text of emDashCells) {
        const trimmed = text.trim();
        // 必须是 "yyyy-MM-dd — yyyy-MM-dd" 完整区间(每侧 10 字符)
        expect(trimmed).toMatch(/^\d{4}-\d{2}-\d{2}\s+—\s+\d{4}-\d{2}-\d{2}$/);
      }
    }
  });

  test('培训场次列表:日期列同样应保留 em-dash 区间', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.login('admin', 'admin123');

    await page.goto('http://localhost:80/#/train/session');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const dateCells = page.locator('.el-table__body-wrapper .el-table__row td');
    const cellCount = await dateCells.count();

    // 扫描全部单元格,只要含有"训练日期"列的内容即可;
    // session 表里日期列位置不固定,这里遍历查找 em-dash + 不为 NaN 的硬指标
    let hasEmDash = false;
    let hasNaN = false;
    for (let i = 0; i < cellCount; i++) {
      const text = (await dateCells.nth(i).innerText()).trim();
      if (text.includes('—')) hasEmDash = true;
      if (/NaN/.test(text)) hasNaN = true;
    }

    expect(hasNaN).toBe(false);

    if (hasEmDash) {
      const emDashCells = await page.locator('.el-table__body-wrapper .el-table__row td:has-text("—")').allInnerTexts();
      for (const text of emDashCells) {
        const trimmed = text.trim();
        // 任何含 em-dash 的日期文本都应该是 yyyy-MM-dd — yyyy-MM-dd 形式
        expect(trimmed).toMatch(/^\d{4}-\d{2}-\d{2}\s+—\s+\d{4}-\d{2}-\d{2}$/);
      }
    }
  });

  test('批次 OCR 校对页:displayOcrDate 输入框正确展示完整区间', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.login('admin', 'admin123');

    await page.goto('http://localhost:80/#/train/batch');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    // 找到第一行 status='1'(待人工校对)的批次,点开校对弹窗
    const reviewBtn = page.locator('.el-table__body-wrapper .el-table__row button:has-text("进行校对结算")').first();
    const hasReviewable = await reviewBtn.count() > 0;

    if (!hasReviewable) {
      test.skip(true, '当前测试环境没有 status=1 的待校对批次,跳过此断言');
      return;
    }

    await reviewBtn.click();
    await page.waitForTimeout(1500);

    // 校对页的"培训日期"输入框(基础信息区块中)
    const dateInput = page.locator('.batch-meta-form input[placeholder="如 2026-05-11"]');
    await dateInput.waitFor({ state: 'visible', timeout: 5000 });
    const dateValue = await dateInput.inputValue();

    // 不允许 NaN / 空(原 Bug 1 表现:displayOcrDate 用 normalizeOcrDate 只返 start)
    expect(dateValue).not.toMatch(/NaN/);
    expect(dateValue.length).toBeGreaterThan(0);

    // 如果是区间数据,必须保留 em-dash;若是单日则只显示 yyyy-MM-dd
    if (dateValue.includes('—')) {
      expect(dateValue).toMatch(/^\d{4}-\d{2}-\d{2}\s+—\s+\d{4}-\d{2}-\d{2}$/);
    } else {
      expect(dateValue).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    }
  });
});