package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainArchive;

/**
 * 个人培训档案明细Service接口
 * 
 * @author admin
 * @date 2026-03-03
 */
public interface IBizTrainArchiveService 
{
    /**
     * 查询个人培训档案明细
     * 
     * @param archiveId 个人培训档案明细主键
     * @return 个人培训档案明细
     */
    public BizTrainArchive selectBizTrainArchiveByArchiveId(Long archiveId);

    /**
     * 查询个人培训档案明细列表
     * 
     * @param bizTrainArchive 个人培训档案明细
     * @return 个人培训档案明细集合
     */
    public List<BizTrainArchive> selectBizTrainArchiveList(BizTrainArchive bizTrainArchive);

    /**
     * 新增个人培训档案明细
     * 
     * @param bizTrainArchive 个人培训档案明细
     * @return 结果
     */
    public int insertBizTrainArchive(BizTrainArchive bizTrainArchive);

    /**
     * 修改个人培训档案明细
     * 
     * @param bizTrainArchive 个人培训档案明细
     * @return 结果
     */
    public int updateBizTrainArchive(BizTrainArchive bizTrainArchive);

    /**
     * 批量删除个人培训档案明细
     * 
     * @param archiveIds 需要删除的个人培训档案明细主键集合
     * @return 结果
     */
    public int deleteBizTrainArchiveByArchiveIds(Long[] archiveIds);

    /**
     * 删除个人培训档案明细信息
     *
     * @param archiveId 个人培训档案明细主键
     * @return 结果
     */
    public int deleteBizTrainArchiveByArchiveId(Long archiveId);

    /**
     * 查询个人培训档案明细列表（带历史快照）
     * 根据培训日期动态关联sys_user_history获取当时的人员信息
     *
     * @param bizTrainArchive 个人培训档案明细
     * @return 个人培训档案明细集合
     */
    public List<BizTrainArchive> selectArchiveListWithSnapshot(BizTrainArchive bizTrainArchive);
}
