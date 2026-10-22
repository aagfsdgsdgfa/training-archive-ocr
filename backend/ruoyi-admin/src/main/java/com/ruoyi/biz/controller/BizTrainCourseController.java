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
import com.ruoyi.biz.domain.BizTrainCourse;
import com.ruoyi.biz.service.IBizTrainCourseService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 标准课程资源库Controller
 * 
 * @author admin
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/biz/course")
public class BizTrainCourseController extends BaseController
{
    @Autowired
    private IBizTrainCourseService bizTrainCourseService;

    /**
     * 查询标准课程资源库列表
     */
    @PreAuthorize("@ss.hasPermi('biz:course:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainCourse bizTrainCourse)
    {
        startPage();
        List<BizTrainCourse> list = bizTrainCourseService.selectBizTrainCourseList(bizTrainCourse);
        return getDataTable(list);
    }

    /**
     * 导出标准课程资源库列表
     */
    @PreAuthorize("@ss.hasPermi('biz:course:export')")
    @Log(title = "标准课程资源库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainCourse bizTrainCourse)
    {
        List<BizTrainCourse> list = bizTrainCourseService.selectBizTrainCourseList(bizTrainCourse);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无课程数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainCourse> util = new ExcelUtil<BizTrainCourse>(BizTrainCourse.class);
        util.exportExcel(response, list, "标准课程资源库数据");
    }

    /**
     * 获取标准课程资源库详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:course:query')")
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId)
    {
        return success(bizTrainCourseService.selectBizTrainCourseByCourseId(courseId));
    }

    /**
     * 新增标准课程资源库
     */
    @PreAuthorize("@ss.hasPermi('biz:course:add')")
    @Log(title = "标准课程资源库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainCourse bizTrainCourse)
    {
        return toAjax(bizTrainCourseService.insertBizTrainCourse(bizTrainCourse));
    }

    /**
     * 修改标准课程资源库
     */
    @PreAuthorize("@ss.hasPermi('biz:course:edit')")
    @Log(title = "标准课程资源库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainCourse bizTrainCourse)
    {
        return toAjax(bizTrainCourseService.updateBizTrainCourse(bizTrainCourse));
    }

    @PreAuthorize("@ss.hasPermi('biz:course:remove')")
    @Log(title = "标准课程资源库", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainCourse query) {
        List<BizTrainCourse> list = bizTrainCourseService.selectBizTrainCourseList(query);
        if (list.isEmpty()) { return error("没有符合条件的数据"); }
        Long[] ids = list.stream().map(BizTrainCourse::getCourseId).toArray(Long[]::new);
        return toAjax(bizTrainCourseService.deleteBizTrainCourseByCourseIds(ids));
    }

    /**
     * 删除标准课程资源库
     */
    @PreAuthorize("@ss.hasPermi('biz:course:remove')")
    @Log(title = "标准课程资源库", businessType = BusinessType.DELETE)
	@DeleteMapping("/{courseIds}")
    public AjaxResult remove(@PathVariable Long[] courseIds)
    {
        return toAjax(bizTrainCourseService.deleteBizTrainCourseByCourseIds(courseIds));
    }
}
