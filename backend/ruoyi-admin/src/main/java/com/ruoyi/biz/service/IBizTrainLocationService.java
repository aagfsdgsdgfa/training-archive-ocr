package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainLocation;

/**
 * 培训地点与会议室管理Service接口
 * 
 * @author admin
 * @date 2026-03-04
 */
public interface IBizTrainLocationService 
{
    /**
     * 查询培训地点与会议室管理
     * 
     * @param locationId 培训地点与会议室管理主键
     * @return 培训地点与会议室管理
     */
    public BizTrainLocation selectBizTrainLocationByLocationId(Long locationId);

    /**
     * 查询培训地点与会议室管理列表
     * 
     * @param bizTrainLocation 培训地点与会议室管理
     * @return 培训地点与会议室管理集合
     */
    public List<BizTrainLocation> selectBizTrainLocationList(BizTrainLocation bizTrainLocation);

    /**
     * 新增培训地点与会议室管理
     * 
     * @param bizTrainLocation 培训地点与会议室管理
     * @return 结果
     */
    public int insertBizTrainLocation(BizTrainLocation bizTrainLocation);

    /**
     * 修改培训地点与会议室管理
     * 
     * @param bizTrainLocation 培训地点与会议室管理
     * @return 结果
     */
    public int updateBizTrainLocation(BizTrainLocation bizTrainLocation);

    /**
     * 批量删除培训地点与会议室管理
     * 
     * @param locationIds 需要删除的培训地点与会议室管理主键集合
     * @return 结果
     */
    public int deleteBizTrainLocationByLocationIds(Long[] locationIds);

    /**
     * 删除培训地点与会议室管理信息
     * 
     * @param locationId 培训地点与会议室管理主键
     * @return 结果
     */
    public int deleteBizTrainLocationByLocationId(Long locationId);
}
