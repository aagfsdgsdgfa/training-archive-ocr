package com.ruoyi.biz.controller;

import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.biz.domain.BizTrainOcrResult;
import com.ruoyi.biz.domain.vo.OcrBatchUploadReq;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.BizTrainOcrBatch;
import com.ruoyi.biz.service.IBizTrainOcrBatchService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * OCR扫描批次主Controller
 * 
 * @author admin
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/biz/batch")
public class BizTrainOcrBatchController extends BaseController
{
    @Autowired
    private IBizTrainOcrBatchService bizTrainOcrBatchService;

    /**
     * 管理员校对完成，确认入库结算
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:edit')")
    @Log(title = "OCR批次结算入库", businessType = BusinessType.UPDATE)
    @PostMapping("/confirmAndGenerate/{batchId}")
    public AjaxResult confirmAndGenerate(@PathVariable("batchId") Long batchId, @RequestBody List<BizTrainOcrResult> editedList) {
        if (editedList == null || editedList.isEmpty()) {
            return AjaxResult.error("提交的数据不能为空");
        }
        bizTrainOcrBatchService.confirmAndGenerate(batchId, editedList);
        return AjaxResult.success("考勤与台账生成成功！");
    }

    /**
     * 基于源文件重新 OCR 提取元数据（用于历史批次补提应到/实到/培训日期）
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:edit')")
    @Log(title = "OCR批次-重新提取", businessType = BusinessType.UPDATE)
    @PostMapping("/reextract/{batchId}")
    public AjaxResult reextract(@PathVariable("batchId") Long batchId) {
        int updated = bizTrainOcrBatchService.reextractBatch(batchId);
        return updated > 0 ? AjaxResult.success("已重新提取元数据")
                           : AjaxResult.error("批次不存在或文件已失效");
    }

    /**
     * 接收高拍仪批量扫表上传
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:add')") // 根据你实际的权限标识调整
    @Log(title = "OCR批次扫表", businessType = BusinessType.INSERT)
    @PostMapping("/uploadBatch")
    public AjaxResult uploadBatch(@Validated @RequestBody OcrBatchUploadReq req) {
        Long batchId = bizTrainOcrBatchService.uploadAndCreateBatch(req);
        // 这里直接返回成功，不阻塞前端，真正的 OCR 放到后台异步执行
        return AjaxResult.success("扫描文件已接收，共 " + req.getBase64Images().size() + " 页，后台正在智能解析中...", batchId);
    }

    /**
     * 查询OCR扫描批次主列表
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainOcrBatch bizTrainOcrBatch)
    {
        startPage();
        List<BizTrainOcrBatch> list = bizTrainOcrBatchService.selectBizTrainOcrBatchList(bizTrainOcrBatch);
        return getDataTable(list);
    }

    /**
     * 导出OCR扫描批次主列表
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:export')")
    @Log(title = "OCR扫描批次主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainOcrBatch bizTrainOcrBatch)
    {
        List<BizTrainOcrBatch> list = bizTrainOcrBatchService.selectBizTrainOcrBatchList(bizTrainOcrBatch);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无批次数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainOcrBatch> util = new ExcelUtil<BizTrainOcrBatch>(BizTrainOcrBatch.class);
        util.exportExcel(response, list, "OCR扫描批次主数据");
    }

    /**
     * 获取OCR扫描批次主详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:query')")
    @GetMapping(value = "/{batchId}")
    public AjaxResult getInfo(@PathVariable("batchId") Long batchId)
    {
        return success(bizTrainOcrBatchService.selectBizTrainOcrBatchByBatchId(batchId));
    }

    /**
     * 新增OCR扫描批次主
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:add')")
    @Log(title = "OCR扫描批次主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainOcrBatch bizTrainOcrBatch)
    {
        return toAjax(bizTrainOcrBatchService.insertBizTrainOcrBatch(bizTrainOcrBatch));
    }

    /**
     * 修改OCR扫描批次主
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:edit')")
    @Log(title = "OCR扫描批次主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainOcrBatch bizTrainOcrBatch)
    {
        return toAjax(bizTrainOcrBatchService.updateBizTrainOcrBatch(bizTrainOcrBatch));
    }

    @PreAuthorize("@ss.hasPermi('biz:batch:remove')")
    @Log(title = "OCR扫描批次主", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainOcrBatch query) {
        List<BizTrainOcrBatch> list = bizTrainOcrBatchService.selectBizTrainOcrBatchList(query);
        if (list.isEmpty()) { return error("没有符合条件的数据"); }
        Long[] ids = list.stream().map(BizTrainOcrBatch::getBatchId).toArray(Long[]::new);
        return toAjax(bizTrainOcrBatchService.deleteBizTrainOcrBatchByBatchIds(ids));
    }

    /**
     * 删除OCR扫描批次主
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:remove')")
    @Log(title = "OCR扫描批次主", businessType = BusinessType.DELETE)
	@DeleteMapping("/{batchIds}")
    public AjaxResult remove(@PathVariable Long[] batchIds)
    {
        return toAjax(bizTrainOcrBatchService.deleteBizTrainOcrBatchByBatchIds(batchIds));
    }
}
