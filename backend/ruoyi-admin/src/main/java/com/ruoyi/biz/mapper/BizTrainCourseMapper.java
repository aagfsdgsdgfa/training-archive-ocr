package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainCourse;

/**
 * 标准课程资源库Mapper接口
 * 
 * @author admin
 * @date 2026-03-04
 */
public interface BizTrainCourseMapper 
{
    /**
     * 查询标准课程资源库
     * 
     * @param courseId 标准课程资源库主键
     * @return 标准课程资源库
     */
    public BizTrainCourse selectBizTrainCourseByCourseId(Long courseId);

    /**
     * 查询标准课程资源库列表
     * 
     * @param bizTrainCourse 标准课程资源库
     * @return 标准课程资源库集合
     */
    public List<BizTrainCourse> selectBizTrainCourseList(BizTrainCourse bizTrainCourse);

    /**
     * 新增标准课程资源库
     * 
     * @param bizTrainCourse 标准课程资源库
     * @return 结果
     */
    public int insertBizTrainCourse(BizTrainCourse bizTrainCourse);

    /**
     * 修改标准课程资源库
     * 
     * @param bizTrainCourse 标准课程资源库
     * @return 结果
     */
    public int updateBizTrainCourse(BizTrainCourse bizTrainCourse);

    /**
     * 删除标准课程资源库
     * 
     * @param courseId 标准课程资源库主键
     * @return 结果
     */
    public int deleteBizTrainCourseByCourseId(Long courseId);

    /**
     * 批量删除标准课程资源库
     * 
     * @param courseIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTrainCourseByCourseIds(Long[] courseIds);
}
