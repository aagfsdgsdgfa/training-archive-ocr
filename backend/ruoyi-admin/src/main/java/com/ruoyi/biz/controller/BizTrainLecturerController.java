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
import com.ruoyi.biz.domain.BizTrainLecturer;
import com.ruoyi.biz.service.IBizTrainLecturerService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 培训讲师资质库Controller
 * 
 * @author admin
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/biz/lecturer")
public class BizTrainLecturerController extends BaseController
{
    @Autowired
    private IBizTrainLecturerService bizTrainLecturerService;

    /**
     * 查询培训讲师资质库列表
     */
    @PreAuthorize("@ss.hasPermi('biz:lecturer:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainLecturer bizTrainLecturer)
    {
        startPage();
        List<BizTrainLecturer> list = bizTrainLecturerService.selectBizTrainLecturerList(bizTrainLecturer);
        return getDataTable(list);
    }

    /**
     * 导出培训讲师资质库列表
     */
    @PreAuthorize("@ss.hasPermi('biz:lecturer:export')")
    @Log(title = "培训讲师资质库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainLecturer bizTrainLecturer)
    {
        List<BizTrainLecturer> list = bizTrainLecturerService.selectBizTrainLecturerList(bizTrainLecturer);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无讲师数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainLecturer> util = new ExcelUtil<BizTrainLecturer>(BizTrainLecturer.class);
        util.exportExcel(response, list, "培训讲师资质库数据");
    }

    /**
     * 获取培训讲师资质库详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:lecturer:query')")
    @GetMapping(value = "/{lecturerId}")
    public AjaxResult getInfo(@PathVariable("lecturerId") Long lecturerId)
    {
        return success(bizTrainLecturerService.selectBizTrainLecturerByLecturerId(lecturerId));
    }

    /**
     * 新增培训讲师资质库
     */
    @PreAuthorize("@ss.hasPermi('biz:lecturer:add')")
    @Log(title = "培训讲师资质库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainLecturer bizTrainLecturer)
    {
        return toAjax(bizTrainLecturerService.insertBizTrainLecturer(bizTrainLecturer));
    }

    /**
     * 修改培训讲师资质库
     */
    @PreAuthorize("@ss.hasPermi('biz:lecturer:edit')")
    @Log(title = "培训讲师资质库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainLecturer bizTrainLecturer)
    {
        return toAjax(bizTrainLecturerService.updateBizTrainLecturer(bizTrainLecturer));
    }

    @PreAuthorize("@ss.hasPermi('biz:lecturer:remove')")
    @Log(title = "培训讲师资质库", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainLecturer query) {
        List<BizTrainLecturer> list = bizTrainLecturerService.selectBizTrainLecturerList(query);
        if (list.isEmpty()) { return error("没有符合条件的数据"); }
        Long[] ids = list.stream().map(BizTrainLecturer::getLecturerId).toArray(Long[]::new);
        return toAjax(bizTrainLecturerService.deleteBizTrainLecturerByLecturerIds(ids));
    }

    /**
     * 删除培训讲师资质库
     */
    @PreAuthorize("@ss.hasPermi('biz:lecturer:remove')")
    @Log(title = "培训讲师资质库", businessType = BusinessType.DELETE)
	@DeleteMapping("/{lecturerIds}")
    public AjaxResult remove(@PathVariable Long[] lecturerIds)
    {
        return toAjax(bizTrainLecturerService.deleteBizTrainLecturerByLecturerIds(lecturerIds));
    }
}
