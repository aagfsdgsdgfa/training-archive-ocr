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
import com.ruoyi.biz.domain.BizTrainLocation;
import com.ruoyi.biz.service.IBizTrainLocationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 培训地点与会议室管理Controller
 * 
 * @author admin
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/biz/location")
public class BizTrainLocationController extends BaseController
{
    @Autowired
    private IBizTrainLocationService bizTrainLocationService;

    /**
     * 查询培训地点与会议室管理列表
     */
    @PreAuthorize("@ss.hasPermi('biz:location:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainLocation bizTrainLocation)
    {
        startPage();
        List<BizTrainLocation> list = bizTrainLocationService.selectBizTrainLocationList(bizTrainLocation);
        return getDataTable(list);
    }

    /**
     * 导出培训地点与会议室管理列表
     */
    @PreAuthorize("@ss.hasPermi('biz:location:export')")
    @Log(title = "培训地点与会议室管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainLocation bizTrainLocation)
    {
        List<BizTrainLocation> list = bizTrainLocationService.selectBizTrainLocationList(bizTrainLocation);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无地点数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainLocation> util = new ExcelUtil<BizTrainLocation>(BizTrainLocation.class);
        util.exportExcel(response, list, "培训地点与会议室管理数据");
    }

    /**
     * 获取培训地点与会议室管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:location:query')")
    @GetMapping(value = "/{locationId}")
    public AjaxResult getInfo(@PathVariable("locationId") Long locationId)
    {
        return success(bizTrainLocationService.selectBizTrainLocationByLocationId(locationId));
    }

    /**
     * 新增培训地点与会议室管理
     */
    @PreAuthorize("@ss.hasPermi('biz:location:add')")
    @Log(title = "培训地点与会议室管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainLocation bizTrainLocation)
    {
        return toAjax(bizTrainLocationService.insertBizTrainLocation(bizTrainLocation));
    }

    /**
     * 修改培训地点与会议室管理
     */
    @PreAuthorize("@ss.hasPermi('biz:location:edit')")
    @Log(title = "培训地点与会议室管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainLocation bizTrainLocation)
    {
        return toAjax(bizTrainLocationService.updateBizTrainLocation(bizTrainLocation));
    }

    @PreAuthorize("@ss.hasPermi('biz:location:remove')")
    @Log(title = "培训地点与会议室管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainLocation query) {
        List<BizTrainLocation> list = bizTrainLocationService.selectBizTrainLocationList(query);
        if (list.isEmpty()) { return error("没有符合条件的数据"); }
        Long[] ids = list.stream().map(BizTrainLocation::getLocationId).toArray(Long[]::new);
        return toAjax(bizTrainLocationService.deleteBizTrainLocationByLocationIds(ids));
    }

    /**
     * 删除培训地点与会议室管理
     */
    @PreAuthorize("@ss.hasPermi('biz:location:remove')")
    @Log(title = "培训地点与会议室管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{locationIds}")
    public AjaxResult remove(@PathVariable Long[] locationIds)
    {
        return toAjax(bizTrainLocationService.deleteBizTrainLocationByLocationIds(locationIds));
    }
}
