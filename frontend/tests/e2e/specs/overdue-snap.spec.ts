import { test } from '@playwright/test';

test('截首页和计划页逾期对比图', async ({ page }) => {
  page.setDefaultTimeout(30000);

  // 登录
  await page.goto('http://localhost:82/');
  await page.waitForSelector('input[placeholder*="账"]', { timeout: 15000 });
  await page.locator('input[placeholder*="账"]').first().fill('admin');
  await page.locator('input[type="password"]').fill('admin123');
  await page.keyboard.press('Enter');
  await page.waitForTimeout(3000);

  // 首页
  await page.goto('http://localhost:82/index');
  await page.waitForTimeout(3000);
  const homeOverdueTags = page.locator('.el-tag', { hasText: '已逾期' });
  const homeCount = await homeOverdueTags.count();
  console.log(`首页找到 ${homeCount} 个"已逾期"标签`);
  await page.screenshot({ path: 'tests/e2e/results/home-overdue.png', fullPage: false });

  // 计划页
  await page.goto('http://localhost:82/train/plan');
  await page.waitForSelector('.el-table__body-wrapper', { timeout: 20000 });
  await page.waitForTimeout(3000);
  const planOverdueTags = page.locator('.el-table .el-tag', { hasText: '已逾期' });
  const planCount = await planOverdueTags.count();
  console.log(`计划页找到 ${planCount} 个"已逾期"标签`);
  await page.screenshot({ path: 'tests/e2e/results/plan-overdue.png', fullPage: false });
});
