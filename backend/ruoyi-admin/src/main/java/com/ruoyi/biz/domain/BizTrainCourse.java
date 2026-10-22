package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 标准课程资源库对象 biz_train_course
 * 
 * @author admin
 * @date 2026-03-04
 */
public class BizTrainCourse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 课程ID */
    private Long courseId;

    /** 标准课程名称 */
    @Excel(name = "标准课程名称")
    private String courseName;

    /** 项目分类（如：GMP文件、新员工入职） */
    @Excel(name = "项目分类", readConverterExp = "如=：GMP文件、新员工入职")
    private String projectCategory;

    /** 默认培训学时 */
    @Excel(name = "默认培训学时")
    private BigDecimal defaultHours;

    /** 课程大纲与内容介绍 */
    @Excel(name = "课程大纲与内容介绍")
    private String courseSyllabus;

    /** 培训课件附件（PPT、PDF等存储路径） */
    @Excel(name = "培训课件附件", readConverterExp = "P=PT、PDF等存储路径")
    private String coursewareUrl;

    /** 状态（0启用 1停用） */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setCourseName(String courseName) 
    {
        this.courseName = courseName;
    }

    public String getCourseName() 
    {
        return courseName;
    }

    public void setProjectCategory(String projectCategory) 
    {
        this.projectCategory = projectCategory;
    }

    public String getProjectCategory() 
    {
        return projectCategory;
    }

    public void setDefaultHours(BigDecimal defaultHours) 
    {
        this.defaultHours = defaultHours;
    }

    public BigDecimal getDefaultHours() 
    {
        return defaultHours;
    }

    public void setCourseSyllabus(String courseSyllabus) 
    {
        this.courseSyllabus = courseSyllabus;
    }

    public String getCourseSyllabus() 
    {
        return courseSyllabus;
    }

    public void setCoursewareUrl(String coursewareUrl) 
    {
        this.coursewareUrl = coursewareUrl;
    }

    public String getCoursewareUrl() 
    {
        return coursewareUrl;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("courseId", getCourseId())
            .append("courseName", getCourseName())
            .append("projectCategory", getProjectCategory())
            .append("defaultHours", getDefaultHours())
            .append("courseSyllabus", getCourseSyllabus())
            .append("coursewareUrl", getCoursewareUrl())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
