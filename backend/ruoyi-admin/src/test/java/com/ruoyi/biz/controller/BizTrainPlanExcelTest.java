package com.ruoyi.biz.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Excel 解析逻辑验证脚本（无 JUnit 依赖）
 * 运行: mvn compile test-compile exec:java -Dexec.mainClass=com.ruoyi.biz.controller.BizTrainPlanExcelTest -pl ruoyi-admin
 */
public class BizTrainPlanExcelTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Excel 解析逻辑测试 ===\n");

        testGetCellStr_NumericCell_ReturnsInteger();
        testBuildColMap_VariableColumnOrder();
        testParseMonth_VariousFormats();
        testReadActualExcelFile();

        System.out.println("\n=== 测试结果 ===");
        System.out.println("  通过: " + passed);
        System.out.println("  失败: " + failed);
        if (failed > 0) {
            System.out.println("\n有 " + failed + " 项测试失败，请修复后重试！");
            System.exit(1);
        } else {
            System.out.println("\n全部测试通过！");
        }
    }

    // ===== 1. getCellStr: NUMERIC 列应返回整数（无小数点） =====
    static void testGetCellStr_NumericCell_ReturnsInteger() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Test");
        Row row = sheet.createRow(0);
        // 在 Excel 里存 2.0（POI 内部用 double 存储）
        row.createCell(0).setCellValue(2.0);
        row.createCell(1).setCellValue(1.5);
        row.createCell(2).setCellValue(600.0);
        row.createCell(3).setCellValue("6月");  // 字符串

        File tmpFile = File.createTempFile("poi-test-", ".xlsx");
        FileOutputStream fos = new FileOutputStream(tmpFile);
        wb.write(fos);
        fos.close();
        wb.close();

        FileInputStream fis = new FileInputStream(tmpFile);
        Workbook wb2 = new XSSFWorkbook(fis);
        Row r = wb2.getSheetAt(0).getRow(0);

        String v1 = getCellStr(r.getCell(0));
        String v2 = getCellStr(r.getCell(1));
        String v3 = getCellStr(r.getCell(2));
        String v4 = getCellStr(r.getCell(3));

        wb2.close();
        fis.close();
        tmpFile.delete();

        System.out.println("[getCellStr] 数字列读值:");
        System.out.println("  2.0  -> [" + v1 + "]");
        System.out.println("  1.5  -> [" + v2 + "]");
        System.out.println("  600  -> [" + v3 + "]");
        System.out.println("  6月  -> [" + v4 + "]");

        check("NUMERIC 2.0 无小数点", !v1.contains(".") && v1.equals("2"));
        check("NUMERIC 1.5 无小数点", !v2.contains(".") && v2.equals("1"));
        check("NUMERIC 600 无小数点", !v3.contains(".") && v3.equals("600"));
        check("STRING '6月' 保持原样", v4.equals("6月"));
    }

    // ===== 2. buildColMap: 列顺序不固定也能正确建立映射 =====
    static void testBuildColMap_VariableColumnOrder() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Test");

        // 场景1: 月份在最前
        Row h1 = sheet.createRow(0);
        h1.createCell(0).setCellValue("培训月份");
        h1.createCell(1).setCellValue("序号");
        h1.createCell(2).setCellValue("培训内容");
        h1.createCell(3).setCellValue("部门");

        // 场景2: 月份在最后
        Row h2 = sheet.createRow(1);
        h2.createCell(0).setCellValue("序号");
        h2.createCell(1).setCellValue("培训内容");
        h2.createCell(2).setCellValue("部门");
        h2.createCell(3).setCellValue("培训月份");

        Map<String, Integer> map1 = buildColMap(h1);
        Map<String, Integer> map2 = buildColMap(h2);
        wb.close();

        System.out.println("\n[buildColMap] 列顺序兼容:");
        System.out.println("  顺序1 映射: " + map1);
        System.out.println("  顺序2 映射: " + map2);

        check("场景1: 月份=0", map1.get("培训月份").equals(0));
        check("场景1: 序号=1", map1.get("序号").equals(1));
        check("场景2: 月份=3", map2.get("培训月份").equals(3));
        check("场景2: 序号=0", map2.get("序号").equals(0));
    }

    // ===== 3. parseMonth: 各种格式解析 =====
    static void testParseMonth_VariousFormats() {
        System.out.println("\n[parseMonth] 各种格式:");

        check("'2.0'  -> 2", parseMonth("2.0").equals(2));
        check("'2'    -> 2", parseMonth("2").equals(2));
        check("'6月'  -> 6", parseMonth("6月").equals(6));
        check("'12月' -> 12", parseMonth("12月").equals(12));
        check("'2.5小时' -> null", parseMonth("2.5小时") == null);
        check("'不定时' -> null", parseMonth("不定时") == null);
        check("'待定'  -> null", parseMonth("待定") == null);
        check("空串    -> null", parseMonth("") == null);
        check("null   -> null", parseMonth(null) == null);
    }

    // ===== 4. 读取实际 Excel 文件，打印内容 =====
    static void testReadActualExcelFile() throws Exception {
        String path = "C:/Users/Administrator/Desktop/trainarchive/trainarchive/培训计划2.xlsx";
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("\n[Excel文件] 文件不存在: " + path);
            return;
        }

        FileInputStream fis = new FileInputStream(f);
        Workbook wb = new XSSFWorkbook(fis);
        System.out.println("\n[Excel文件] " + path);
        System.out.println("  Sheet数量: " + wb.getNumberOfSheets());

        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            System.out.println("\n  === Sheet: " + sheetName + " ===");
            int lastRow = Math.min(sheet.getLastRowNum(), 25);
            for (int r = 0; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                System.out.printf("    Row%-2d: ", r);
                for (int c = 0; c < 12; c++) {
                    Cell cell = row.getCell(c);
                    String val = getCellStr(cell);
                    if (val.isEmpty()) val = "_";
                    String display = val.length() > 15 ? val.substring(0, 13) + ".." : val;
                    System.out.printf("%-17s", display);
                }
                System.out.println();
            }
        }

        wb.close();
        fis.close();
    }

    // ===== 辅助方法（复制自 BizTrainPlanController） =====

    private static String getCellStr(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue();
            case NUMERIC: return DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:  try { return String.valueOf((long) cell.getNumericCellValue()); } catch (Exception e) { return cell.getCellFormula(); }
            default:      return "";
        }
    }

    private static Map<String, Integer> buildColMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        if (headerRow == null) return map;
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            String header = getCellStr(cell).trim();
            if (!header.isEmpty() && !map.containsKey(header)) {
                map.put(header, Integer.valueOf(i));
            }
        }
        return map;
    }

    private static Integer parseMonth(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        if (text.contains("不定时") || text.contains("待定")) return null;
        // 排除带小数点的数字（如 "2.5小时" 是课时，不是月份）
        if (text.matches(".*\\d+\\.[0-9]*[^0-9\\s].*")) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(text);
        if (m.find()) return Integer.valueOf(Integer.parseInt(m.group()));
        return null;
    }

    private static void check(String desc, boolean result) {
        if (result) {
            System.out.println("  [PASS] " + desc);
            passed++;
        } else {
            System.out.println("  [FAIL] " + desc);
            failed++;
        }
    }
}
