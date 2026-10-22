package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainArchiveMapper;
import com.ruoyi.biz.domain.BizTrainArchive;
import com.ruoyi.biz.service.IBizTrainArchiveService;

/**
 * 个人培训档案明细Service业务层处理
 * 
 * @author admin
 * @date 2026-03-03
 */
@Service
public class BizTrainArchiveServiceImpl implements IBizTrainArchiveService 
{
    @Autowired
    private BizTrainArchiveMapper bizTrainArchiveMapper;

    /**
     * 查询个人培训档案明细
     * 
     * @param archiveId 个人培训档案明细主键
     * @return 个人培训档案明细
     */
    @Override
    public BizTrainArchive selectBizTrainArchiveByArchiveId(Long archiveId)
    {
        return bizTrainArchiveMapper.selectBizTrainArchiveByArchiveId(archiveId);
    }

    /**
     * 查询个人培训档案明细列表
     * 
     * @param bizTrainArchive 个人培训档案明细
     * @return 个人培训档案明细
     */
    @Override
    public List<BizTrainArchive> selectBizTrainArchiveList(BizTrainArchive bizTrainArchive)
    {
        return bizTrainArchiveMapper.selectBizTrainArchiveList(bizTrainArchive);
    }

    /**
     * 新增个人培训档案明细
     * 
     * @param bizTrainArchive 个人培训档案明细
     * @return 结果
     */
    @Override
    public int insertBizTrainArchive(BizTrainArchive bizTrainArchive)
    {
        bizTrainArchive.setCreateTime(DateUtils.getNowDate());
        return bizTrainArchiveMapper.insertBizTrainArchive(bizTrainArchive);
    }

    /**
     * 修改个人培训档案明细
     * 
     * @param bizTrainArchive 个人培训档案明细
     * @return 结果
     */
    @Override
    public int updateBizTrainArchive(BizTrainArchive bizTrainArchive)
    {
        bizTrainArchive.setUpdateTime(DateUtils.getNowDate());
        return bizTrainArchiveMapper.updateBizTrainArchive(bizTrainArchive);
    }

    /**
     * 批量删除个人培训档案明细
     * 
     * @param archiveIds 需要删除的个人培训档案明细主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainArchiveByArchiveIds(Long[] archiveIds)
    {
        return bizTrainArchiveMapper.deleteBizTrainArchiveByArchiveIds(archiveIds);
    }

    /**
     * 删除个人培训档案明细信息
     *
     * @param archiveId 个人培训档案明细主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainArchiveByArchiveId(Long archiveId)
    {
        return bizTrainArchiveMapper.deleteBizTrainArchiveByArchiveId(archiveId);
    }

    /**
     * 查询个人培训档案明细列表（带历史快照）
     * 根据培训日期动态关联sys_user_history获取当时的人员信息
     *
     * @param bizTrainArchive 个人培训档案明细
     * @return 个人培训档案明细集合
     */
    @Override
    public List<BizTrainArchive> selectArchiveListWithSnapshot(BizTrainArchive bizTrainArchive)
    {
        return bizTrainArchiveMapper.selectArchiveListWithSnapshot(bizTrainArchive);
    }
}
