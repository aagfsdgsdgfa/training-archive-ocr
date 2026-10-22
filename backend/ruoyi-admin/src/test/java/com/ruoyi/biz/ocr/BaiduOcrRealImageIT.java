package com.ruoyi.biz.ocr;

import com.ruoyi.biz.controller.BizTrainPlanController;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实图片 OCR 集成测试（默认禁用，跑时加 -Dbaidu.real.ocr=true 启用）。
 * 跑前需 application.yml 中配置真实 baidu.aip.api-key / secret-key。
 */
@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.com.ruoyi.biz.ocr.BaiduOcrService=DEBUG"
})
@EnabledIfSystemProperty(named = "baidu.real.ocr", matches = "true")
class BaiduOcrRealImageIT {

    private static final String IMAGE_DIR = "C:/Users/Administrator/Desktop/trainarchive(3)";

    @Autowired private BaiduOcrService ocrService;
    @Autowired private BizTrainPlanController controller;

    private int invokeWriteOcrCellsToSheet(Sheet sheet, JSONObject ocr) throws Exception {
        Method m = BizTrainPlanController.class.getDeclaredMethod(
                "writeOcrCellsToSheet", Sheet.class, JSONObject.class, boolean.class, int.class);
        m.setAccessible(true);
        return (int) m.invoke(controller, sheet, ocr, true, 0);
    }

    @Test
    @DisplayName("图片 1（公司级）→ OCR 真实调用 → 归一化 → writeOcrCellsToSheet → 6 行")
    void image1_company() throws Exception {
        File f = new File(IMAGE_DIR, "图片 1.jpg");
        assertThat(f).exists();

        JSONObject ocr = ocrService.recognizeTable(f.getAbsolutePath());

        // 1. 归一化格式检查
        assertThat(ocr.has("result")).as("归一化后必须有 result 字段").isTrue();
        JSONObject res = ocr.getJSONObject("result");
        assertThat(res.getJSONArray("tables_result").length()).isGreaterThan(0);

        // 2. 写入 Sheet 并验证
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test");
        int written = invokeWriteOcrCellsToSheet(sheet, ocr);
        // 公司级：1 表头 + 5 数据 = 6 行
        assertThat(written).as("写入了 6 行（含表头）").isEqualTo(6);

        // 3. 验证表头行
        Row header = sheet.getRow(0);
        assertThat(header.getCell(0).getStringCellValue()).isEqualTo("序号");
        assertThat(header.getCell(1).getStringCellValue()).isEqualTo("培训内容");
        assertThat(header.getCell(2).getStringCellValue()).isEqualTo("培训月份");
        assertThat(header.getCell(3).getStringCellValue()).isEqualTo("培训课时");
        assertThat(header.getCell(4).getStringCellValue()).isEqualTo("负责授课部门/人员");
        assertThat(header.getCell(5).getStringCellValue()).isEqualTo("培训对象");

        // 4. 验证第一行数据
        Row r1 = sheet.getRow(1);
        assertThat(r1.getCell(0).getStringCellValue()).isEqualTo("1");
        assertThat(r1.getCell(1).getStringCellValue()).isEqualTo("中药材鉴别");
        assertThat(r1.getCell(2).getStringCellValue()).isEqualTo("5");
        assertThat(r1.getCell(3).getStringCellValue()).isEqualTo("2h");

        // 5. 验证最后一行
        Row rLast = sheet.getRow(5);
        assertThat(rLast.getCell(0).getStringCellValue()).isEqualTo("5");
        assertThat(rLast.getCell(1).getStringCellValue()).isEqualTo("消防安全培训");
    }

    @Test
    @DisplayName("图片2（部门级）→ OCR 真实调用 → 归一化 → writeOcrCellsToSheet → 3 行")
    void image2_dept() throws Exception {
        File f = new File(IMAGE_DIR, "图片2.jpg");
        assertThat(f).exists();

        JSONObject ocr = ocrService.recognizeTable(f.getAbsolutePath());
        assertThat(ocr.has("result")).isTrue();

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test");
        int written = invokeWriteOcrCellsToSheet(sheet, ocr);
        // 部门级：1 表头 + 2 数据 = 3 行
        assertThat(written).as("写入了 3 行（含表头）").isEqualTo(3);

        Row header = sheet.getRow(0);
        assertThat(header.getCell(0).getStringCellValue()).isEqualTo("序号");
        assertThat(header.getCell(4).getStringCellValue()).isEqualTo("培训对象");

        Row r1 = sheet.getRow(1);
        assertThat(r1.getCell(1).getStringCellValue()).isEqualTo("成本管理");
    }
}