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
import com.ruoyi.biz.domain.BizDevice;
import com.ruoyi.biz.service.IBizDeviceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 硬件设备与高拍仪管理Controller
 * 
 * @author 杨正和
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/biz/device")
public class BizDeviceController extends BaseController
{
    @Autowired
    private IBizDeviceService bizDeviceService;

    /**
     * 查询硬件设备与高拍仪管理列表
     */
    @PreAuthorize("@ss.hasPermi('biz:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizDevice bizDevice)
    {
        startPage();
        List<BizDevice> list = bizDeviceService.selectBizDeviceList(bizDevice);
        return getDataTable(list);
    }

    /**
     * 导出硬件设备与高拍仪管理列表
     */
    @PreAuthorize("@ss.hasPermi('biz:device:export')")
    @Log(title = "硬件设备与高拍仪管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizDevice bizDevice)
    {
        List<BizDevice> list = bizDeviceService.selectBizDeviceList(bizDevice);
        if (list == null || list.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            try { response.setStatus(HttpServletResponse.SC_OK); } catch (Exception e) {}
            PrintWriter writer = null;
            try { writer = response.getWriter(); writer.write("{\"code\":500,\"msg\":\"暂无设备数据可导出\"}"); writer.flush(); } catch (Exception e) {}
            return;
        }
        ExcelUtil<BizDevice> util = new ExcelUtil<BizDevice>(BizDevice.class);
        util.exportExcel(response, list, "硬件设备与高拍仪管理数据");
    }

    /**
     * 获取硬件设备与高拍仪管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:device:query')")
    @GetMapping(value = "/{deviceId}")
    public AjaxResult getInfo(@PathVariable("deviceId") Long deviceId)
    {
        return success(bizDeviceService.selectBizDeviceByDeviceId(deviceId));
    }

    /**
     * 新增硬件设备与高拍仪管理
     */
    @PreAuthorize("@ss.hasPermi('biz:device:add')")
    @Log(title = "设备信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizDevice bizDevice)
    {
        BizDevice query = new BizDevice();
        query.setDeviceSn(bizDevice.getDeviceSn());
        List<BizDevice> list = bizDeviceService.selectBizDeviceList(query);

        if (list != null && !list.isEmpty()) {
            return error("新增设备失败，设备编号/序列号 '" + bizDevice.getDeviceSn() + "' 已存在！");
        }

        bizDevice.setCreateBy(getUsername());
        return toAjax(bizDeviceService.insertBizDevice(bizDevice));
    }

    /**
     * 修改硬件设备与高拍仪管理
     */
    @PreAuthorize("@ss.hasPermi('biz:device:edit')")
    @Log(title = "硬件设备与高拍仪管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizDevice bizDevice)
    {
        return toAjax(bizDeviceService.updateBizDevice(bizDevice));
    }

    @PreAuthorize("@ss.hasPermi('biz:device:remove')")
    @Log(title = "硬件设备与高拍仪管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAll")
    public AjaxResult deleteAll(BizDevice query) {
        List<BizDevice> list = bizDeviceService.selectBizDeviceList(query);
        if (list.isEmpty()) { return error("没有符合条件的数据"); }
        Long[] ids = list.stream().map(BizDevice::getDeviceId).toArray(Long[]::new);
        return toAjax(bizDeviceService.deleteBizDeviceByDeviceIds(ids));
    }

    /**
     * 删除硬件设备与高拍仪管理
     */
    @PreAuthorize("@ss.hasPermi('biz:device:remove')")
    @Log(title = "硬件设备与高拍仪管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable Long[] deviceIds)
    {
        return toAjax(bizDeviceService.deleteBizDeviceByDeviceIds(deviceIds));
    }
}
