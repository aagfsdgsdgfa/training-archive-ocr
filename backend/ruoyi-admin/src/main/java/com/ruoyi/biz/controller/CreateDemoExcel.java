package com.ruoyi.biz.controller;

import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.xssf.usermodel.*;

/**
 * 一次性工具：生成演示用Excel表格（OCR版，使用正确的列顺序）。
 * OCR导入要求列顺序：A=姓名, B=部门, C=签到序号, D=考核成绩（空）
 *
 * 运行方式（项目根目录）：
 *   cd backend
 *   mvn compile exec:java -Dexec.mainClass="com.ruoyi.biz.controller.CreateDemoExcel" -pl ruoyi-admin
 * 或直接在 IDE 中运行 main()
 */
public class CreateDemoExcel {

    public static void main(String[] args) throws Exception {
        new CreateDemoExcel().generate();
    }

    public void generate() throws Exception {
        String outDir = "src/main/resources/templates/";
        new File(outDir).mkdirs();

        // ============================================================
        // 模板1：demo_dept_plan_2026.xlsx（部门表，8列）
        // ============================================================
        try (Workbook wb = WorkbookFactory.create(true)) {
            Sheet sheet = wb.createSheet("2026年度部门培训计划");

            CellStyle titleStyle = mkTitle(wb);
            CellStyle headerStyle = mkHeader(wb);
            CellStyle dataStyle = mkData(wb);
            CellStyle noteStyle = mkNote(wb);
            CellStyle subTitleStyle = mkSubTitle(wb);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            Row r0 = sheet.createRow(0); r0.setHeight((short) 500);
            r0.createCell(0).setCellValue("2026年度部门培训计划");
            r0.getCell(0).setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
            Row r1 = sheet.createRow(1); r1.setHeight((short) 300);
            r1.createCell(0).setCellValue("编号：R-JR007-03    生效日期：2026年01月01日");
            r1.getCell(0).setCellStyle(noteStyle);

            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));
            Row r2 = sheet.createRow(2); r2.setHeight((short) 300);
            r2.createCell(0).setCellValue(
                "一、培训目的：使员工更全面地熟悉GMP管理规范、提高自身素质和工作技能；" +
                "严格执行GMP物料管理流程，保证合格的物料投入生产，保证合格的产品才能流入市场。");
            r2.getCell(0).setCellStyle(noteStyle);

            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 7));
            Row r3 = sheet.createRow(3); r3.setHeight((short) 300);
            r3.createCell(0).setCellValue("二、培训内容、月份及对象");
            r3.getCell(0).setCellStyle(subTitleStyle);

            Row rh = sheet.createRow(4); rh.setHeight((short) 400);
            String[] hdrs = {"序号", "部门", "培训内容", "培训月份", "培训课时", "培训对象", "完成情况", "备注"};
            for (int i = 0; i < hdrs.length; i++) {
                Cell ch = rh.createCell(i);
                ch.setCellValue(hdrs[i]);
                ch.setCellStyle(headerStyle);
            }

            String[][] deptData = {
                {"1", "生产部",      "GMP生产管理规范",    "3月",  "2小时", "全体员工", "未完成", ""},
                {"2", "质量部",      "质量检验标准",        "5月",  "3小时", "全体员工", "未完成", ""},
                {"3", "仓储部",      "物料管理流程",        "7月",  "2小时", "全体员工", "未完成", ""},
                {"4", "设备部",      "设备维护与保养",      "9月",  "2小时", "全体员工", "未完成", ""},
                {"5", "人力资源部", "员工绩效考核培训",    "11月", "2小时", "部门主管", "未完成", ""},
            };
            for (int ri = 0; ri < deptData.length; ri++) {
                Row rd = sheet.createRow(5 + ri); rd.setHeight((short) 400);
                for (int ci = 0; ci < deptData[ri].length; ci++) {
                    Cell cd = rd.createCell(ci);
                    cd.setCellValue(deptData[ri][ci]);
                    cd.setCellStyle(dataStyle);
                }
            }

            sheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 7));
            Row rn = sheet.createRow(10); rn.setHeight((short) 300);
            rn.createCell(0).setCellValue("注：完成情况请在培训结束后填写实际完成情况。");
            rn.getCell(0).setCellStyle(noteStyle);

            int[] deptWidths = {2000, 4000, 6000, 3000, 3000, 4000, 3000, 4000};
            for (int i = 0; i < deptWidths.length; i++) sheet.setColumnWidth(i, deptWidths[i]);
            sheet.createFreezePane(0, 5);

            try (FileOutputStream fos = new FileOutputStream(outDir + "demo_dept_plan_2026.xlsx")) {
                wb.write(fos);
            }
            System.out.println("OK: " + outDir + "demo_dept_plan_2026.xlsx");
        }

        // ============================================================
        // 模板2：demo_company_plan_2026.xlsx（公司表，6列）
        // ============================================================
        try (Workbook wb = WorkbookFactory.create(true)) {
            Sheet sheet = wb.createSheet("2026年度公司培训总计划");

            CellStyle titleStyle = mkTitle(wb);
            CellStyle headerStyle = mkHeader(wb);
            CellStyle dataStyle = mkData(wb);
            CellStyle noteStyle = mkNote(wb);
            CellStyle subTitleStyle = mkSubTitle(wb);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            Row r0 = sheet.createRow(0); r0.setHeight((short) 500);
            r0.createCell(0).setCellValue("2026年度公司培训总计划");
            r0.getCell(0).setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 5));
            Row r1 = sheet.createRow(1); r1.setHeight((short) 300);
            r1.createCell(0).setCellValue("编号：R-JR006-03");
            r1.getCell(0).setCellStyle(noteStyle);
            r1.createCell(2).setCellValue("生效日期：2026年10月01日");
            r1.getCell(2).setCellStyle(noteStyle);

            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
            Row r2 = sheet.createRow(2); r2.setHeight((short) 300);
            r2.createCell(0).setCellValue(
                "一、培训目的：1、提升职工综合素质和工作技能；2、认真贯彻GMP相关规定，严把质量关，保证产品质量。");
            r2.getCell(0).setCellStyle(noteStyle);

            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 5));
            Row r3 = sheet.createRow(3); r3.setHeight((short) 300);
            r3.createCell(0).setCellValue("二、培训内容、月份、课时、部门、对象");
            r3.getCell(0).setCellStyle(subTitleStyle);

            Row rh = sheet.createRow(4); rh.setHeight((short) 400);
            String[] hdrs2 = {"序号", "培训内容", "培训月份", "培训课时", "负责授课部门/人员", "培训对象"};
            for (int i = 0; i < hdrs2.length; i++) {
                Cell ch = rh.createCell(i);
                ch.setCellValue(hdrs2[i]);
                ch.setCellStyle(headerStyle);
            }

            String[][] compData = {
                {"1", "新员工入职培训",   "1月",  "4小时", "人力资源部",   "新入职员工"},
                {"2", "安全生产培训",     "4月",  "2小时", "安全环保部",   "全体员工"},
                {"3", "GMP法规宣贯",      "6月",  "3小时", "质量管理部",   "全体员工"},
                {"4", "消防应急演练",     "7月",  "2小时", "安全环保部",   "全体员工"},
                {"5", "年度质量回顾培训", "12月", "3小时", "质量管理部",   "质量部/生产部"},
            };
            for (int ri = 0; ri < compData.length; ri++) {
                Row rd = sheet.createRow(5 + ri); rd.setHeight((short) 400);
                for (int ci = 0; ci < compData[ri].length; ci++) {
                    Cell cd = rd.createCell(ci);
                    cd.setCellValue(compData[ri][ci]);
                    cd.setCellStyle(dataStyle);
                }
            }

            String[] notes = {
                "三、培训地点及授课人：根据培训内容自行安排。",
                "四、考核方式：笔试（开卷或闭卷）；口头考核；也可现场实际操作或笔试与实际操作结合等。",
                "五、一年一次对培训效果进行评估。",
                "起草人：                                                      审核或批准人：",
                "日期：        年      月      日",
            };
            for (int ni = 0; ni < notes.length; ni++) {
                int rowIdx = 10 + ni;
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 5));
                Row nr = sheet.createRow(rowIdx); nr.setHeight((short) 500);
                nr.createCell(0).setCellValue(notes[ni]);
                nr.getCell(0).setCellStyle(noteStyle);
            }

            int[] compWidths = {2000, 6000, 3000, 3000, 5000, 4000};
            for (int i = 0; i < compWidths.length; i++) sheet.setColumnWidth(i, compWidths[i]);
            sheet.createFreezePane(0, 5);

            try (FileOutputStream fos = new FileOutputStream(outDir + "demo_company_plan_2026.xlsx")) {
                wb.write(fos);
            }
            System.out.println("OK: " + outDir + "demo_company_plan_2026.xlsx");
        }

        // ============================================================
        // 模板3：demo_ocr_signin_mixed.xlsx（OCR混合签到表）
        // 列顺序严格匹配 OCR 解析逻辑：
        //   A=姓名  B=部门  C=签到序号  D=考核成绩（留空）
        // ============================================================
        try (Workbook wb = WorkbookFactory.create(true)) {
            Sheet sheet = wb.createSheet("OCR混合签到表示例");

            CellStyle titleStyle = mkTitle(wb);
            CellStyle headerStyle = mkHeader(wb);
            CellStyle dataStyle = mkData(wb);
            CellStyle noteStyle = mkNote(wb);

            // Row 0: 标题
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
            Row r0 = sheet.createRow(0); r0.setHeight((short) 500);
            r0.createCell(0).setCellValue("OCR扫描签到表示例（老员工+新人员混合）");
            r0.getCell(0).setCellStyle(titleStyle);

            // Row 1: 说明
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
            Row r1 = sheet.createRow(1); r1.setHeight((short) 300);
            r1.createCell(0).setCellValue(
                "说明：A=姓名  B=部门  C=签到序号  D=考核成绩（留空）");
            r1.getCell(0).setCellStyle(noteStyle);

            // Row 2: 表头（必须4列）
            Row rh = sheet.createRow(2); rh.setHeight((short) 400);
            String[] hdrs3 = {"姓名", "部门", "签到序号", "考核成绩"};
            for (int i = 0; i < hdrs3.length; i++) {
                Cell ch = rh.createCell(i);
                ch.setCellValue(hdrs3[i]);
                ch.setCellStyle(headerStyle);
            }

            // Row 3~12: 演示数据
            // 混合：老员工（系统已有账号）和新人员（系统无账号，需入库时自动创建）
            String[][] ocrData = {
                // 老员工（isNewUser=0，系统已有账号）
                {"张伟",   "生产部",     "1",  ""},
                {"李娜",   "质量部",     "2",  ""},
                {"刘强",   "设备部",     "3",  ""},
                {"杨玲",   "质量部",     "4",  ""},
                {"周涛",   "人力资源部", "5",  ""},
                {"郑军",   "设备部",     "6",  ""},
                // 新人员（isNewUser=1，系统自动建号）
                {"王芳",   "仓储部",     "7",  ""},
                {"陈明",   "生产部",     "8",  ""},
                {"黄磊",   "安全环保部", "9",  ""},
                {"吴燕",   "生产部",     "10", ""},
            };
            for (int ri = 0; ri < ocrData.length; ri++) {
                Row rd = sheet.createRow(3 + ri); rd.setHeight((short) 400);
                for (int ci = 0; ci < ocrData[ri].length; ci++) {
                    Cell cd = rd.createCell(ci);
                    cd.setCellValue(ocrData[ri][ci]);
                    cd.setCellStyle(dataStyle);
                }
            }

            // 备注
            int noteRow = 3 + ocrData.length + 1;
            sheet.addMergedRegion(new CellRangeAddress(noteRow, noteRow, 0, 3));
            Row rn = sheet.createRow(noteRow); rn.setHeight((short) 300);
            rn.createCell(0).setCellValue(
                "注：前6行为老员工（系统已有账号），后4行为新人员（系统自动建号）。" +
                "导入后系统会自动区分：老员工关联已有账号，新人员自动创建临时账号。");
            rn.getCell(0).setCellStyle(noteStyle);

            int[] ocrWidths = {4000, 5000, 3000, 4000};
            for (int i = 0; i < ocrWidths.length; i++) sheet.setColumnWidth(i, ocrWidths[i]);
            sheet.createFreezePane(0, 3);

            try (FileOutputStream fos = new FileOutputStream(outDir + "demo_ocr_signin_mixed.xlsx")) {
                wb.write(fos);
            }
            System.out.println("OK: " + outDir + "demo_ocr_signin_mixed.xlsx");
        }

        System.out.println("Done! 3 demo files generated.");
    }

    // ---- 样式方法 ----

    private CellStyle mkTitle(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font tf = wb.createFont();
        tf.setBold(true);
        tf.setFontHeightInPoints((short) 14);
        cs.setFont(tf);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        return cs;
    }

    private CellStyle mkHeader(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font hf = wb.createFont();
        hf.setBold(true);
        hf.setFontHeightInPoints((short) 11);
        cs.setFont(hf);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setWrapText(true);
        return cs;
    }

    private CellStyle mkData(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        return cs;
    }

    private CellStyle mkNote(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font nf = wb.createFont();
        nf.setFontHeightInPoints((short) 10);
        cs.setFont(nf);
        return cs;
    }

    private CellStyle mkSubTitle(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font sf = wb.createFont();
        sf.setBold(true);
        sf.setFontHeightInPoints((short) 10);
        cs.setFont(sf);
        cs.setAlignment(HorizontalAlignment.LEFT);
        return cs;
    }
}
