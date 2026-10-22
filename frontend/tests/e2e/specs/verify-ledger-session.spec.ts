import { test, expect } from '@playwright/test';

test('端到端验证 ledger / session 列表 - 通过菜单点击', async ({ page }) => {
  const errors: string[] = [];
  const consoleLogs: string[] = [];
  page.on('pageerror', err => errors.push(`[PAGEERROR] ${err.message}\n  stack: ${err.stack}`));
  page.on('console', msg => {
    if (msg.type() === 'error' || msg.type() === 'warning') {
      consoleLogs.push(`[${msg.type()}] ${msg.text()}`);
    }
  });

  console.log('=== Step 1: 登录 admin ===');
  await page.goto('http://localhost/');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(2000);

  // 可能在登录页或首页
  if (page.url().includes('/login') || page.url().includes('#/login')) {
    console.log('  检测到登录页,自动登录');
    await page.locator('input[placeholder="账号"]').fill('admin');
    await page.locator('input[placeholder="密码"]').fill('admin123');
    await page.locator('button.submit-btn').click();
    await page.waitForURL(url => !url.toString().includes('/login'), { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(3000);
  }
  console.log('  当前 URL:', page.url());

  console.log('\n=== Step 2: 通过菜单点击 培训管理 → 培训台账记录 ===');
  // 列出侧边栏所有可见菜单文本
  const menuTexts = await page.locator('.sidebar-menu').first().innerText().catch(() => '');
  console.log('  侧边栏文本前 200:', menuTexts.substring(0, 200).replace(/\n/g, ' | '));

  // 用更宽松的 selector
  const menuTrain = page.locator('text=培训管理').first();
  await menuTrain.click();
  await page.waitForTimeout(1500);

  // 培训台账记录 子菜单
  const menuLedger = page.locator('text=培训台账记录').first();
  await menuLedger.click();
  await page.waitForTimeout(4000);
  await page.waitForLoadState('networkidle');

  await page.screenshot({ path: 'tests/e2e/results/ledger-via-menu.png', fullPage: true });
  console.log('  截图: tests/e2e/results/ledger-via-menu.png');
  console.log('  URL:', page.url());

  await page.waitForSelector('.el-table', { timeout: 10000 }).catch(() => console.log('  ⚠️ el-table 未出现'));
  await page.waitForTimeout(1500);

  const ledgerRowCount = await page.locator('.el-table__body-wrapper .el-table__row').count();
  console.log(`  表格行数: ${ledgerRowCount}`);

  if (ledgerRowCount > 0) {
    const allCells = await page.locator('.el-table__body-wrapper .el-table__row').first().locator('td').allInnerTexts();
    console.log(`  第一行全部单元格(${allCells.length} 列):`);
    allCells.forEach((c, i) => console.log(`    [${i}]: ${c.substring(0, 60)}`));
  } else {
    const pageText = (await page.locator('body').innerText()).substring(0, 500);
    console.log('  页面文本前 500 字符:');
    console.log('  ', pageText.replace(/\n/g, ' | '));
  }

  if (errors.length > 0) {
    console.log('\n  Page Errors:');
    errors.forEach(e => console.log('  ', e));
    errors.length = 0;
  }

  if (consoleLogs.length > 0) {
    console.log('\n  Console (前 10 条):');
    consoleLogs.slice(0, 10).forEach(l => console.log('  ', l));
  }

  // 深入诊断:把 el-table 内部的 tbody HTML 抓出来
  const tableHtml = await page.evaluate(() => {
    const tbody = document.querySelector('.el-table__body-wrapper tbody');
    return tbody ? tbody.outerHTML.substring(0, 2000) : 'NO TBODY';
  });
  console.log('\n  tbody HTML 前 2000 字符:');
  console.log('  ', tableHtml.replace(/\n/g, ' '));

  console.log('\n=== Step 3: 通过菜单点击 培训管理 → 培训场次 ===');
  // 培训管理菜单已展开,直接找子项
  await page.waitForTimeout(1000);
  const menuSession = page.locator('.sidebar-container .el-menu-item:has-text("培训场次")').first();
  await menuSession.click();
  await page.waitForTimeout(4000);
  await page.waitForLoadState('networkidle');

  await page.screenshot({ path: 'tests/e2e/results/session-via-menu.png', fullPage: true });
  console.log('  截图: tests/e2e/results/session-via-menu.png');
  console.log('  URL:', page.url());

  await page.waitForSelector('.el-table', { timeout: 10000 }).catch(() => console.log('  ⚠️ el-table 未出现'));
  await page.waitForTimeout(1500);

  const sessionRowCount = await page.locator('.el-table__body-wrapper .el-table__row').count();
  console.log(`  表格行数: ${sessionRowCount}`);

  if (sessionRowCount > 0) {
    const allCells = await page.locator('.el-table__body-wrapper .el-table__row').first().locator('td').allInnerTexts();
    console.log(`  第一行全部单元格(${allCells.length} 列):`);
    allCells.forEach((c, i) => console.log(`    [${i}]: ${c.substring(0, 60)}`));
  } else {
    const pageText = (await page.locator('body').innerText()).substring(0, 500);
    console.log('  页面文本前 500 字符:');
    console.log('  ', pageText.replace(/\n/g, ' | '));
  }

  if (errors.length > 0) {
    console.log('\n  Page Errors:');
    errors.forEach(e => console.log('  ', e));
  }

  const sessionTableHtml = await page.evaluate(() => {
    const tbody = document.querySelector('.el-table__body-wrapper tbody');
    return tbody ? tbody.outerHTML.substring(0, 2000) : 'NO TBODY';
  });
  console.log('\n  session tbody HTML 前 2000 字符:');
  console.log('  ', sessionTableHtml.replace(/\n/g, ' '));
});