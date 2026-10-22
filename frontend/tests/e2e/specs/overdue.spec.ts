import { test, expect } from '@playwright/test';

test('BUG-001 已逾期标签在年度计划列表中显示', async ({ page }) => {
  page.setDefaultTimeout(30000);

  // 登录
  await page.goto('http://localhost:82/');
  await page.waitForSelector('input[placeholder*="账"]', { timeout: 15000 });
  await page.locator('input[placeholder*="账"]').first().fill('admin');
  await page.locator('input[type="password"]').fill('admin123');
  // 勾选"记住我"如有
  // 提交登录（按 Enter 或点登录按钮）
  await page.keyboard.press('Enter');
  await page.waitForTimeout(2000);

  // 直接打开年度计划管理
  await page.goto('http://localhost:82/train/plan');
  await page.waitForSelector('.el-table__body-wrapper', { timeout: 20000 });
  // 等待 loading mask 消失
  await page.waitForFunction(() => {
    const mask = document.querySelector('.el-loading-mask');
    return !mask || (mask as HTMLElement).style.display === 'none';
  }, { timeout: 15000 });
  await page.waitForTimeout(2000);

  // 在搜索框输入"中药材"过滤
  const subjectInput = page.locator('input[placeholder*="培训主题"]').first();
  if (await subjectInput.isVisible()) {
    await subjectInput.fill('中药材');
    await subjectInput.press('Enter');
    await page.waitForTimeout(2000);
  }

  // 表格里找包含"已逾期"文字的 el-tag
  const overdueTag = page.locator('.el-table .el-tag', { hasText: '已逾期' });
  const count = await overdueTag.count();
  console.log(`找到 ${count} 个"已逾期"标签`);

  if (count > 0) {
    // 截图取证
    await page.screenshot({ path: 'tests/e2e/results/bug-001-overdue.png', fullPage: true });
    await expect(overdueTag.first()).toBeVisible();
  } else {
    // 输出整页 HTML 帮助调试
    const html = await page.locator('.el-table__body-wrapper').first().innerHTML().catch(() => 'no table');
    console.log('表格 HTML 片段:', html.substring(0, 2000));
    throw new Error('未找到"已逾期"标签 — BUG-001 修复未生效');
  }
});
