package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainLecturerMapper;
import com.ruoyi.biz.domain.BizTrainLecturer;
import com.ruoyi.biz.service.IBizTrainLecturerService;

/**
 * 培训讲师资质库Service业务层处理
 * 
 * @author admin
 * @date 2026-03-04
 */
@Service
public class BizTrainLecturerServiceImpl implements IBizTrainLecturerService 
{
    @Autowired
    private BizTrainLecturerMapper bizTrainLecturerMapper;

    /**
     * 查询培训讲师资质库
     * 
     * @param lecturerId 培训讲师资质库主键
     * @return 培训讲师资质库
     */
    @Override
    public BizTrainLecturer selectBizTrainLecturerByLecturerId(Long lecturerId)
    {
        return bizTrainLecturerMapper.selectBizTrainLecturerByLecturerId(lecturerId);
    }

    /**
     * 查询培训讲师资质库列表
     * 
     * @param bizTrainLecturer 培训讲师资质库
     * @return 培训讲师资质库
     */
    @Override
    public List<BizTrainLecturer> selectBizTrainLecturerList(BizTrainLecturer bizTrainLecturer)
    {
        return bizTrainLecturerMapper.selectBizTrainLecturerList(bizTrainLecturer);
    }

    /**
     * 新增培训讲师资质库
     * 
     * @param bizTrainLecturer 培训讲师资质库
     * @return 结果
     */
    @Override
    public int insertBizTrainLecturer(BizTrainLecturer bizTrainLecturer)
    {
        bizTrainLecturer.setCreateTime(DateUtils.getNowDate());
        return bizTrainLecturerMapper.insertBizTrainLecturer(bizTrainLecturer);
    }

    /**
     * 修改培训讲师资质库
     * 
     * @param bizTrainLecturer 培训讲师资质库
     * @return 结果
     */
    @Override
    public int updateBizTrainLecturer(BizTrainLecturer bizTrainLecturer)
    {
        bizTrainLecturer.setUpdateTime(DateUtils.getNowDate());
        return bizTrainLecturerMapper.updateBizTrainLecturer(bizTrainLecturer);
    }

    /**
     * 批量删除培训讲师资质库
     * 
     * @param lecturerIds 需要删除的培训讲师资质库主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainLecturerByLecturerIds(Long[] lecturerIds)
    {
        return bizTrainLecturerMapper.deleteBizTrainLecturerByLecturerIds(lecturerIds);
    }

    /**
     * 删除培训讲师资质库信息
     * 
     * @param lecturerId 培训讲师资质库主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainLecturerByLecturerId(Long lecturerId)
    {
        return bizTrainLecturerMapper.deleteBizTrainLecturerByLecturerId(lecturerId);
    }
}
