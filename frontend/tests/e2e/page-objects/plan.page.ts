import { Page, Locator } from '@playwright/test';

export class PlanPage {
  readonly page: Page;
  readonly baseURL = 'http://localhost:80';

  // Search form
  readonly planYearSelect: Locator;
  readonly keywordInput: Locator;
  readonly searchButton: Locator;
  readonly resetButton: Locator;

  // Action buttons
  readonly addButton: Locator;
  readonly importButton: Locator;

  // Table
  readonly menuTable: Locator;
  readonly tableRows: Locator;

  // Dialog
  readonly dialog: Locator;
  readonly dialogTitle: Locator;
  readonly submitButton: Locator;
  readonly cancelButton: Locator;

  // Delete modal
  readonly confirmModal: Locator;
  readonly confirmButton: Locator;

  constructor(page: Page) {
    this.page = page;

    // Search form
    this.planYearSelect = page.locator('.el-select').first();
    this.keywordInput = page.locator('input[placeholder="请输入计划名称"]').first();
    this.searchButton = page.locator('button:has-text("搜索")');
    this.resetButton = page.locator('button:has-text("重置")');

    // Action buttons
    this.addButton = page.locator('button:has-text("新增")');
    this.importButton = page.locator('button:has-text("导入")');

    // Table
    this.menuTable = page.locator('.el-table');
    this.tableRows = page.locator('.el-table__body-wrapper .el-table__row');

    // Dialog
    this.dialog = page.locator('.el-dialog');
    this.dialogTitle = page.locator('.el-dialog__title');
    this.submitButton = this.dialog.locator('button:has-text("确 定")');
    this.cancelButton = this.dialog.locator('button:has-text("取 消")');

    // Delete modal
    this.confirmModal = page.locator('.el-message-box');
    this.confirmButton = page.locator('.el-message-box__wrapper button:has-text("确定")');
  }

  async goto() {
    await this.page.goto(`${this.baseURL}/#/system/plan`);
    await this.page.waitForLoadState('networkidle');
    await this.page.waitForTimeout(3000);
  }

  async waitForTableLoad() {
    await this.page.waitForSelector('.el-table', { timeout: 15000 });
    await this.page.waitForFunction(() => {
      const loading = document.querySelector('.el-loading-mask');
      return !loading || loading.getAttribute('style')?.includes('display') !== null;
    }, { timeout: 15000 }).catch(() => {});
    await this.page.waitForTimeout(1000);
  }

  async clickAddButton() {
    await this.addButton.click();
    await this.dialog.waitFor({ state: 'visible', timeout: 10000 });
  }

  async clickEditButton(rowIndex: number = 0) {
    const rows = await this.page.locator('.el-table__body-wrapper .el-table__row').all();
    if (rows.length > rowIndex) {
      const editButton = rows[rowIndex].locator('button:has-text("修改")');
      await editButton.click();
      await this.dialog.waitFor({ state: 'visible', timeout: 10000 });
    }
  }

  async clickDeleteButton(rowIndex: number = 0) {
    const rows = await this.page.locator('.el-table__body-wrapper .el-table__row').all();
    if (rows.length > rowIndex) {
      const deleteButton = rows[rowIndex].locator('button:has-text("删除")');
      await deleteButton.click();
      await this.confirmModal.waitFor({ state: 'visible', timeout: 10000 });
    }
  }

  async confirmDelete() {
    await this.confirmButton.click();
    await this.confirmModal.waitFor({ state: 'hidden', timeout: 10000 });
  }

  async submitForm() {
    await this.submitButton.click();
    await this.dialog.waitFor({ state: 'hidden', timeout: 10000 });
  }

  async cancelForm() {
    await this.cancelButton.click();
    await this.dialog.waitFor({ state: 'hidden', timeout: 10000 });
  }

  async searchByKeyword(keyword: string) {
    await this.keywordInput.fill(keyword);
    await this.searchButton.click();
    await this.waitForTableLoad();
  }

  async clickImportButton() {
    await this.importButton.click();
    await this.dialog.waitFor({ state: 'visible', timeout: 10000 });
  }

  async getTableRowCount(): Promise<number> {
    return await this.tableRows.count();
  }

  async getPlanNames(): Promise<string[]> {
    const names: string[] = [];
    const rows = await this.tableRows.all();
    for (const row of rows) {
      const nameCell = row.locator('td').first();
      if (await nameCell.isVisible()) {
        names.push(await nameCell.textContent() || '');
      }
    }
    return names;
  }
}
