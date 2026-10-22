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
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.BizTrainSession;
import com.ruoyi.biz.service.IBizTrainSessionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 培训执行场次Controller
 * 
 * @author admin
 * @date 2026-03-03
 */
@RestController
@RequestMapping("/biz/session")
public class BizTrainSessionController extends BaseController
{
    @Autowired
    private IBizTrainSessionService bizTrainSessionService;

    /**
     * 一键归档结算培训场次，生成台账与档案
     */
    @PreAuthorize("@ss.hasPermi('biz:session:archive')")
    @Log(title = "培训执行场次归档", businessType = BusinessType.UPDATE)
    @PostMapping("/archive/{sessionId}")
    public AjaxResult archive(@PathVariable Long sessionId)
    {
        try {
            // getUsername() 是 BaseController 自带的，获取当前操作人
            bizTrainSessionService.archiveSession(sessionId, getUsername());
            return success("场次归档成功，已自动生成培训台账与个人档案！");
        } catch (Exception e) {
            return error("归档失败：" + e.getMessage());
        }
    }

    /**
     * 查询培训执行场次列表
     */
    @PreAuthorize("@ss.hasPermi('biz:session:list')")
    @DataScope(deptAlias = "s", userAlias = "s")
    @GetMapping("/list")
    public TableDataInfo list(BizTrainSession bizTrainSession)
    {
        startPage();
        List<BizTrainSession> list = bizTrainSessionService.selectBizTrainSessionList(bizTrainSession);
        return getDataTable(list);
    }

    /**
     * 导出培训执行场次列表
     */
    @PreAuthorize("@ss.hasPermi('biz:session:export')")
    @Log(title = "培训执行场次", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTrainSession bizTrainSession)
    {
        List<BizTrainSession> list = bizTrainSessionService.selectBizTrainSessionList(bizTrainSession);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无场次数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizTrainSession> util = new ExcelUtil<BizTrainSession>(BizTrainSession.class);
        util.exportExcel(response, list, "培训执行场次数据");
    }

    /**
     * 获取培训执行场次详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:session:query')")
    @GetMapping(value = "/{sessionId}")
    public AjaxResult getInfo(@PathVariable("sessionId") Long sessionId)
    {
        return success(bizTrainSessionService.selectBizTrainSessionBySessionId(sessionId));
    }

    /**
     * 新增培训执行场次
     */
    @PreAuthorize("@ss.hasPermi('biz:session:add')")
    @Log(title = "培训执行场次", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTrainSession bizTrainSession)
    {
        if (bizTrainSessionService.checkLocationConflict(bizTrainSession) > 0) {
            return error("新增失败：该培训地点在所选时间段内已被占用，请修改时间或更换地点！");
        }
        return toAjax(bizTrainSessionService.insertBizTrainSession(bizTrainSession));
    }

    /**
     * 修改培训执行场次
     */
    @PreAuthorize("@ss.hasPermi('biz:session:edit')")
    @Log(title = "培训执行场次", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTrainSession bizTrainSession)
    {
        if (bizTrainSessionService.checkLocationConflict(bizTrainSession) > 0) {
            return error("修改失败：该培训地点在所选时间段内已被占用，请修改时间或更换地点！");
        }
        return toAjax(bizTrainSessionService.updateBizTrainSession(bizTrainSession));
    }

    /**
     * 按条件批量删除所有匹配数据（跨页全选删除）
     */
    @PreAuthorize("@ss.hasPermi('biz:session:remove')")
    @Log(title = "培训执行场次", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizTrainSession query) {
        List<BizTrainSession> list = bizTrainSessionService.selectBizTrainSessionList(query);
        if (list.isEmpty()) {
            return error("没有符合条件的数据");
        }
        Long[] ids = list.stream().map(BizTrainSession::getSessionId).toArray(Long[]::new);
        return toAjax(bizTrainSessionService.deleteBizTrainSessionBySessionIds(ids));
    }

    /**
     * 删除培训执行场次
     */
    @PreAuthorize("@ss.hasPermi('biz:session:remove')")
    @Log(title = "培训执行场次", businessType = BusinessType.DELETE)
	@DeleteMapping("/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds)
    {
        return toAjax(bizTrainSessionService.deleteBizTrainSessionBySessionIds(sessionIds));
    }
}
