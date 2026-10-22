package com.ruoyi.biz.controller;

import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.xssf.usermodel.*;

/**
 * 一次性工具：生成年度培训计划导出模板（两个独立文件）。
 *   - dept_plan_template.xlsx   （部门表：8列）
 *   - company_plan_template.xlsx （公司表：6列）
 *
 * 运行方式：
 *   mvn compile exec:java -Dexec.mainClass="com.ruoyi.biz.controller.CreatePlanTemplate" -pl ruoyi-admin
 * 或直接在 IDE 中运行 main()
 */
public class CreatePlanTemplate {

    public static void main(String[] args) throws Exception {
        new CreatePlanTemplate().generate(2026);
    }

    public void generate(int year) throws Exception {
        String outDir = "src/main/resources/templates/";
        new File(outDir).mkdirs();

        // ===== 通用样式定义 =====
        CellStyle titleStyle;
        CellStyle headerStyle;
        CellStyle dataStyle;
        CellStyle subTitleStyle;
        CellStyle noteStyle;

        try (Workbook wb = WorkbookFactory.create(true)) {
            titleStyle = wb.createCellStyle();
            Font tf = wb.createFont(); tf.setBold(true); tf.setFontHeightInPoints((short) 14);
            titleStyle.setFont(tf);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            headerStyle = wb.createCellStyle();
            Font hf = wb.createFont(); hf.setBold(true); hf.setFontHeightInPoints((short) 11);
            headerStyle.setFont(hf);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setWrapText(true);

            dataStyle = wb.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            subTitleStyle = wb.createCellStyle();
            Font sf = wb.createFont(); sf.setBold(true); sf.setFontHeightInPoints((short) 10);
            subTitleStyle.setFont(sf);
            subTitleStyle.setAlignment(HorizontalAlignment.LEFT);

            noteStyle = wb.createCellStyle();
            Font nf = wb.createFont(); nf.setFontHeightInPoints((short) 10);
            noteStyle.setFont(nf);

            // =====================================================
            // 模板1：dept_plan_template.xlsx（部门表，8列）
            // 序号, 部门, 培训内容, 培训月份, 培训课时, 培训对象, 完成情况, 备注
            // 注意：标题中含 {year} 占位符，导出时会被替换为实际年份
            // =====================================================
            Sheet deptSheet = wb.createSheet("{year}年度部门培训计划");

            // Row 0: 大标题
            deptSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            Row r0 = deptSheet.createRow(0); r0.setHeight((short) 500);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("{year}年度部门培训计划");
            c0.setCellStyle(titleStyle);

            // Row 1: 编号（生效日期也用 {year} 占位符，导出时替换）
            deptSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
            Row r1 = deptSheet.createRow(1); r1.setHeight((short) 300);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("编号：R-JR007-03    生效日期：{year}年01月01日");
            c1.setCellStyle(noteStyle);

            // Row 2: 目的说明
            deptSheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));
            Row r2 = deptSheet.createRow(2); r2.setHeight((short) 300);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("一、培训目的：使员工更全面地熟悉GMP管理规范、提高自身素质和工作技能；严格执行GMP物料管理流程，保证合格的物料投入生产，保证合格的产品才能流入市场。");
            c2.setCellStyle(noteStyle);

            // Row 3: 内容说明
            deptSheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 7));
            Row r3 = deptSheet.createRow(3); r3.setHeight((short) 300);
            Cell c3 = r3.createCell(0);
            c3.setCellValue("二、培训内容、月份及对象");
            c3.setCellStyle(subTitleStyle);

            // Row 4: 表头
            Row rh = deptSheet.createRow(4); rh.setHeight((short) 400);
            String[] hdrs = {"序号", "部门", "培训内容", "培训月份", "培训课时", "培训对象", "完成情况", "备注"};
            for (int i = 0; i < hdrs.length; i++) {
                Cell ch = rh.createCell(i);
                ch.setCellValue(hdrs[i]);
                ch.setCellStyle(headerStyle);
            }

            // Row 5: 模板数据行（POI 导出时从这里开始逐行写入）
            String[] data = {"{index}", "{deptName}", "{trainContent}", "{trainMonth}", "{trainHours}", "{trainTarget}", "{finishStatus}", "{remark}"};
            Row rd = deptSheet.createRow(5); rd.setHeight((short) 400);
            for (int i = 0; i < data.length; i++) {
                Cell cd = rd.createCell(i);
                cd.setCellValue(data[i]);
                cd.setCellStyle(dataStyle);
            }

            // Row 6: 备注
            deptSheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 7));
            Row rn = deptSheet.createRow(6); rn.setHeight((short) 300);
            Cell cn = rn.createCell(0);
            cn.setCellValue("注：完成情况请在培训结束后填写实际完成情况。");
            cn.setCellStyle(noteStyle);

            // 列宽
            int[] deptWidths = {2000, 4000, 6000, 3000, 3000, 4000, 3000, 4000};
            for (int i = 0; i < deptWidths.length; i++) deptSheet.setColumnWidth(i, deptWidths[i]);
            deptSheet.createFreezePane(0, 5);

            // 写出部门模板
            try (FileOutputStream fos = new FileOutputStream(outDir + "dept_plan_template.xlsx")) {
                wb.write(fos);
            }
            System.out.println("已生成: " + outDir + "dept_plan_template.xlsx（部门表，8列）");
            System.out.println("  Sheet: " + year + "年度部门培训计划");
        }

        // =====================================================
        // 模板2：company_plan_template.xlsx（公司表，6列）
        // 序号, 培训内容, 培训月份, 培训课时, 负责授课部门/人员, 培训对象
        // =====================================================
        try (Workbook wb = WorkbookFactory.create(true)) {
            CellStyle titleStyle2 = wb.createCellStyle();
            Font tf2 = wb.createFont(); tf2.setBold(true); tf2.setFontHeightInPoints((short) 14);
            titleStyle2.setFont(tf2);
            titleStyle2.setAlignment(HorizontalAlignment.CENTER);
            titleStyle2.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle headerStyle2 = wb.createCellStyle();
            Font hf2 = wb.createFont(); hf2.setBold(true); hf2.setFontHeightInPoints((short) 11);
            headerStyle2.setFont(hf2);
            headerStyle2.setAlignment(HorizontalAlignment.CENTER);
            headerStyle2.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle2.setBorderTop(BorderStyle.THIN);
            headerStyle2.setBorderBottom(BorderStyle.THIN);
            headerStyle2.setBorderLeft(BorderStyle.THIN);
            headerStyle2.setBorderRight(BorderStyle.THIN);
            headerStyle2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle2.setWrapText(true);

            CellStyle dataStyle2 = wb.createCellStyle();
            dataStyle2.setBorderTop(BorderStyle.THIN);
            dataStyle2.setBorderBottom(BorderStyle.THIN);
            dataStyle2.setBorderLeft(BorderStyle.THIN);
            dataStyle2.setBorderRight(BorderStyle.THIN);
            dataStyle2.setAlignment(HorizontalAlignment.CENTER);
            dataStyle2.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle subTitleStyle2 = wb.createCellStyle();
            Font sf2 = wb.createFont(); sf2.setBold(true); sf2.setFontHeightInPoints((short) 10);
            subTitleStyle2.setFont(sf2);
            subTitleStyle2.setAlignment(HorizontalAlignment.LEFT);

            CellStyle noteStyle2 = wb.createCellStyle();
            Font nf2 = wb.createFont(); nf2.setFontHeightInPoints((short) 10);
            noteStyle2.setFont(nf2);

            Sheet compSheet = wb.createSheet("{year}年度公司培训总计划");

            // Row 0: 大标题
            compSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            Row r0 = compSheet.createRow(0); r0.setHeight((short) 500);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("{year}年度公司培训总计划");
            c0.setCellStyle(titleStyle2);

            // Row 1: 编号+日期
            compSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
            compSheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 5));
            Row r1 = compSheet.createRow(1); r1.setHeight((short) 300);
            Cell c1a = r1.createCell(0); c1a.setCellValue("编号：R-JR006-03"); c1a.setCellStyle(noteStyle2);
            Cell c1b = r1.createCell(2); c1b.setCellValue("生效日期：{year}年10月01日"); c1b.setCellStyle(noteStyle2);

            // Row 2: 目的
            compSheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
            Row r2 = compSheet.createRow(2); r2.setHeight((short) 300);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("一、培训目的：1、提升职工综合素质和工作技能；2、认真贯彻GMP相关规定，严把质量关，保证产品质量。");
            c2.setCellStyle(noteStyle2);

            // Row 3: 内容说明
            compSheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 5));
            Row r3 = compSheet.createRow(3); r3.setHeight((short) 300);
            Cell c3 = r3.createCell(0);
            c3.setCellValue("二、培训内容、月份、课时、部门、对象");
            c3.setCellStyle(subTitleStyle2);

            // Row 4: 表头（6列）
            Row rh = compSheet.createRow(4); rh.setHeight((short) 400);
            String[] hdrs2 = {"序号", "培训内容", "培训月份", "培训课时", "负责授课部门/人员", "培训对象"};
            for (int i = 0; i < hdrs2.length; i++) {
                Cell ch = rh.createCell(i);
                ch.setCellValue(hdrs2[i]);
                ch.setCellStyle(headerStyle2);
            }

            // Row 5: 模板数据行
            String[] data2 = {"{index}", "{trainContent}", "{trainMonth}", "{trainHours}", "{instructor}", "{trainTarget}"};
            Row rd = compSheet.createRow(5); rd.setHeight((short) 400);
            for (int i = 0; i < data2.length; i++) {
                Cell cd = rd.createCell(i);
                cd.setCellValue(data2[i]);
                cd.setCellStyle(dataStyle2);
            }

            // Row 6: 三、培训地点及授课人
            compSheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 5));
            Row rn6 = compSheet.createRow(6); rn6.setHeight((short) 300);
            Cell cn6 = rn6.createCell(0);
            cn6.setCellValue("三、培训地点及授课人：根据培训内容自行安排。");
            cn6.setCellStyle(noteStyle2);

            // Row 7: 四、考核方式
            compSheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 5));
            Row rn7 = compSheet.createRow(7); rn7.setHeight((short) 300);
            Cell cn7 = rn7.createCell(0);
            cn7.setCellValue("四、考核方式：笔试（开卷或闭卷）；口头考核；也可现场实际操作或笔试与实际操作结合等。");
            cn7.setCellStyle(noteStyle2);

            // Row 8: 五、培训效果评估
            compSheet.addMergedRegion(new CellRangeAddress(8, 8, 0, 5));
            Row rn8 = compSheet.createRow(8); rn8.setHeight((short) 300);
            Cell cn8 = rn8.createCell(0);
            cn8.setCellValue("五、一年一次对培训效果进行评估。");
            cn8.setCellStyle(noteStyle2);

            // Row 9: 起草人 / 审核批准人 / 日期
            compSheet.addMergedRegion(new CellRangeAddress(9, 9, 0, 5));
            Row rn9 = compSheet.createRow(9); rn9.setHeight((short) 500);
            Cell cn9 = rn9.createCell(0);
            cn9.setCellValue("起草人：                                                      审核或批准人：");
            cn9.setCellStyle(noteStyle2);

            // Row 10: 日期空白行（供签字用）
            compSheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 5));
            Row rn10 = compSheet.createRow(10); rn10.setHeight((short) 500);
            Cell cn10 = rn10.createCell(0);
            cn10.setCellValue("日期：        年      月      日");
            cn10.setCellStyle(noteStyle2);

            // 列宽
            int[] compWidths = {2000, 6000, 3000, 3000, 5000, 4000};
            for (int i = 0; i < compWidths.length; i++) compSheet.setColumnWidth(i, compWidths[i]);
            compSheet.createFreezePane(0, 5);

            // 写出公司模板
            try (FileOutputStream fos = new FileOutputStream(outDir + "company_plan_template.xlsx")) {
                wb.write(fos);
            }
            System.out.println("已生成: " + outDir + "company_plan_template.xlsx（公司表，6列）");
            System.out.println("  Sheet: " + year + "年度公司培训总计划");
            System.out.println("完成！共2个模板文件。");
        }
    }
}
