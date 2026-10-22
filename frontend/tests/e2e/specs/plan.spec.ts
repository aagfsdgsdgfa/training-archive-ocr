import { test, expect } from '@playwright/test';
import { LoginPage } from '../page-objects/login.page';
import { PlanPage } from '../page-objects/plan.page';

test.describe('年度培训计划 E2E 测试', () => {
  let loginPage: LoginPage;
  let planPage: PlanPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    planPage = new PlanPage(page);

    // 登录
    await loginPage.login('admin', 'admin123');
  });

  test('导航到培训计划页面', async ({ page }) => {
    await planPage.goto();

    // 等待页面加载
    await page.waitForSelector('.el-table, .el-loading-mask, input[placeholder="请输入计划名称"]', { timeout: 15000 });

    // 验证搜索输入框可见
    const searchInput = page.locator('input[placeholder="请输入计划名称"]');
    await expect(searchInput).toBeVisible({ timeout: 10000 });
  });

  test('列表显示培训计划数据', async ({ page }) => {
    await planPage.goto();
    await planPage.waitForTableLoad();

    // 表格应可见
    const table = page.locator('.el-table');
    await expect(table).toBeVisible({ timeout: 10000 });
  });

  test('按关键词搜索计划', async ({ page }) => {
    await planPage.goto();
    await planPage.waitForTableLoad();

    // 输入搜索关键词
    await planPage.keywordInput.fill('安全');
    await planPage.searchButton.click();

    // 等待结果
    await page.waitForTimeout(2000);

    // 表格应仍然可见
    await expect(planPage.menuTable).toBeVisible({ timeout: 5000 });
  });

  test('新增培训计划', async ({ page }) => {
    await planPage.goto();
    await planPage.waitForTableLoad();

    // 点击新增按钮
    await planPage.clickAddButton();

    // 验证对话框打开
    await expect(planPage.dialog).toBeVisible({ timeout: 10000 });

    // 关闭对话框
    await planPage.cancelForm();
  });

  test('修改培训计划', async ({ page }) => {
    await planPage.goto();
    await planPage.waitForTableLoad();

    // 检查是否有行可以编辑
    const rows = await page.locator('.el-table__body-wrapper .el-table__row').all();

    if (rows.length > 0) {
      // 点击编辑
      await planPage.clickEditButton(0);

      // 验证对话框打开
      await expect(planPage.dialog).toBeVisible({ timeout: 10000 });

      // 关闭对话框
      await planPage.cancelForm();
    }
  });

  test('删除培训计划', async ({ page }) => {
    await planPage.goto();
    await planPage.waitForTableLoad();

    // 检查是否有行可以删除
    const rows = await page.locator('.el-table__body-wrapper .el-table__row').all();

    if (rows.length > 0) {
      // 点击删除
      await planPage.clickDeleteButton(0);

      // 确认删除
      await planPage.confirmDelete();

      // 等待表格更新
      await page.waitForTimeout(2000);
    }
  });

  test('重置搜索表单', async ({ page }) => {
    await planPage.goto();
    await planPage.waitForTableLoad();

    // 输入搜索条件
    await planPage.keywordInput.fill('test search');

    // 点击重置
    await planPage.resetButton.click();

    // 等待重置
    await page.waitForTimeout(1000);
  });

  test('展开折叠表格', async ({ page }) => {
    await planPage.goto();
    await planPage.waitForTableLoad();

    // 查找展开/折叠按钮
    const expandButton = page.locator('button:has-text("展开")');

    if (await expandButton.isVisible()) {
      await expandButton.click();
      await page.waitForTimeout(1000);
    }
  });
});
