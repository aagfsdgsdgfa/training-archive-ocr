package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainCourseMapper;
import com.ruoyi.biz.domain.BizTrainCourse;
import com.ruoyi.biz.service.IBizTrainCourseService;

/**
 * 标准课程资源库Service业务层处理
 * 
 * @author admin
 * @date 2026-03-04
 */
@Service
public class BizTrainCourseServiceImpl implements IBizTrainCourseService 
{
    @Autowired
    private BizTrainCourseMapper bizTrainCourseMapper;

    /**
     * 查询标准课程资源库
     * 
     * @param courseId 标准课程资源库主键
     * @return 标准课程资源库
     */
    @Override
    public BizTrainCourse selectBizTrainCourseByCourseId(Long courseId)
    {
        return bizTrainCourseMapper.selectBizTrainCourseByCourseId(courseId);
    }

    /**
     * 查询标准课程资源库列表
     * 
     * @param bizTrainCourse 标准课程资源库
     * @return 标准课程资源库
     */
    @Override
    public List<BizTrainCourse> selectBizTrainCourseList(BizTrainCourse bizTrainCourse)
    {
        return bizTrainCourseMapper.selectBizTrainCourseList(bizTrainCourse);
    }

    /**
     * 新增标准课程资源库
     * 
     * @param bizTrainCourse 标准课程资源库
     * @return 结果
     */
    @Override
    public int insertBizTrainCourse(BizTrainCourse bizTrainCourse)
    {
        bizTrainCourse.setCreateTime(DateUtils.getNowDate());
        return bizTrainCourseMapper.insertBizTrainCourse(bizTrainCourse);
    }

    /**
     * 修改标准课程资源库
     * 
     * @param bizTrainCourse 标准课程资源库
     * @return 结果
     */
    @Override
    public int updateBizTrainCourse(BizTrainCourse bizTrainCourse)
    {
        bizTrainCourse.setUpdateTime(DateUtils.getNowDate());
        return bizTrainCourseMapper.updateBizTrainCourse(bizTrainCourse);
    }

    /**
     * 批量删除标准课程资源库
     * 
     * @param courseIds 需要删除的标准课程资源库主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainCourseByCourseIds(Long[] courseIds)
    {
        return bizTrainCourseMapper.deleteBizTrainCourseByCourseIds(courseIds);
    }

    /**
     * 删除标准课程资源库信息
     * 
     * @param courseId 标准课程资源库主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainCourseByCourseId(Long courseId)
    {
        return bizTrainCourseMapper.deleteBizTrainCourseByCourseId(courseId);
    }
}
