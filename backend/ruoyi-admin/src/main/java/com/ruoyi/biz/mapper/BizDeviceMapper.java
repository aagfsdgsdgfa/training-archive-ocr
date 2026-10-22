package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizDevice;

/**
 * 硬件设备与高拍仪管理Mapper接口
 * 
 * @author 杨正和
 * @date 2026-03-04
 */
public interface BizDeviceMapper 
{
    /**
     * 查询硬件设备与高拍仪管理
     * 
     * @param deviceId 硬件设备与高拍仪管理主键
     * @return 硬件设备与高拍仪管理
     */
    public BizDevice selectBizDeviceByDeviceId(Long deviceId);

    /**
     * 查询硬件设备与高拍仪管理列表
     * 
     * @param bizDevice 硬件设备与高拍仪管理
     * @return 硬件设备与高拍仪管理集合
     */
    public List<BizDevice> selectBizDeviceList(BizDevice bizDevice);

    /**
     * 新增硬件设备与高拍仪管理
     * 
     * @param bizDevice 硬件设备与高拍仪管理
     * @return 结果
     */
    public int insertBizDevice(BizDevice bizDevice);

    /**
     * 修改硬件设备与高拍仪管理
     * 
     * @param bizDevice 硬件设备与高拍仪管理
     * @return 结果
     */
    public int updateBizDevice(BizDevice bizDevice);

    /**
     * 删除硬件设备与高拍仪管理
     * 
     * @param deviceId 硬件设备与高拍仪管理主键
     * @return 结果
     */
    public int deleteBizDeviceByDeviceId(Long deviceId);

    /**
     * 批量删除硬件设备与高拍仪管理
     * 
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizDeviceByDeviceIds(Long[] deviceIds);
}
