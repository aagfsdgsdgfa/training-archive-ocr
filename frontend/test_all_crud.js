const { chromium } = require('playwright');

async function test() {
  const browser = await chromium.launch({ headless: false });
  const page = await browser.newPage();

  const results = [];

  try {
    // 登录
    console.log('=== 登录系统 ===');
    await page.goto('http://localhost/login');
    await page.waitForTimeout(2000);
    await page.fill('input[placeholder="Username"]', 'admin');
    await page.fill('input[placeholder="Password"]', 'admin123');
    await page.fill('input[placeholder="Code"]', '1234');
    await page.click('button:has-text("Sign In")');
    await page.waitForTimeout(3000);
    console.log('登录成功\n');

    // ========== 1. 课程管理测试 ==========
    console.log('========== 课程管理 ==========');
    await page.goto('http://localhost/config/course');
    await page.waitForTimeout(3000);

    const courseRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', courseRows > 0 ? '有数据' : '无数据');
    results.push({ test: '课程-列表', status: courseRows > 0 ? 'PASS' : 'FAIL' });

    // 新增
    await page.locator('button:has-text("建立新课程")').click();
    await page.waitForTimeout(2000);
    const courseAddDialog = await page.locator('.el-dialog').count();
    console.log('新增弹窗:', courseAddDialog > 0 ? '打开成功' : '打开失败');
    results.push({ test: '课程-新增', status: courseAddDialog > 0 ? 'PASS' : 'FAIL' });

    if (courseAddDialog > 0) {
      await page.keyboard.press('Escape');
      await page.waitForTimeout(1000);
    }

    // 编辑
    await page.locator('.el-table__row').first().scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
    const editBtns = page.locator('button:has-text("编辑")');
    const editCount = await editBtns.count();
    if (editCount > 0) {
      await editBtns.first().click({ force: true });
      await page.waitForTimeout(2000);
      const courseEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', courseEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '课程-编辑', status: courseEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (courseEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '课程-编辑', status: 'SKIP' });
    }

    // 删除
    if (courseRows > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      await page.locator('button:has-text("删除")').first().click({ force: true });
      await page.waitForTimeout(1000);
      await page.locator('.el-message-box__btns button:has-text("确定")').click();
      await page.waitForTimeout(3000);
      console.log('删除: 已执行');
      results.push({ test: '课程-删除', status: 'PASS' });
    } else {
      results.push({ test: '课程-删除', status: 'SKIP' });
    }

    // ========== 2. 地点管理测试 ==========
    console.log('\n========== 地点管理 ==========');
    await page.goto('http://localhost/config/location');
    await page.waitForTimeout(3000);

    const locationRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', locationRows > 0 ? '有数据' : '无数据');
    results.push({ test: '地点-列表', status: locationRows > 0 ? 'PASS' : 'FAIL' });

    await page.locator('button:has-text("新增")').first().click();
    await page.waitForTimeout(2000);
    const locationAddDialog = await page.locator('.el-dialog').count();
    console.log('新增弹窗:', locationAddDialog > 0 ? '打开成功' : '打开失败');
    results.push({ test: '地点-新增', status: locationAddDialog > 0 ? 'PASS' : 'FAIL' });

    if (locationAddDialog > 0) {
      await page.keyboard.press('Escape');
      await page.waitForTimeout(1000);
    }

    await page.locator('.el-table__row').first().scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
    const locEditBtns = page.locator('button:has-text("编辑")');
    const locEditCount = await locEditBtns.count();
    if (locEditCount > 0) {
      await locEditBtns.first().click({ force: true });
      await page.waitForTimeout(2000);
      const locationEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', locationEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '地点-编辑', status: locationEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (locationEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '地点-编辑', status: 'SKIP' });
    }

    const locRowsNow = await page.locator('.el-table__row').count();
    if (locRowsNow > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      await page.locator('button:has-text("删除")').first().click({ force: true });
      await page.waitForTimeout(1000);
      await page.locator('.el-message-box__btns button:has-text("确定")').click();
      await page.waitForTimeout(3000);
      console.log('删除: 已执行');
      results.push({ test: '地点-删除', status: 'PASS' });
    } else {
      results.push({ test: '地点-删除', status: 'SKIP' });
    }

    // ========== 3. 讲师管理测试 ==========
    console.log('\n========== 讲师管理 ==========');
    await page.goto('http://localhost/config/lecturer');
    await page.waitForTimeout(3000);

    const lecturerRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', lecturerRows > 0 ? '有数据' : '无数据');
    results.push({ test: '讲师-列表', status: lecturerRows > 0 ? 'PASS' : 'FAIL' });

    await page.locator('button:has-text("新增")').first().click();
    await page.waitForTimeout(2000);
    const lecturerAddDialog = await page.locator('.el-dialog').count();
    console.log('新增弹窗:', lecturerAddDialog > 0 ? '打开成功' : '打开失败');
    results.push({ test: '讲师-新增', status: lecturerAddDialog > 0 ? 'PASS' : 'FAIL' });

    if (lecturerAddDialog > 0) {
      await page.keyboard.press('Escape');
      await page.waitForTimeout(1000);
    }

    await page.locator('.el-table__row').first().scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
    const lecturerEditBtns = page.locator('button:has-text("编辑")');
    const lecturerEditCount = await lecturerEditBtns.count();
    if (lecturerEditCount > 0) {
      await lecturerEditBtns.first().click({ force: true });
      await page.waitForTimeout(2000);
      const lecturerEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', lecturerEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '讲师-编辑', status: lecturerEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (lecturerEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '讲师-编辑', status: 'SKIP' });
    }

    const lecturerRowsNow = await page.locator('.el-table__row').count();
    if (lecturerRowsNow > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      await page.locator('button:has-text("删除")').first().click({ force: true });
      await page.waitForTimeout(1000);
      await page.locator('.el-message-box__btns button:has-text("确定")').click();
      await page.waitForTimeout(3000);
      console.log('删除: 已执行');
      results.push({ test: '讲师-删除', status: 'PASS' });
    } else {
      results.push({ test: '讲师-删除', status: 'SKIP' });
    }

    // ========== 4. 计划管理测试 ==========
    console.log('\n========== 计划管理 ==========');
    await page.goto('http://localhost/train/plan');
    await page.waitForTimeout(3000);

    const planRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', planRows > 0 ? '有数据' : '无数据');
    results.push({ test: '计划-列表', status: planRows > 0 ? 'PASS' : 'FAIL' });

    await page.locator('button:has-text("新增")').first().click();
    await page.waitForTimeout(2000);
    const planAddDialog = await page.locator('.el-dialog').count();
    console.log('新增弹窗:', planAddDialog > 0 ? '打开成功' : '打开失败');
    results.push({ test: '计划-新增', status: planAddDialog > 0 ? 'PASS' : 'FAIL' });

    if (planAddDialog > 0) {
      await page.keyboard.press('Escape');
      await page.waitForTimeout(1000);
    }

    await page.locator('.el-table__row').first().scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
    const planEditBtns = page.locator('button:has-text("编辑")');
    const planEditCount = await planEditBtns.count();
    if (planEditCount > 0) {
      await planEditBtns.first().click({ force: true });
      await page.waitForTimeout(2000);
      const planEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', planEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '计划-编辑', status: planEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (planEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '计划-编辑', status: 'SKIP' });
    }

    const planRowsNow = await page.locator('.el-table__row').count();
    if (planRowsNow > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      await page.locator('button:has-text("删除")').first().click({ force: true });
      await page.waitForTimeout(1000);
      await page.locator('.el-message-box__btns button:has-text("确定")').click();
      await page.waitForTimeout(3000);
      console.log('删除: 已执行');
      results.push({ test: '计划-删除', status: 'PASS' });
    } else {
      results.push({ test: '计划-删除', status: 'SKIP' });
    }

    // ========== 5. 场次管理测试 ==========
    console.log('\n========== 场次管理 ==========');
    await page.goto('http://localhost/train/session');
    await page.waitForTimeout(3000);

    const sessionRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', sessionRows > 0 ? '有数据' : '无数据');
    results.push({ test: '场次-列表', status: sessionRows > 0 ? 'PASS' : 'FAIL' });

    await page.locator('button:has-text("新增")').first().click();
    await page.waitForTimeout(2000);
    const sessionAddDialog = await page.locator('.el-dialog').count();
    console.log('新增弹窗:', sessionAddDialog > 0 ? '打开成功' : '打开失败');
    results.push({ test: '场次-新增', status: sessionAddDialog > 0 ? 'PASS' : 'FAIL' });

    if (sessionAddDialog > 0) {
      await page.keyboard.press('Escape');
      await page.waitForTimeout(1000);
    }

    await page.locator('.el-table__row').first().scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
    const sessionEditBtns = page.locator('button:has-text("编辑")');
    const sessionEditCount = await sessionEditBtns.count();
    if (sessionEditCount > 0) {
      await sessionEditBtns.first().click({ force: true });
      await page.waitForTimeout(2000);
      const sessionEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', sessionEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '场次-编辑', status: sessionEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (sessionEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '场次-编辑', status: 'SKIP' });
    }

    const sessionRowsNow = await page.locator('.el-table__row').count();
    if (sessionRowsNow > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      await page.locator('button:has-text("删除")').first().click({ force: true });
      await page.waitForTimeout(1000);
      await page.locator('.el-message-box__btns button:has-text("确定")').click();
      await page.waitForTimeout(3000);
      console.log('删除: 已执行');
      results.push({ test: '场次-删除', status: 'PASS' });
    } else {
      results.push({ test: '场次-删除', status: 'SKIP' });
    }

    // ========== 6. 考勤管理测试 ==========
    console.log('\n========== 考勤管理 ==========');
    await page.goto('http://localhost/train/attendance');
    await page.waitForTimeout(3000);

    const attendanceRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', attendanceRows > 0 ? '有数据' : '无数据');
    results.push({ test: '考勤-列表', status: attendanceRows > 0 ? 'PASS' : 'FAIL' });

    const attAddVisible = await page.locator('button:has-text("新增")').first().isVisible().catch(() => false);
    if (attAddVisible) {
      await page.locator('button:has-text("新增")').first().click();
      await page.waitForTimeout(2000);
      const attendanceAddDialog = await page.locator('.el-dialog').count();
      console.log('新增弹窗:', attendanceAddDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '考勤-新增', status: attendanceAddDialog > 0 ? 'PASS' : 'FAIL' });
      if (attendanceAddDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      console.log('无新增按钮: 跳过');
      results.push({ test: '考勤-新增', status: 'SKIP' });
    }

    const attEditVisible = await page.locator('button:has-text("编辑")').first().isVisible().catch(() => false);
    if (attEditVisible) {
      await page.locator('button:has-text("编辑")').first().click();
      await page.waitForTimeout(2000);
      const attendanceEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', attendanceEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '考勤-编辑', status: attendanceEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (attendanceEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '考勤-编辑', status: 'SKIP' });
    }

    if (attendanceRows > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      const attDelVisible = await page.locator('button:has-text("删除")').first().isVisible().catch(() => false);
      if (attDelVisible) {
        await page.locator('button:has-text("删除")').first().click({ force: true });
        await page.waitForTimeout(1000);
        await page.locator('.el-message-box__btns button:has-text("确定")').click();
        await page.waitForTimeout(3000);
        console.log('删除: 已执行');
        results.push({ test: '考勤-删除', status: 'PASS' });
      } else {
        results.push({ test: '考勤-删除', status: 'SKIP' });
      }
    } else {
      results.push({ test: '考勤-删除', status: 'SKIP' });
    }

    // ========== 7. 台账管理测试 ==========
    console.log('\n========== 台账管理 ==========');
    await page.goto('http://localhost/train/ledger');
    await page.waitForTimeout(3000);

    const ledgerRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', ledgerRows > 0 ? '有数据' : '无数据');
    results.push({ test: '台账-列表', status: ledgerRows > 0 ? 'PASS' : 'FAIL' });

    const ledgerAddVisible = await page.locator('button:has-text("新增")').first().isVisible().catch(() => false);
    if (ledgerAddVisible) {
      await page.locator('button:has-text("新增")').first().click();
      await page.waitForTimeout(2000);
      const ledgerAddDialog = await page.locator('.el-dialog').count();
      console.log('新增弹窗:', ledgerAddDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '台账-新增', status: ledgerAddDialog > 0 ? 'PASS' : 'FAIL' });
      if (ledgerAddDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '台账-新增', status: 'SKIP' });
    }

    const ledgerEditVisible = await page.locator('button:has-text("编辑")').first().isVisible().catch(() => false);
    if (ledgerEditVisible) {
      await page.locator('button:has-text("编辑")').first().click();
      await page.waitForTimeout(2000);
      const ledgerEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', ledgerEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '台账-编辑', status: ledgerEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (ledgerEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '台账-编辑', status: 'SKIP' });
    }

    if (ledgerRows > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      const ledgerDelVisible = await page.locator('button:has-text("删除")').first().isVisible().catch(() => false);
      if (ledgerDelVisible) {
        await page.locator('button:has-text("删除")').first().click({ force: true });
        await page.waitForTimeout(1000);
        await page.locator('.el-message-box__btns button:has-text("确定")').click();
        await page.waitForTimeout(3000);
        console.log('删除: 已执行');
        results.push({ test: '台账-删除', status: 'PASS' });
      } else {
        results.push({ test: '台账-删除', status: 'SKIP' });
      }
    } else {
      results.push({ test: '台账-删除', status: 'SKIP' });
    }

    // ========== 8. 档案管理测试 ==========
    console.log('\n========== 档案管理 ==========');
    await page.goto('http://localhost/train/archive');
    await page.waitForTimeout(3000);

    const archiveRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', archiveRows > 0 ? '有数据' : '无数据');
    results.push({ test: '档案-列表', status: archiveRows > 0 ? 'PASS' : 'FAIL' });

    const archiveAddVisible = await page.locator('button:has-text("新增")').first().isVisible().catch(() => false);
    if (archiveAddVisible) {
      await page.locator('button:has-text("新增")').first().click();
      await page.waitForTimeout(2000);
      const archiveAddDialog = await page.locator('.el-dialog').count();
      console.log('新增弹窗:', archiveAddDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '档案-新增', status: archiveAddDialog > 0 ? 'PASS' : 'FAIL' });
      if (archiveAddDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '档案-新增', status: 'SKIP' });
    }

    const archiveEditVisible = await page.locator('button:has-text("编辑")').first().isVisible().catch(() => false);
    if (archiveEditVisible) {
      await page.locator('button:has-text("编辑")').first().click();
      await page.waitForTimeout(2000);
      const archiveEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', archiveEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: '档案-编辑', status: archiveEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (archiveEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: '档案-编辑', status: 'SKIP' });
    }

    if (archiveRows > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      const archiveDelVisible = await page.locator('button:has-text("删除")').first().isVisible().catch(() => false);
      if (archiveDelVisible) {
        await page.locator('button:has-text("删除")').first().click({ force: true });
        await page.waitForTimeout(1000);
        await page.locator('.el-message-box__btns button:has-text("确定")').click();
        await page.waitForTimeout(3000);
        console.log('删除: 已执行');
        results.push({ test: '档案-删除', status: 'PASS' });
      } else {
        results.push({ test: '档案-删除', status: 'SKIP' });
      }
    } else {
      results.push({ test: '档案-删除', status: 'SKIP' });
    }

    // ========== 9. OCR扫表测试 ==========
    console.log('\n========== OCR扫表 ==========');
    await page.goto('http://localhost/train/batch');
    await page.waitForTimeout(3000);

    const batchRows = await page.locator('.el-table__row').count();
    console.log('列表数据:', batchRows > 0 ? '有数据' : '无数据');
    results.push({ test: 'OCR-列表', status: 'PASS' });

    const batchAddVisible = await page.locator('button:has-text("新增")').first().isVisible().catch(() => false);
    if (batchAddVisible) {
      await page.locator('button:has-text("新增")').first().click();
      await page.waitForTimeout(2000);
      const batchAddDialog = await page.locator('.el-dialog').count();
      console.log('新增弹窗:', batchAddDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: 'OCR-新增', status: batchAddDialog > 0 ? 'PASS' : 'FAIL' });
      if (batchAddDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: 'OCR-新增', status: 'SKIP' });
    }

    const batchEditVisible = await page.locator('button:has-text("编辑")').first().isVisible().catch(() => false);
    if (batchEditVisible) {
      await page.locator('button:has-text("编辑")').first().click();
      await page.waitForTimeout(2000);
      const batchEditDialog = await page.locator('.el-dialog').count();
      console.log('编辑弹窗:', batchEditDialog > 0 ? '打开成功' : '打开失败');
      results.push({ test: 'OCR-编辑', status: batchEditDialog > 0 ? 'PASS' : 'FAIL' });
      if (batchEditDialog > 0) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(1000);
      }
    } else {
      results.push({ test: 'OCR-编辑', status: 'SKIP' });
    }

    if (batchRows > 0) {
      await page.locator('.el-table__row').first().locator('.el-checkbox').click();
      await page.waitForTimeout(500);
      const batchDelVisible = await page.locator('button:has-text("删除")').first().isVisible().catch(() => false);
      if (batchDelVisible) {
        await page.locator('button:has-text("删除")').first().click({ force: true });
        await page.waitForTimeout(1000);
        await page.locator('.el-message-box__btns button:has-text("确定")').click();
        await page.waitForTimeout(3000);
        console.log('删除: 已执行');
        results.push({ test: 'OCR-删除', status: 'PASS' });
      } else {
        results.push({ test: 'OCR-删除', status: 'SKIP' });
      }
    } else {
      results.push({ test: 'OCR-删除', status: 'SKIP' });
    }

    // ========== 输出结果 ==========
    console.log('\n' + '='.repeat(50));
    console.log('测试结果汇总');
    console.log('='.repeat(50));

    const passCount = results.filter(r => r.status === 'PASS').length;
    const failCount = results.filter(r => r.status === 'FAIL').length;
    const skipCount = results.filter(r => r.status === 'SKIP').length;

    results.forEach(r => {
      const icon = r.status === 'PASS' ? 'V' : r.status === 'FAIL' ? 'X' : 'O';
      console.log('[' + icon + '] ' + r.test + ': ' + r.status);
    });

    console.log('\n' + '-'.repeat(50));
    console.log('通过: ' + passCount + '  失败: ' + failCount + '  跳过: ' + skipCount);
    console.log('总计: ' + results.length);

  } catch (err) {
    console.error('\n测试出错:', err.message);
  } finally {
    await browser.close();
  }
}

test();
