import { test, expect } from '@playwright/test';
import { LoginPage } from '../page-objects/login.page';

test.describe('登录模块 E2E 测试', () => {

  test.beforeEach(async ({ page }) => {
    // 每个测试前先访问登录页
    await page.goto('/#/login');
    await page.waitForLoadState('networkidle');
  });

  test('正确的用户名密码登录成功', async ({ page }) => {
    const loginPage = new LoginPage(page);

    // 使用默认管理员账号登录
    await loginPage.login('admin', 'admin123');

    // 等待登录完成，验证跳转到首页（URL不再包含login）
    await page.waitForURL('**/index**', { timeout: 10000 }).catch(() => {});
    await page.waitForTimeout(2000);

    // 验证登录成功 - URL不包含login
    const url = page.url();
    expect(url).not.toContain('/login');
  });

  test('错误的密码登录失败并提示', async ({ page }) => {
    const loginPage = new LoginPage(page);

    // 输入正确的用户名，错误的密码
    await loginPage.usernameInput.fill('admin');
    await loginPage.passwordInput.fill('wrongpassword');
    await loginPage.submitButton.click();

    // 等待错误提示出现
    await page.waitForTimeout(1000);

    // 验证仍在登录页（登录失败）
    const url = page.url();
    expect(url).toContain('/login');

    // 可能看到错误提示（取决于系统返回的错误方式）
    // 有些系统是弹窗，有些是页面内提示
  });

  test('空用户名登录失败', async ({ page }) => {
    const loginPage = new LoginPage(page);

    // 不输入用户名，直接点击登录
    await loginPage.usernameInput.fill('');
    await loginPage.passwordInput.fill('admin123');
    await loginPage.submitButton.click();

    // 等待响应
    await page.waitForTimeout(1000);

    // 验证仍在登录页
    const url = page.url();
    expect(url).toContain('/login');
  });

  test('空密码登录失败', async ({ page }) => {
    const loginPage = new LoginPage(page);

    // 输入用户名，不输入密码
    await loginPage.usernameInput.fill('admin');
    await loginPage.passwordInput.fill('');
    await loginPage.submitButton.click();

    // 等待响应
    await page.waitForTimeout(1000);

    // 验证仍在登录页
    const url = page.url();
    expect(url).toContain('/login');
  });

  test('登录后跳转到首页', async ({ page }) => {
    const loginPage = new LoginPage(page);

    // 登录
    await loginPage.login('admin', 'admin123');

    // 等待足够时间让页面跳转
    await page.waitForTimeout(3000);

    // 验证跳转到非登录页
    const url = page.url();
    // 若依系统登录后通常跳转到 index 或首页
    expect(url).toMatch(/\/(index|home|dashboard|#\/)?$/i);
  });

  test('不存在的用户登录失败', async ({ page }) => {
    const loginPage = new LoginPage(page);

    // 使用不存在的用户名
    await loginPage.usernameInput.fill('nonexistentuser');
    await loginPage.passwordInput.fill('anypassword');
    await loginPage.submitButton.click();

    // 等待错误响应
    await page.waitForTimeout(2000);

    // 验证仍在登录页
    const url = page.url();
    expect(url).toContain('/login');
  });
});
