import { Page, Locator } from '@playwright/test';

export class MenuPage {
  readonly page: Page;
  readonly baseURL = 'http://localhost:80';

  // Search form
  readonly menuNameInput: Locator;
  readonly statusSelect: Locator;
  readonly searchButton: Locator;
  readonly resetButton: Locator;

  // Action buttons
  readonly addButton: Locator;
  readonly expandCollapseButton: Locator;

  // Table
  readonly menuTable: Locator;
  readonly tableRows: Locator;

  // Dialog
  readonly dialog: Locator;
  readonly dialogTitle: Locator;
  readonly parentMenuSelect: Locator;
  readonly menuTypeRadioGroup: Locator;
  readonly menuNameInputInDialog: Locator;
  readonly orderNumInput: Locator;
  readonly pathInput: Locator;
  readonly componentInput: Locator;
  readonly permsInput: Locator;
  readonly statusRadioGroup: Locator;
  readonly submitButton: Locator;
  readonly cancelButton: Locator;

  // Delete modal
  readonly confirmModal: Locator;
  readonly confirmButton: Locator;

  constructor(page: Page) {
    this.page = page;

    // Search form
    this.menuNameInput = page.locator('input[placeholder="请输入菜单名称"]').first();
    this.statusSelect = page.locator('.el-select').first();
    this.searchButton = page.locator('button:has-text("搜索")');
    this.resetButton = page.locator('button:has-text("重置")');

    // Action buttons
    this.addButton = page.locator('button:has-text("新增")');
    this.expandCollapseButton = page.locator('button:has-text("展开")');

    // Table
    this.menuTable = page.locator('.el-table');
    this.tableRows = page.locator('.el-table__body-wrapper .el-table__row');

    // Dialog
    this.dialog = page.locator('.el-dialog');
    this.dialogTitle = page.locator('.el-dialog__title');
    this.parentMenuSelect = page.locator('.vue-treeselect');
    this.menuTypeRadioGroup = page.locator('.el-radio-group');
    this.menuNameInputInDialog = this.dialog.locator('input').first();
    this.orderNumInput = page.locator('.el-input-number');
    this.pathInput = this.dialog.locator('input[placeholder="请输入路由地址"]');
    this.componentInput = this.dialog.locator('input[placeholder="请输入组件路径"]');
    this.permsInput = this.dialog.locator('input[placeholder="请输入权限标识"]');
    this.statusRadioGroup = this.dialog.locator('.el-radio-group');
    this.submitButton = this.dialog.locator('button:has-text("确 定")');
    this.cancelButton = this.dialog.locator('button:has-text("取 消")');

    // Delete modal
    this.confirmModal = page.locator('.el-message-box');
    this.confirmButton = page.locator('.el-message-box__wrapper button:has-text("确定")');
  }

  async goto() {
    // Navigate directly to the menu page via hash route
    await this.page.goto(`${this.baseURL}/#/system/menu`);
    await this.page.waitForLoadState('networkidle');
    // Wait for Vue app to mount
    await this.page.waitForTimeout(3000);
  }

  async waitForTableLoad() {
    await this.page.waitForSelector('.el-table', { timeout: 15000 });
    // Wait for loading to finish
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

  async fillMenuForm(data: {
    menuName?: string;
    menuType?: 'M' | 'C' | 'F';
    parentId?: number;
    orderNum?: number;
    path?: string;
    component?: string;
    perms?: string;
    status?: '0' | '1';
  }) {
    if (data.menuName) {
      const input = this.dialog.locator('input').first();
      await input.fill(data.menuName);
    }
    if (data.menuType) {
      await this.dialog.locator(`input[value="${data.menuType}"]`).click();
    }
    if (data.orderNum !== undefined) {
      await this.orderNumInput.fill(String(data.orderNum));
    }
    if (data.path) {
      await this.pathInput.fill(data.path);
    }
    if (data.component) {
      await this.componentInput.fill(data.component);
    }
    if (data.perms) {
      await this.permsInput.fill(data.perms);
    }
    if (data.status) {
      await this.dialog.locator(`input[value="${data.status}"]`).click();
    }
  }

  async submitForm() {
    await this.submitButton.click();
    await this.dialog.waitFor({ state: 'hidden', timeout: 10000 });
  }

  async cancelForm() {
    await this.cancelButton.click();
    await this.dialog.waitFor({ state: 'hidden', timeout: 10000 });
  }

  async searchByMenuName(name: string) {
    await this.menuNameInput.fill(name);
    await this.searchButton.click();
    await this.waitForTableLoad();
  }

  async getTableRowCount(): Promise<number> {
    return await this.tableRows.count();
  }

  async getMenuNames(): Promise<string[]> {
    const names: string[] = [];
    const rows = await this.tableRows.all();
    for (const row of rows) {
      const menuNameCell = row.locator('td').first();
      if (await menuNameCell.isVisible()) {
        names.push(await menuNameCell.textContent() || '');
      }
    }
    return names;
  }
}