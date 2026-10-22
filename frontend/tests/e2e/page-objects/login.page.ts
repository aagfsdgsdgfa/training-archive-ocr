import { Page, Locator } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly baseURL: string;

  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.baseURL = 'http://localhost:80';

    this.usernameInput = page.locator('input[placeholder="请输入用户名"]');
    this.passwordInput = page.locator('input[placeholder="请输入密码"]');
    this.submitButton = page.locator('button:has-text("登 录")');
  }

  async goto() {
    await this.page.goto(`${this.baseURL}/#/login`);
    await this.page.waitForLoadState('networkidle');
  }

  async login(username: string, password: string) {
    await this.goto();

    // Wait for login form to be visible
    await this.usernameInput.waitFor({ state: 'visible', timeout: 10000 });

    // Fill username and password
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);

    // Click login button
    await this.submitButton.click();

    // Wait for navigation after login
    await this.page.waitForLoadState('networkidle');
    await this.page.waitForTimeout(2000);
  }

  async isLoggedIn(): Promise<boolean> {
    // Check if we're on a page that indicates logged in state (not login page)
    const url = this.page.url();
    return !url.includes('/login');
  }
}