package com.ruoyi.biz.controller;

import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.BizTrainOcrLog;
import com.ruoyi.biz.service.IBizTrainOcrLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * OCR防重与拦截记录Controller
 * 
 * @author admin
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/biz/log")
public class BizTrainOcrLogController extends BaseController
{
    @Autowired
    private IBizTrainOcrLogService bizTrainOcrLogService;

    /**
     * 查询OCR防重与拦截记录列表
     */
    @PreAuthorize("@ss.hasPermi('biz:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainOcrLog bizTrainOcrLog)
    {
        startPage();
        List<BizTrainOcrLog> list = bizTrainOcrLogService.selectBizTrainOcrLogList(bizTrainOcrLog);
        return getDataTable(list);
    }

    /**
     * 导出OCR防重与拦截记录列表
     */
    @PreAuthorize("@ss.hasPermi('biz:log:export')")
    @Log(title = "OCR防重与拦截记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainOcrLog bizTrainOcrLog)
    {
        List<BizTrainOcrLog> list = bizTrainOcrLogService.selectBizTrainOcrLogList(bizTrainOcrLog);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无日志数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainOcrLog> util = new ExcelUtil<BizTrainOcrLog>(BizTrainOcrLog.class);
        util.exportExcel(response, list, "OCR防重与拦截记录数据");
    }

    /**
     * 获取OCR防重与拦截记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:log:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(bizTrainOcrLogService.selectBizTrainOcrLogByLogId(logId));
    }

    /**
     * 新增OCR防重与拦截记录
     */
    @PreAuthorize("@ss.hasPermi('biz:log:add')")
    @Log(title = "OCR防重与拦截记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainOcrLog bizTrainOcrLog)
    {
        return toAjax(bizTrainOcrLogService.insertBizTrainOcrLog(bizTrainOcrLog));
    }

    /**
     * 修改OCR防重与拦截记录
     */
    @PreAuthorize("@ss.hasPermi('biz:log:edit')")
    @Log(title = "OCR防重与拦截记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainOcrLog bizTrainOcrLog)
    {
        return toAjax(bizTrainOcrLogService.updateBizTrainOcrLog(bizTrainOcrLog));
    }

    @PreAuthorize("@ss.hasPermi('biz:log:remove')")
    @Log(title = "OCR防重与拦截记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainOcrLog query) {
        List<BizTrainOcrLog> list = bizTrainOcrLogService.selectBizTrainOcrLogList(query);
        if (list.isEmpty()) { return error("没有符合条件的数据"); }
        Long[] ids = list.stream().map(BizTrainOcrLog::getLogId).toArray(Long[]::new);
        return toAjax(bizTrainOcrLogService.deleteBizTrainOcrLogByLogIds(ids));
    }

    /**
     * 删除OCR防重与拦截记录
     */
    @PreAuthorize("@ss.hasPermi('biz:log:remove')")
    @Log(title = "OCR防重与拦截记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds)
    {
        return toAjax(bizTrainOcrLogService.deleteBizTrainOcrLogByLogIds(logIds));
    }
}
