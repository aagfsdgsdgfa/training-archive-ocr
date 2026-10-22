package com.ruoyi.biz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ruoyi.biz.domain.BizTrainCourse;
import com.ruoyi.biz.service.impl.BizTrainCourseServiceImpl;

/**
 * BizTrainCourseService 单元测试
 *
 * 测试范围：标准课程资源库的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainCourseServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainCourseMapper bizTrainCourseMapper;

    @InjectMocks
    private BizTrainCourseServiceImpl bizTrainCourseService;

    private BizTrainCourse testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new BizTrainCourse();
        testCourse.setCourseId(1L);
        testCourse.setCourseName("安全生产培训");
        testCourse.setProjectCategory("必修");
        testCourse.setDefaultHours(new java.math.BigDecimal("4"));
    }

    @Test
    @DisplayName("selectBizTrainCourseByCourseId: 课程存在时返回对象")
    void selectBizTrainCourseByCourseId_存在_返回对象() {
        when(bizTrainCourseMapper.selectBizTrainCourseByCourseId(1L)).thenReturn(testCourse);

        BizTrainCourse result = bizTrainCourseService.selectBizTrainCourseByCourseId(1L);

        assertNotNull(result);
        assertEquals("安全生产培训", result.getCourseName());
    }

    @Test
    @DisplayName("selectBizTrainCourseByCourseId: 课程不存在时返回null")
    void selectBizTrainCourseByCourseId_不存在_返回null() {
        when(bizTrainCourseMapper.selectBizTrainCourseByCourseId(999L)).thenReturn(null);

        BizTrainCourse result = bizTrainCourseService.selectBizTrainCourseByCourseId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("selectBizTrainCourseList: 返回课程列表")
    void selectBizTrainCourseList_返回列表() {
        List<BizTrainCourse> mockList = Arrays.asList(testCourse);
        when(bizTrainCourseMapper.selectBizTrainCourseList(any())).thenReturn(mockList);

        List<BizTrainCourse> result = bizTrainCourseService.selectBizTrainCourseList(new BizTrainCourse());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectBizTrainCourseList: 按课程名称筛选")
    void selectBizTrainCourseList_按名称筛选() {
        when(bizTrainCourseMapper.selectBizTrainCourseList(any())).thenReturn(Arrays.asList(testCourse));

        BizTrainCourse query = new BizTrainCourse();
        query.setCourseName("安全");
        List<BizTrainCourse> result = bizTrainCourseService.selectBizTrainCourseList(query);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("insertBizTrainCourse: 成功插入")
    void insertBizTrainCourse_成功() {
        when(bizTrainCourseMapper.insertBizTrainCourse(any())).thenReturn(1);

        BizTrainCourse newCourse = new BizTrainCourse();
        newCourse.setCourseName("新课程");
        int result = bizTrainCourseService.insertBizTrainCourse(newCourse);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainCourse: 成功更新")
    void updateBizTrainCourse_成功() {
        when(bizTrainCourseMapper.updateBizTrainCourse(any())).thenReturn(1);

        testCourse.setDefaultHours(new java.math.BigDecimal("8"));
        int result = bizTrainCourseService.updateBizTrainCourse(testCourse);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("deleteBizTrainCourseByCourseIds: 批量删除成功")
    void deleteBizTrainCourseByCourseIds_成功() {
        when(bizTrainCourseMapper.deleteBizTrainCourseByCourseIds(any())).thenReturn(3);

        int result = bizTrainCourseService.deleteBizTrainCourseByCourseIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainCourseByCourseId: 单个删除成功")
    void deleteBizTrainCourseByCourseId_成功() {
        when(bizTrainCourseMapper.deleteBizTrainCourseByCourseId(1L)).thenReturn(1);

        int result = bizTrainCourseService.deleteBizTrainCourseByCourseId(1L);

        assertEquals(1, result);
    }
}
