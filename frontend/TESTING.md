# 测试文档

## 概述

本项目使用 Playwright 作为 E2E（端到端）测试框架，对关键用户流程进行自动化测试。

**技术栈：**
- 测试框架：Playwright
- 语言：TypeScript
- 测试浏览器：Chromium

---

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 安装 Playwright 浏览器

```bash
npx playwright install
```

### 3. 启动开发服务器

```bash
npm run dev
```

### 4. 运行测试

```bash
npx playwright test
```

---

## 测试结构

```
tests/
└── e2e/
    ├── specs/              # 测试用例
    │   └── menu.spec.ts    # 菜单管理测试
    ├── page-objects/       # 页面对象
    │   ├── login.page.ts   # 登录页面
    │   └── menu.page.ts    # 菜单页面
    ├── playwright.config.ts # Playwright 配置
    ├── results/            # 测试结果（自动生成）
    └── report/             # HTML 报告（自动生成）
```

### 页面对象模式

采用 Page Object 模式，将页面元素和操作封装到独立的类中：

- **LoginPage**：`login.page.ts` - 处理登录操作
- **MenuPage**：`menu.page.ts` - 处理菜单管理页面的所有操作

---

## 配置文件

`playwright.config.ts` 主要配置：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `baseURL` | http://localhost:80 | 被测应用地址 |
| `workers` | 并行数 | CI 环境为 1，其他环境并行 |
| `retries` | CI 环境 2 次 | 失败重试次数 |
| `reporter` | html, list, junit | 报告格式 |
| `trace` | on-first-retry | 首次失败时保存 trace |
| `screenshot` | only-on-failure | 仅失败时截图 |
| `video` | retain-on-failure | 仅失败时录制视频 |

---

## 现有测试用例

### 菜单管理测试 (menu.spec.ts)

| 测试名称 | 描述 |
|---------|------|
| Navigate to Menu Management page | 导航到菜单管理页面 |
| List menus - verify table displays data | 验证表格显示数据 |
| Search menu by name | 按名称搜索菜单 |
| Add new menu with permission | 添加新菜单（含权限） |
| Edit existing menu permission | 编辑现有菜单权限 |
| Delete menu with confirmation | 删除菜单（需确认） |
| Expand/Collapse table toggle | 展开/折叠表格 |
| Reset search form | 重置搜索表单 |

---

## 编写新测试

### 1. 创建页面对象（如需新页面）

```typescript
// tests/e2e/page-objects/new-page.ts
import { Page, Locator } from '@playwright/test';

export class NewPage {
  readonly page: Page;
  readonly someButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.someButton = page.locator('button:has-text("按钮文字")');
  }

  async doSomething() {
    await this.someButton.click();
  }
}
```

### 2. 创建测试用例

```typescript
// tests/e2e/specs/new.spec.ts
import { test, expect } from '@playwright/test';
import { LoginPage } from '../page-objects/login.page';
import { NewPage } from '../page-objects/new.page';

test.describe('新功能测试', () => {
  let loginPage: LoginPage;
  let newPage: NewPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    newPage = new NewPage(page);
    await loginPage.login('admin', 'admin123');
  });

  test('测试场景描述', async ({ page }) => {
    // 测试步骤
  });
});
```

### 3. 运行单个测试

```bash
npx playwright test tests/e2e/specs/new.spec.ts
```

---

## 查看测试结果

### HTML 报告

```bash
# 报告位置
tests/e2e/report/index.html
```

### JUnit XML 报告

```bash
# 报告位置
tests/e2e/results/results.xml
```

### Trace 录制

失败测试的 trace 文件保存在 `tests/e2e/results/` 目录，使用以下命令查看：

```bash
npx playwright show-trace tests/e2e/results/trace.zip
```

---

## 最佳实践

### 推荐

1. **使用页面对象**：将页面元素和操作封装到 Page 类中
2. **语义化等待**：优先使用 `waitForSelector`、`waitForResponse` 而非 `waitForTimeout`
3. **唯一选择器**：使用 `data-testid` 或稳定的唯一属性
4. **测试独立性**：每个测试应能独立运行
5. **清晰的测试描述**：使用描述性的测试名称

### 避免

1. 硬编码凭证 → 使用环境变量
2. 过度使用 `waitForTimeout` → 使用语义化等待
3. 脆弱的中文占位符选择器 → 使用 `data-testid` 或唯一选择器
4. 测试间的隐式依赖 → 确保测试独立

---

## 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `E2E_TEST_USERNAME` | admin | 测试用户名 |
| `E2E_TEST_PASSWORD` | admin123 | 测试密码 |
| `CI` | - | 设为 true 时启用 CI 模式（重试、串行执行） |

---

## 常见问题

### 测试失败：找不到元素

1. 检查页面是否正确加载（等待 `networkidle`）
2. 增加等待时间
3. 使用开发者工具确认选择器正确

### 测试不稳定

1. 检查是否有异步操作未等待
2. 增加适当的重试机制
3. 检查是否有动画干扰

### 浏览器无法启动

```bash
npx playwright install chromium
```

---

## TODO

- [ ] 添加更多 E2E 测试用例
- [ ] 集成到 CI/CD 流程
- [ ] 添加视觉回归测试
- [ ] 使用环境变量管理凭证
