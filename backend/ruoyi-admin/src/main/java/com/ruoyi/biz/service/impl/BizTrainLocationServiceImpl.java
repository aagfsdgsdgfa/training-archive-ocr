package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainLocationMapper;
import com.ruoyi.biz.domain.BizTrainLocation;
import com.ruoyi.biz.service.IBizTrainLocationService;

/**
 * 培训地点与会议室管理Service业务层处理
 * 
 * @author admin
 * @date 2026-03-04
 */
@Service
public class BizTrainLocationServiceImpl implements IBizTrainLocationService 
{
    @Autowired
    private BizTrainLocationMapper bizTrainLocationMapper;

    /**
     * 查询培训地点与会议室管理
     * 
     * @param locationId 培训地点与会议室管理主键
     * @return 培训地点与会议室管理
     */
    @Override
    public BizTrainLocation selectBizTrainLocationByLocationId(Long locationId)
    {
        return bizTrainLocationMapper.selectBizTrainLocationByLocationId(locationId);
    }

    /**
     * 查询培训地点与会议室管理列表
     * 
     * @param bizTrainLocation 培训地点与会议室管理
     * @return 培训地点与会议室管理
     */
    @Override
    public List<BizTrainLocation> selectBizTrainLocationList(BizTrainLocation bizTrainLocation)
    {
        return bizTrainLocationMapper.selectBizTrainLocationList(bizTrainLocation);
    }

    /**
     * 新增培训地点与会议室管理
     * 
     * @param bizTrainLocation 培训地点与会议室管理
     * @return 结果
     */
    @Override
    public int insertBizTrainLocation(BizTrainLocation bizTrainLocation)
    {
        bizTrainLocation.setCreateTime(DateUtils.getNowDate());
        return bizTrainLocationMapper.insertBizTrainLocation(bizTrainLocation);
    }

    /**
     * 修改培训地点与会议室管理
     * 
     * @param bizTrainLocation 培训地点与会议室管理
     * @return 结果
     */
    @Override
    public int updateBizTrainLocation(BizTrainLocation bizTrainLocation)
    {
        bizTrainLocation.setUpdateTime(DateUtils.getNowDate());
        return bizTrainLocationMapper.updateBizTrainLocation(bizTrainLocation);
    }

    /**
     * 批量删除培训地点与会议室管理
     * 
     * @param locationIds 需要删除的培训地点与会议室管理主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainLocationByLocationIds(Long[] locationIds)
    {
        return bizTrainLocationMapper.deleteBizTrainLocationByLocationIds(locationIds);
    }

    /**
     * 删除培训地点与会议室管理信息
     * 
     * @param locationId 培训地点与会议室管理主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainLocationByLocationId(Long locationId)
    {
        return bizTrainLocationMapper.deleteBizTrainLocationByLocationId(locationId);
    }
}
