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
import com.ruoyi.biz.domain.BizTrainOcrResult;
import com.ruoyi.biz.service.IBizTrainOcrResultService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * OCR识别结果暂存明细Controller
 * 
 * @author admin
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/biz/result")
public class BizTrainOcrResultController extends BaseController
{
    @Autowired
    private IBizTrainOcrResultService bizTrainOcrResultService;

    /**
     * 根据批次ID获取校对明细列表
     */
    @PreAuthorize("@ss.hasPermi('biz:batch:query')")
    @GetMapping("/listByBatch/{batchId}")
    public AjaxResult listByBatch(@PathVariable("batchId") Long batchId) {
        BizTrainOcrResult query = new BizTrainOcrResult();
        query.setBatchId(batchId);
        query.setStatus("0"); // 只查状态为0(正常)的数据
        List<BizTrainOcrResult> list = bizTrainOcrResultService.selectBizTrainOcrResultList(query);
        return AjaxResult.success(list);
    }
    /**
     * 查询OCR识别结果暂存明细列表
     */
    @PreAuthorize("@ss.hasPermi('biz:result:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainOcrResult bizTrainOcrResult)
    {
        startPage();
        List<BizTrainOcrResult> list = bizTrainOcrResultService.selectBizTrainOcrResultList(bizTrainOcrResult);
        return getDataTable(list);
    }

    /**
     * 导出OCR识别结果暂存明细列表
     */
    @PreAuthorize("@ss.hasPermi('biz:result:export')")
    @Log(title = "OCR识别结果暂存明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainOcrResult bizTrainOcrResult)
    {
        List<BizTrainOcrResult> list = bizTrainOcrResultService.selectBizTrainOcrResultList(bizTrainOcrResult);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无识别结果数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainOcrResult> util = new ExcelUtil<BizTrainOcrResult>(BizTrainOcrResult.class);
        util.exportExcel(response, list, "OCR识别结果暂存明细数据");
    }

    /**
     * 获取OCR识别结果暂存明细详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:result:query')")
    @GetMapping(value = "/{resultId}")
    public AjaxResult getInfo(@PathVariable("resultId") Long resultId)
    {
        return success(bizTrainOcrResultService.selectBizTrainOcrResultByResultId(resultId));
    }

    /**
     * 新增OCR识别结果暂存明细
     */
    @PreAuthorize("@ss.hasPermi('biz:result:add')")
    @Log(title = "OCR识别结果暂存明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainOcrResult bizTrainOcrResult)
    {
        return toAjax(bizTrainOcrResultService.insertBizTrainOcrResult(bizTrainOcrResult));
    }

    /**
     * 修改OCR识别结果暂存明细
     */
    @PreAuthorize("@ss.hasPermi('biz:result:edit')")
    @Log(title = "OCR识别结果暂存明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainOcrResult bizTrainOcrResult)
    {
        return toAjax(bizTrainOcrResultService.updateBizTrainOcrResult(bizTrainOcrResult));
    }

    @PreAuthorize("@ss.hasPermi('biz:result:remove')")
    @Log(title = "OCR识别结果暂存明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainOcrResult query) {
        List<BizTrainOcrResult> list = bizTrainOcrResultService.selectBizTrainOcrResultList(query);
        if (list.isEmpty()) { return error("没有符合条件的数据"); }
        Long[] ids = list.stream().map(BizTrainOcrResult::getResultId).toArray(Long[]::new);
        return toAjax(bizTrainOcrResultService.deleteBizTrainOcrResultByResultIds(ids));
    }

    /**
     * 删除OCR识别结果暂存明细
     */
    @PreAuthorize("@ss.hasPermi('biz:result:remove')")
    @Log(title = "OCR识别结果暂存明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{resultIds}")
    public AjaxResult remove(@PathVariable Long[] resultIds)
    {
        return toAjax(bizTrainOcrResultService.deleteBizTrainOcrResultByResultIds(resultIds));
    }
}
