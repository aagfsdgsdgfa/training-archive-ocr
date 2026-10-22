package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainLecturer;

/**
 * 培训讲师资质库Mapper接口
 * 
 * @author admin
 * @date 2026-03-04
 */
public interface BizTrainLecturerMapper 
{
    /**
     * 查询培训讲师资质库
     * 
     * @param lecturerId 培训讲师资质库主键
     * @return 培训讲师资质库
     */
    public BizTrainLecturer selectBizTrainLecturerByLecturerId(Long lecturerId);

    /**
     * 查询培训讲师资质库列表
     * 
     * @param bizTrainLecturer 培训讲师资质库
     * @return 培训讲师资质库集合
     */
    public List<BizTrainLecturer> selectBizTrainLecturerList(BizTrainLecturer bizTrainLecturer);

    /**
     * 新增培训讲师资质库
     * 
     * @param bizTrainLecturer 培训讲师资质库
     * @return 结果
     */
    public int insertBizTrainLecturer(BizTrainLecturer bizTrainLecturer);

    /**
     * 修改培训讲师资质库
     * 
     * @param bizTrainLecturer 培训讲师资质库
     * @return 结果
     */
    public int updateBizTrainLecturer(BizTrainLecturer bizTrainLecturer);

    /**
     * 删除培训讲师资质库
     * 
     * @param lecturerId 培训讲师资质库主键
     * @return 结果
     */
    public int deleteBizTrainLecturerByLecturerId(Long lecturerId);

    /**
     * 批量删除培训讲师资质库
     * 
     * @param lecturerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTrainLecturerByLecturerIds(Long[] lecturerIds);
}
