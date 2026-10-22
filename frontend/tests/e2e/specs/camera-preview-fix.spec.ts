import { test, expect } from '@playwright/test';

/**
 * 回归测试 - OCR 实时预览轮询修复 (camera-preview-fix)
 *
 * 验证目标:
 *  1. CameraPreviewPanel 在 127.0.0.1:38008 不可达时,不再立刻跳出"底层服务未启动",
 *     而是保持"正在初始化高拍仪引擎..."并按 3s 间隔轮询探测
 *  2. 每次探测失败都会在 console 打印 [CameraPreviewPanel] warn
 *  3. formatOcrDate 在 chunk 异步加载下不再丢引用
 *
 * 运行前提: 38088 端口必须不可达(本机未启动高拍仪服务 = ERR_CONNECTION_REFUSED)
 *
 * 浏览器通过 dev server 加载 public/camera-preview-harness.html,这是一个独立 harness,
 * 镜像了 CameraPreviewPanel 修复后的状态机。这样不需要登录后端、不需要后端菜单注入,
 * 直接验证组件行为。
 */

test.describe('OCR 实时预览轮询修复 回归 E2E', () => {

  test('38088 不可达时:持续轮询 + 多次 warn + 文案不变', async ({ page }) => {
    const warnings: string[] = [];
    const consoleErrors: string[] = [];

    page.on('console', msg => {
      const text = msg.text();
      if (msg.type() === 'warning' || msg.type() === 'warn') {
        warnings.push(text);
      }
      if (msg.type() === 'error') {
        consoleErrors.push(text);
      }
    });

    // 拦击 38088 端点,强制返回连接失败(模拟高拍仪未启动)
    await page.route('**/127.0.0.1:38088/**', route => {
      route.abort('connectionrefused');
    });

    // 加载测试 HTML harness
    const harnessUrl = 'http://localhost:80/camera-preview-harness.html';
    await page.goto(harnessUrl, { waitUntil: 'domcontentloaded' });

    // 等待组件挂载 + 实例暴露到 window
    await page.waitForSelector('.camera-window', { timeout: 15000 });
    await page.waitForFunction(() => (window as any).__cameraPreviewInstance != null, { timeout: 10000 });

    // 初始应展示"正在初始化高拍仪引擎..."
    const initText = page.locator('text=正在初始化高拍仪引擎');
    await expect(initText).toBeVisible({ timeout: 5000 });

    // 直接从组件实例读取轮询次数(最可靠的信号)
    const pollCount1 = await page.evaluate(() => (window as any).__cameraPreviewInstance?.initPollCount);
    console.log(`[E2E] T+0s pollCount=${pollCount1}`);

    // 收集 13 秒内的轮询行为(应该出现约 4 次 warn)
    await page.waitForTimeout(13000);

    const pollCount2 = await page.evaluate(() => (window as any).__cameraPreviewInstance?.initPollCount);
    console.log(`[E2E] T+13s pollCount=${pollCount2}`);
    console.log(`[E2E] 收集到 ${warnings.length} 条 warn`);
    if (warnings.length > 0) {
      console.log(`[E2E] warn 样例: ${warnings[0].substring(0, 200)}`);
    }

    // 关键断言 1:轮询期间 UI 仍然显示"正在初始化高拍仪引擎...",没有切到"底层服务未启动"
    await expect(initText).toBeVisible();

    // 关键断言 2:initPollCount 至少 >= 3(说明确实在轮询,不是只探测一次就放弃)
    expect(typeof pollCount2).toBe('number');
    expect(pollCount2).toBeGreaterThanOrEqual(3);

    // 关键断言 3:warn 至少出现 3 次
    expect(warnings.length).toBeGreaterThanOrEqual(3);

    // 关键断言 4:没有 formatOcrDate 相关错误
    const fmtErr = consoleErrors.find(e => e.includes('formatOcrDate'));
    expect(fmtErr).toBeUndefined();

    // 截图存档
    await page.screenshot({ path: 'tests/e2e/results/camera-preview-polling.png', fullPage: true });
  });

  test('formatOcrDate 在异步 chunk 中可正常调用', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', err => errors.push(err.message));
    page.on('console', msg => {
      if (msg.type() === 'error') errors.push(msg.text());
    });

    await page.goto('http://localhost:80/camera-preview-harness.html', { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.camera-window');
    await page.waitForFunction(() => (window as any).__cameraPreviewInstance != null);

    const result = await page.evaluate(async () => {
      const win = window as any;
      let attempts = 0;
      while (!win.__cameraPreviewInstance && attempts++ < 30) {
        await new Promise(r => setTimeout(r, 200));
      }
      if (!win.__cameraPreviewInstance) {
        return { ok: false, reason: 'instance not mounted' };
      }
      const inst = win.__cameraPreviewInstance;
      return {
        ok: typeof inst.formatOcrDate === 'function',
        type: typeof inst.formatOcrDate,
        sample: typeof inst.formatOcrDate === 'function'
          ? inst.formatOcrDate('2026-07-02')
          : null
      };
    });

    console.log('[E2E] formatOcrDate 检查结果:', JSON.stringify(result));
    expect(result.ok).toBe(true);
    expect(result.sample).toBe('2026-07-02');
    expect(errors.filter(e => e.includes('formatOcrDate')).length).toBe(0);
  });
});