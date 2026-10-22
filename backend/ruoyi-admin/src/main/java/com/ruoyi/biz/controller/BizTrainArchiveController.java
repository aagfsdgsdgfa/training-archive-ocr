package com.ruoyi.biz.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.BizTrainArchive;
import com.ruoyi.biz.service.IBizTrainArchiveService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/biz/archive")
public class BizTrainArchiveController extends BaseController
{
    private static final int TEMPLATE_DATA_START_ROW = 5;
    private static final int TEMPLATE_TRAIN_CONTENT_START_COLUMN = 3;
    private static final int TEMPLATE_TRAIN_CONTENT_END_COLUMN = 4;

    @Autowired
    private IBizTrainArchiveService bizTrainArchiveService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询【我的】个人培训档案列表
     */
    @GetMapping("/myList")
    public TableDataInfo myArchiveList(BizTrainArchive bizTrainArchive) {
        // 开启分页
        startPage();
        Long currentUserId = SecurityUtils.getUserId();
        bizTrainArchive.setUserId(currentUserId);

        List<BizTrainArchive> list = bizTrainArchiveService.selectArchiveListWithSnapshot(bizTrainArchive);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('biz:archive:export')")
    @Log(title = "个人培训档案导出", businessType = BusinessType.EXPORT)
    @PostMapping("/exportTemplate")
    public void exportTemplate(HttpServletResponse response,
        @RequestParam("userId") Long userId,
        @RequestParam(value = "beginTrainDate", required = false) String beginTrainDate,
        @RequestParam(value = "endTrainDate", required = false) String endTrainDate) {
        try {
            SysUser user = userService.selectUserById(userId);
            if (user == null) {
                throw new ServiceException("未找到该员工信息");
            }
            String userName = user.getNickName() != null ? user.getNickName() : user.getUserName();
            String deptName = user.getDept() != null ? user.getDept().getDeptName() : "未分配部门";

            String sexStr = "未知";
            if ("0".equals(user.getSex())) sexStr = "男";
            else if ("1".equals(user.getSex())) sexStr = "女";

            String postNameStr = userService.selectUserPostGroup(user.getUserName());
            if (StringUtils.isEmpty(postNameStr)) {
                postNameStr = "未分配岗位";
            }

            String birthdayStr = user.getBirthday() != null ? DateUtils.parseDateToStr("yyyy-MM", user.getBirthday()) : "未填写";
            String joinDateStr = user.getJoinDate() != null ? DateUtils.parseDateToStr("yyyy-MM-dd", user.getJoinDate()) : "未填写";
            String eduStr = StringUtils.isNotEmpty(user.getEducation()) ? user.getEducation() : "未填写";

            BizTrainArchive query = new BizTrainArchive();
            query.setUserId(userId);
            query.setBeginTrainDate(beginTrainDate);
            query.setEndTrainDate(endTrainDate);
            // 使用历史快照查询，获取培训发生时的人员信息
            List<BizTrainArchive> allList = bizTrainArchiveService.selectArchiveListWithSnapshot(query);

            if (allList.isEmpty()) {
                response.setContentType("application/json;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
                PrintWriter writer = null;
                try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"该员工暂无培训记录，无需导出\"}"); writer.flush(); } catch (Exception e) {}
                return;
            }

            // 按 (年份, 当时的部门名) 分组，每个分组生成一张Sheet
            Map<String, List<BizTrainArchive>> groupMap = allList.stream()
                    .collect(Collectors.groupingBy(item -> extractYear(item.getTrainDate()) + "-" + StringUtils.defaultString(item.getDeptName(), "未知部门")));

            List<String> groupKeys = new ArrayList<>(groupMap.keySet());
            Collections.sort(groupKeys);

            ClassPathResource templateResource = new ClassPathResource("templates/archive_template.xlsx");
            byte[] dynamicTemplateBytes;

            try (InputStream is = templateResource.getInputStream();
                 XSSFWorkbook workbook = new XSSFWorkbook(is);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                for (int i = 1; i < groupKeys.size(); i++) {
                    workbook.cloneSheet(0);
                }

                for (int i = 0; i < groupKeys.size(); i++) {
                    workbook.setSheetName(i, groupKeys.get(i) + "年度");
                }

                workbook.write(bos);
                dynamicTemplateBytes = bos.toByteArray();
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(userName + "-个人培训档案", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            ByteArrayOutputStream exportBos = new ByteArrayOutputStream();
            try (ByteArrayInputStream bis = new ByteArrayInputStream(dynamicTemplateBytes);
                 ExcelWriter excelWriter = EasyExcel.write(exportBos).withTemplate(bis).build()) {

                FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();

                for (int i = 0; i < groupKeys.size(); i++) {
                    String groupKey = groupKeys.get(i);
                    List<BizTrainArchive> groupList = groupMap.get(groupKey);
                    WriteSheet writeSheet = EasyExcel.writerSheet(i).build();

                    // 解析分组key：格式为 "年份-部门名"
                    String[] parts = groupKey.split("-", 2);
                    String year = parts[0];
                    String snapshotDeptName = parts.length > 1 ? parts[1] : deptName;

                    Map<String, Object> map = new HashMap<>();
                    map.put("userName", userName);
                    map.put("deptName", snapshotDeptName);
                    map.put("year", year);
                    map.put("exportDate", DateUtils.getDate());
                    map.put("sex", sexStr);
                    map.put("postName", postNameStr);
                    map.put("birthday", birthdayStr);
                    map.put("education", eduStr);
                    map.put("joinDate", joinDateStr);

                    String joinYear = "0000";
                    if(user.getJoinDate() != null) joinYear = DateUtils.parseDateToStr("yyyy", user.getJoinDate());
                    map.put("reportNo", "DA-" + joinYear + "-" + String.format("%04d", userId));

                    List<Map<String, Object>> mapList = new ArrayList<>();
                    int index = 1;
                    for (BizTrainArchive item : groupList) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("index", index++);

                        String dateStr = "";
                        if(item.getTrainDate() != null) {
                            dateStr = item.getTrainDate().toString().split(" ")[0];
                        }
                        row.put("trainDate", dateStr);
                        row.put("trainDept", item.getTrainDept() != null ? item.getTrainDept() : "");
                        row.put("trainLocation", item.getTrainLocation() != null ? item.getTrainLocation() : "");
                        row.put("subject", item.getSubject() != null ? item.getSubject() : "");
                        row.put("trainContent", "");
                        row.put("signSeq", item.getSignSeq() != null ? item.getSignSeq() : "");

                        row.put("speaker", item.getSpeaker() != null ? item.getSpeaker() : "");
                        row.put("score", item.getScore() != null ? item.getScore() : "");
                        row.put("trainHours", item.getTrainHours() != null ? item.getTrainHours() : "");
                        row.put("jobChange", item.getJobChange() != null ? item.getJobChange() : "");
                        row.put("remark", item.getRemark() != null ? item.getRemark() : "");
                        mapList.add(row);
                    }

                    excelWriter.fill(map, writeSheet);
                    excelWriter.fill(mapList, fillConfig, writeSheet);
                }
            }

            try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(exportBos.toByteArray()))) {
                for (int i = 0; i < groupKeys.size(); i++) {
                    fillTrainContentColumn(workbook, workbook.getSheetAt(i), groupMap.get(groupKeys.get(i)));
                }
                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            logger.error("导出个人档案失败", e);
            throw new ServiceException("导出档案失败: " + e.getMessage());
        }
    }

    private void fillTrainContentColumn(XSSFWorkbook workbook, Sheet sheet, List<BizTrainArchive> yearList) {
        for (int i = 0; i < yearList.size(); i++) {
            int rowIndex = TEMPLATE_DATA_START_ROW + i;
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            // D列
            Cell leftCell = row.getCell(TEMPLATE_TRAIN_CONTENT_START_COLUMN);
            if (leftCell == null) {
                leftCell = row.createCell(TEMPLATE_TRAIN_CONTENT_START_COLUMN);
            }
            // E列
            Cell rightCell = row.getCell(TEMPLATE_TRAIN_CONTENT_END_COLUMN);
            if (rightCell == null) {
                rightCell = row.createCell(TEMPLATE_TRAIN_CONTENT_END_COLUMN);
            }

            CellStyle leftAlignStyle = workbook.createCellStyle();
            leftAlignStyle.cloneStyleFrom(leftCell.getCellStyle());
            leftAlignStyle.setAlignment(HorizontalAlignment.LEFT);
            leftAlignStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            String trainContent = StringUtils.defaultString(yearList.get(i).getTrainContent());
            removeMergedRegions(sheet, rowIndex, TEMPLATE_TRAIN_CONTENT_START_COLUMN, TEMPLATE_TRAIN_CONTENT_END_COLUMN);

            // D和E列都写入值，合并后只显示D列内容
            leftCell.setCellStyle(leftAlignStyle);
            leftCell.setCellValue(trainContent);
            rightCell.setCellStyle(leftAlignStyle);
            rightCell.setCellValue("");

            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, TEMPLATE_TRAIN_CONTENT_START_COLUMN, TEMPLATE_TRAIN_CONTENT_END_COLUMN));
        }
    }

    private void removeMergedRegions(Sheet sheet, int rowIndex, int startColumn, int endColumn) {
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            boolean sameRow = range.getFirstRow() == rowIndex && range.getLastRow() == rowIndex;
            boolean overlapColumn = range.getFirstColumn() <= endColumn && range.getLastColumn() >= startColumn;
            if (sameRow && overlapColumn) {
                sheet.removeMergedRegion(i);
            }
        }
    }

    private String extractYear(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) return "未知年份";
        Matcher m = Pattern.compile("\\d{4}").matcher(dateStr);
        return m.find() ? m.group() : "未知年份";
    }

    @PreAuthorize("@ss.hasPermi('biz:archive:list')")
    @DataScope(deptAlias = "a", userAlias = "a")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainArchive bizTrainArchive)
    {
        startPage();
        List<BizTrainArchive> list = bizTrainArchiveService.selectArchiveListWithSnapshot(bizTrainArchive);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('biz:archive:export')")
    @Log(title = "个人培训档案明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainArchive bizTrainArchive)
    {
        // 使用历史快照查询，获取培训发生时的人员信息
        List<BizTrainArchive> list = bizTrainArchiveService.selectArchiveListWithSnapshot(bizTrainArchive);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无档案数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainArchive> util = new ExcelUtil<BizTrainArchive>(BizTrainArchive.class);
        util.exportExcel(response, list, "个人培训档案明细数据");
    }

    @PreAuthorize("@ss.hasPermi('biz:archive:query')")
    @GetMapping(value = "/{archiveId}")
    public AjaxResult getInfo(@PathVariable("archiveId") Long archiveId)
    {
        return success(bizTrainArchiveService.selectBizTrainArchiveByArchiveId(archiveId));
    }

    @PreAuthorize("@ss.hasPermi('biz:archive:add')")
    @Log(title = "个人培训档案明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainArchive bizTrainArchive)
    {
        return toAjax(bizTrainArchiveService.insertBizTrainArchive(bizTrainArchive));
    }

    @PreAuthorize("@ss.hasPermi('biz:archive:edit')")
    @Log(title = "个人培训档案明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainArchive bizTrainArchive)
    {
        return toAjax(bizTrainArchiveService.updateBizTrainArchive(bizTrainArchive));
    }

    /**
     * 按条件批量删除所有匹配数据（跨页全选删除）
     */
    @PreAuthorize("@ss.hasPermi('biz:archive:remove')")
    @Log(title = "个人培训档案明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainArchive query) {
        List<BizTrainArchive> list = bizTrainArchiveService.selectArchiveListWithSnapshot(query);
        if (list.isEmpty()) {
            return error("没有符合条件的数据");
        }
        Long[] ids = list.stream().map(BizTrainArchive::getArchiveId).toArray(Long[]::new);
        return toAjax(bizTrainArchiveService.deleteBizTrainArchiveByArchiveIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('biz:archive:remove')")
    @Log(title = "个人培训档案明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{archiveIds}")
    public AjaxResult remove(@PathVariable Long[] archiveIds)
    {
        return toAjax(bizTrainArchiveService.deleteBizTrainArchiveByArchiveIds(archiveIds));
    }
}