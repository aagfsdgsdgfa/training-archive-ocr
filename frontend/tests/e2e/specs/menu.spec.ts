import { test, expect } from '@playwright/test';
import { LoginPage } from '../page-objects/login.page';
import { MenuPage } from '../page-objects/menu.page';

test.describe('Menu Management E2E Tests', () => {
  let loginPage: LoginPage;
  let menuPage: MenuPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    menuPage = new MenuPage(page);

    // Login first - using default RuoYi credentials
    // Note: You may need to change these to valid credentials in your environment
    await loginPage.login('admin', 'admin123');
  });

  test('Navigate to Menu Management page', async ({ page }) => {
    // Now navigate to menu page
    await menuPage.goto();

    // Wait for page to load - look for any of these elements
    await page.waitForSelector('.el-table, .el-loading-mask, input[placeholder="请输入菜单名称"], .el-aside', { timeout: 15000 });

    // Page should have search input
    const searchInput = page.locator('input[placeholder="请输入菜单名称"]');
    await expect(searchInput).toBeVisible({ timeout: 10000 });
  });

  test('List menus - verify table displays data', async ({ page }) => {
    await menuPage.goto();
    await menuPage.waitForTableLoad();

    // Table should be visible
    const table = page.locator('.el-table');
    await expect(table).toBeVisible({ timeout: 10000 });
  });

  test('Search menu by name', async ({ page }) => {
    await menuPage.goto();
    await menuPage.waitForTableLoad();

    // Enter search text
    await menuPage.menuNameInput.fill('系统');
    await menuPage.searchButton.click();

    // Wait for results
    await page.waitForTimeout(2000);

    // Table should still be visible with potentially filtered results
    await expect(menuPage.menuTable).toBeVisible({ timeout: 5000 });
  });

  test('Add new menu with permission', async ({ page }) => {
    await menuPage.goto();
    await menuPage.waitForTableLoad();

    // Click add button
    await menuPage.clickAddButton();

    // Verify dialog opened
    await expect(menuPage.dialog).toBeVisible({ timeout: 10000 });

    // Fill menu form - Adding button type with biz:device:list permission
    const menuNameInput = menuPage.dialog.locator('input').first();
    await menuNameInput.fill('设备列表');

    // Select menu type F (按钮)
    await menuPage.dialog.locator('input[type="radio"][value="F"]').click();

    // Fill order number
    await menuPage.orderNumInput.fill('10');

    // Fill permission
    await menuPage.permsInput.fill('biz:device:list');

    // Submit
    await menuPage.submitForm();

    // Wait for success
    await page.waitForTimeout(2000);
  });

  test('Edit existing menu permission', async ({ page }) => {
    await menuPage.goto();
    await menuPage.waitForTableLoad();

    // Check if there are rows to edit
    const rows = await page.locator('.el-table__body-wrapper .el-table__row').all();

    if (rows.length > 0) {
      // Click edit on first row
      await menuPage.clickEditButton(0);

      // Verify dialog opened
      await expect(menuPage.dialog).toBeVisible({ timeout: 10000 });

      // Modify the permission field
      await menuPage.permsInput.clear();
      await menuPage.permsInput.fill('biz:device:list:updated');

      // Submit
      await menuPage.submitForm();

      // Wait for update
      await page.waitForTimeout(2000);
    }
  });

  test('Delete menu with confirmation', async ({ page }) => {
    await menuPage.goto();
    await menuPage.waitForTableLoad();

    // Check if there are rows to delete
    const rows = await page.locator('.el-table__body-wrapper .el-table__row').all();

    if (rows.length > 0) {
      // Click delete on first row
      await menuPage.clickDeleteButton(0);

      // Confirm deletion
      await menuPage.confirmDelete();

      // Wait for table to update
      await page.waitForTimeout(2000);
    }
  });

  test('Expand/Collapse table toggle', async ({ page }) => {
    await menuPage.goto();
    await menuPage.waitForTableLoad();

    // Click expand/collapse button
    await menuPage.expandCollapseButton.click();

    // Wait for toggle
    await page.waitForTimeout(1000);
  });

  test('Reset search form', async ({ page }) => {
    await menuPage.goto();
    await menuPage.waitForTableLoad();

    // Enter search text
    await menuPage.menuNameInput.fill('test search');

    // Click reset
    await menuPage.resetButton.click();

    // Wait for reset
    await page.waitForTimeout(1000);
  });
});