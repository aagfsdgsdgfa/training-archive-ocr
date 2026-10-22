package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainCourse;

/**
 * 标准课程资源库Service接口
 * 
 * @author admin
 * @date 2026-03-04
 */
public interface IBizTrainCourseService 
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
     * 批量删除标准课程资源库
     * 
     * @param courseIds 需要删除的标准课程资源库主键集合
     * @return 结果
     */
    public int deleteBizTrainCourseByCourseIds(Long[] courseIds);

    /**
     * 删除标准课程资源库信息
     * 
     * @param courseId 标准课程资源库主键
     * @return 结果
     */
    public int deleteBizTrainCourseByCourseId(Long courseId);
}
