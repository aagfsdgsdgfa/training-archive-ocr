package com.ruoyi.biz.controller;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.BizTrainLedger;
import com.ruoyi.biz.service.IBizTrainLedgerService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.service.IBizTrainPlanService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.SysDept;

@RestController
@RequestMapping("/biz/ledger")
public class BizTrainLedgerController extends BaseController
{
    @Autowired
    private IBizTrainLedgerService bizTrainLedgerService;

    @Autowired
    private IBizTrainPlanService bizTrainPlanService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysDeptService deptService;

    @PreAuthorize("@ss.hasPermi('biz:ledger:export')")
    @Log(title = "培训台账总表导出", businessType = BusinessType.EXPORT)
    @PostMapping("/exportTemplate")
    public void exportTemplate(HttpServletResponse response, BizTrainLedger bizTrainLedger) {
        try {
            List<BizTrainLedger> list = bizTrainLedgerService.selectBizTrainLedgerList(bizTrainLedger);
            List<LedgerExportVo> exportList = new ArrayList<>();
            int index = 1;

            for (BizTrainLedger item : list) {
                LedgerExportVo vo = new LedgerExportVo();
                vo.setIndex(index++);
                vo.setHostDeptName(item.getHostDeptName());
                vo.setProjectCategory(item.getProjectCategory());
                vo.setSubject(item.getSubject());
                vo.setTrainDate(item.getTrainDate() != null ?  item.getTrainDate() : "");
                vo.setTrainHours(item.getTrainHours() != null ? item.getTrainHours().toString() : "");
                vo.setSpeaker(item.getSpeaker());
                vo.setLocationName(item.getLocationName());
                vo.setTargetCount(item.getTargetCount() != null ? item.getTargetCount() : 0);
                vo.setActualCount(item.getActualCount() != null ? item.getActualCount() : 0);
                vo.setPlanType(item.getPlanType());
                vo.setTrainLevel(item.getTrainLevel());
                vo.setRemark(item.getRemark());

                String finalAttendeeNames = item.getAttendeeNames();
                if (item.getPlanId() != null) {
                    BizTrainPlan plan = bizTrainPlanService.selectBizTrainPlanByPlanId(item.getPlanId());
                    if (plan != null && StringUtils.isNotEmpty(plan.getAudienceScope())) {
                        String scope = plan.getAudienceScope();
                        String ids = plan.getAudienceIds();
                        if ("0".equals(scope)) {
                            finalAttendeeNames = "全体员工";
                        } else if ("1".equals(scope) && StringUtils.isNotEmpty(ids)) {
                            List<String> deptNames = new ArrayList<>();
                            for (String id : ids.split(",")) {
                                if (StringUtils.isNotEmpty(id.trim())) {
                                    try {
                                        SysDept dept = deptService.selectDeptById(Long.valueOf(id.trim()));
                                        if (dept != null) deptNames.add(dept.getDeptName());
                                    } catch (Exception e) {}
                                }
                            }
                            finalAttendeeNames = String.join("、", deptNames);
                        } else if ("2".equals(scope) && StringUtils.isNotEmpty(ids)) {
                            List<String> userNames = new ArrayList<>();
                            for (String id : ids.split(",")) {
                                if (StringUtils.isNotEmpty(id.trim())) {
                                    try {
                                        SysUser user = userService.selectUserById(Long.valueOf(id.trim()));
                                        if (user != null) userNames.add(user.getNickName());
                                    } catch (Exception e) {}
                                }
                            }
                            finalAttendeeNames = String.join("、", userNames);
                        }
                    }
                }
                vo.setAttendeeNames(finalAttendeeNames);
                exportList.add(vo);
            }

            if (exportList.isEmpty()) {
                response.setContentType("application/json;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
                PrintWriter writer = null;
                try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无台账数据可导出\"}"); writer.flush(); } catch (Exception e) {}
                return;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("reportNo", "TZ-" + DateUtils.dateTimeNow("yyyyMMdd") + "-" + (int)(Math.random()*100));

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("年度培训登记台账", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            ClassPathResource templateResource = new ClassPathResource("templates/ledger_template.xlsx");
            InputStream templateInputStream = templateResource.getInputStream();

            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).withTemplate(templateInputStream).build();
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();

            excelWriter.fill(exportList, fillConfig, writeSheet);
            excelWriter.fill(map, writeSheet);

            excelWriter.finish();
            templateInputStream.close();

        } catch (Exception e) {
            logger.error("导出台账模板失败", e);
            throw new ServiceException("导出台账失败: " + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('biz:ledger:list')")
    @DataScope(deptAlias = "l", userAlias = "l")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainLedger bizTrainLedger)
    {
        startPage();
        List<BizTrainLedger> list = bizTrainLedgerService.selectBizTrainLedgerList(bizTrainLedger);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('biz:ledger:export')")
    @Log(title = "培训台账总", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainLedger bizTrainLedger)
    {
        List<BizTrainLedger> list = bizTrainLedgerService.selectBizTrainLedgerList(bizTrainLedger);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无台账数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainLedger> util = new ExcelUtil<BizTrainLedger>(BizTrainLedger.class);
        util.exportExcel(response, list, "培训台账总数据");
    }

    @PreAuthorize("@ss.hasPermi('biz:ledger:query')")
    @GetMapping(value = "/{ledgerId}")
    public AjaxResult getInfo(@PathVariable("ledgerId") Long ledgerId)
    {
        return success(bizTrainLedgerService.selectBizTrainLedgerByLedgerId(ledgerId));
    }

    @PreAuthorize("@ss.hasPermi('biz:ledger:add')")
    @Log(title = "培训台账总", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainLedger bizTrainLedger)
    {
        int rows = bizTrainLedgerService.insertBizTrainLedger(bizTrainLedger);
        if (rows > 0)
        {
            AjaxResult ajax = AjaxResult.success("归档成功，流水号：" + bizTrainLedger.getSerialNo());
            ajax.put("serialNo", bizTrainLedger.getSerialNo());
            return ajax;
        }
        return AjaxResult.error();
    }

    @PreAuthorize("@ss.hasPermi('biz:ledger:edit')")
    @Log(title = "培训台账总", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainLedger bizTrainLedger)
    {
        return toAjax(bizTrainLedgerService.updateBizTrainLedger(bizTrainLedger));
    }

    /**
     * 按条件批量删除所有匹配数据（跨页全选删除）
     */
    @PreAuthorize("@ss.hasPermi('biz:ledger:remove')")
    @Log(title = "培训台账总", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainLedger query) {
        List<BizTrainLedger> list = bizTrainLedgerService.selectBizTrainLedgerList(query);
        if (list.isEmpty()) {
            return error("没有符合条件的数据");
        }
        Long[] ids = list.stream().map(BizTrainLedger::getLedgerId).toArray(Long[]::new);
        return toAjax(bizTrainLedgerService.deleteBizTrainLedgerByLedgerIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('biz:ledger:remove')")
    @Log(title = "培训台账总", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ledgerIds}")
    public AjaxResult remove(@PathVariable Long[] ledgerIds)
    {
        return toAjax(bizTrainLedgerService.deleteBizTrainLedgerByLedgerIds(ledgerIds));
    }

    public static class LedgerExportVo {
        private Integer index;
        private String hostDeptName;
        private String projectCategory;
        private String subject;
        private String trainDate;
        private String trainHours;
        private String speaker;
        private String locationName;
        private String attendeeNames;
        private Integer targetCount;
        private Integer actualCount;
        private String planType;
        private String trainLevel;
        private String remark;

        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }
        public String getHostDeptName() { return hostDeptName; }
        public void setHostDeptName(String hostDeptName) { this.hostDeptName = hostDeptName; }
        public String getProjectCategory() { return projectCategory; }
        public void setProjectCategory(String projectCategory) { this.projectCategory = projectCategory; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getTrainDate() { return trainDate; }
        public void setTrainDate(String trainDate) { this.trainDate = trainDate; }
        public String getTrainHours() { return trainHours; }
        public void setTrainHours(String trainHours) { this.trainHours = trainHours; }
        public String getSpeaker() { return speaker; }
        public void setSpeaker(String speaker) { this.speaker = speaker; }
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getAttendeeNames() { return attendeeNames; }
        public void setAttendeeNames(String attendeeNames) { this.attendeeNames = attendeeNames; }
        public Integer getTargetCount() { return targetCount; }
        public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }
        public Integer getActualCount() { return actualCount; }
        public void setActualCount(Integer actualCount) { this.actualCount = actualCount; }
        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        public String getTrainLevel() { return trainLevel; }
        public void setTrainLevel(String trainLevel) { this.trainLevel = trainLevel; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}