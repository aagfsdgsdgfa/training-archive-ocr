package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizDeviceMapper;
import com.ruoyi.biz.domain.BizDevice;
import com.ruoyi.biz.service.IBizDeviceService;

/**
 * 硬件设备与高拍仪管理Service业务层处理
 * 
 * @author 杨正和
 * @date 2026-03-04
 */
@Service
public class BizDeviceServiceImpl implements IBizDeviceService 
{
    @Autowired
    private BizDeviceMapper bizDeviceMapper;

    /**
     * 查询硬件设备与高拍仪管理
     * 
     * @param deviceId 硬件设备与高拍仪管理主键
     * @return 硬件设备与高拍仪管理
     */
    @Override
    public BizDevice selectBizDeviceByDeviceId(Long deviceId)
    {
        return bizDeviceMapper.selectBizDeviceByDeviceId(deviceId);
    }

    /**
     * 查询硬件设备与高拍仪管理列表
     * 
     * @param bizDevice 硬件设备与高拍仪管理
     * @return 硬件设备与高拍仪管理
     */
    @Override
    public List<BizDevice> selectBizDeviceList(BizDevice bizDevice)
    {
        return bizDeviceMapper.selectBizDeviceList(bizDevice);
    }

    /**
     * 新增硬件设备与高拍仪管理
     * 
     * @param bizDevice 硬件设备与高拍仪管理
     * @return 结果
     */
    @Override
    public int insertBizDevice(BizDevice bizDevice)
    {
        bizDevice.setCreateTime(DateUtils.getNowDate());
        return bizDeviceMapper.insertBizDevice(bizDevice);
    }

    /**
     * 修改硬件设备与高拍仪管理
     * 
     * @param bizDevice 硬件设备与高拍仪管理
     * @return 结果
     */
    @Override
    public int updateBizDevice(BizDevice bizDevice)
    {
        bizDevice.setUpdateTime(DateUtils.getNowDate());
        return bizDeviceMapper.updateBizDevice(bizDevice);
    }

    /**
     * 批量删除硬件设备与高拍仪管理
     * 
     * @param deviceIds 需要删除的硬件设备与高拍仪管理主键
     * @return 结果
     */
    @Override
    public int deleteBizDeviceByDeviceIds(Long[] deviceIds)
    {
        return bizDeviceMapper.deleteBizDeviceByDeviceIds(deviceIds);
    }

    /**
     * 删除硬件设备与高拍仪管理信息
     * 
     * @param deviceId 硬件设备与高拍仪管理主键
     * @return 结果
     */
    @Override
    public int deleteBizDeviceByDeviceId(Long deviceId)
    {
        return bizDeviceMapper.deleteBizDeviceByDeviceId(deviceId);
    }
}
