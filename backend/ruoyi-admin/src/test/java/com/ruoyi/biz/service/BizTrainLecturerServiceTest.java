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

import com.ruoyi.biz.domain.BizTrainLecturer;
import com.ruoyi.biz.service.impl.BizTrainLecturerServiceImpl;

/**
 * BizTrainLecturerService 单元测试
 *
 * 测试范围：培训讲师资质库的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainLecturerServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainLecturerMapper bizTrainLecturerMapper;

    @InjectMocks
    private BizTrainLecturerServiceImpl bizTrainLecturerService;

    private BizTrainLecturer testLecturer;

    @BeforeEach
    void setUp() {
        testLecturer = new BizTrainLecturer();
        testLecturer.setLecturerId(1L);
        testLecturer.setLecturerName("张教授");
        testLecturer.setExpertiseArea("安全管理");
        testLecturer.setCompanyName("中食药信息网");
    }

    @Test
    @DisplayName("selectBizTrainLecturerByLecturerId: 讲师存在时返回对象")
    void selectBizTrainLecturerByLecturerId_存在_返回对象() {
        when(bizTrainLecturerMapper.selectBizTrainLecturerByLecturerId(1L)).thenReturn(testLecturer);

        BizTrainLecturer result = bizTrainLecturerService.selectBizTrainLecturerByLecturerId(1L);

        assertNotNull(result);
        assertEquals("张教授", result.getLecturerName());
    }

    @Test
    @DisplayName("selectBizTrainLecturerByLecturerId: 讲师不存在时返回null")
    void selectBizTrainLecturerByLecturerId_不存在_返回null() {
        when(bizTrainLecturerMapper.selectBizTrainLecturerByLecturerId(999L)).thenReturn(null);

        BizTrainLecturer result = bizTrainLecturerService.selectBizTrainLecturerByLecturerId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("selectBizTrainLecturerList: 返回讲师列表")
    void selectBizTrainLecturerList_返回列表() {
        List<BizTrainLecturer> mockList = Arrays.asList(testLecturer);
        when(bizTrainLecturerMapper.selectBizTrainLecturerList(any())).thenReturn(mockList);

        List<BizTrainLecturer> result = bizTrainLecturerService.selectBizTrainLecturerList(new BizTrainLecturer());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("insertBizTrainLecturer: 成功插入")
    void insertBizTrainLecturer_成功() {
        when(bizTrainLecturerMapper.insertBizTrainLecturer(any())).thenReturn(1);

        BizTrainLecturer newLecturer = new BizTrainLecturer();
        newLecturer.setLecturerName("新讲师");
        int result = bizTrainLecturerService.insertBizTrainLecturer(newLecturer);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainLecturer: 成功更新")
    void updateBizTrainLecturer_成功() {
        when(bizTrainLecturerMapper.updateBizTrainLecturer(any())).thenReturn(1);

        testLecturer.setExpertiseArea("安全管理、应急处理");
        int result = bizTrainLecturerService.updateBizTrainLecturer(testLecturer);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("deleteBizTrainLecturerByLecturerIds: 批量删除成功")
    void deleteBizTrainLecturerByLecturerIds_成功() {
        when(bizTrainLecturerMapper.deleteBizTrainLecturerByLecturerIds(any())).thenReturn(3);

        int result = bizTrainLecturerService.deleteBizTrainLecturerByLecturerIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainLecturerByLecturerId: 单个删除成功")
    void deleteBizTrainLecturerByLecturerId_成功() {
        when(bizTrainLecturerMapper.deleteBizTrainLecturerByLecturerId(1L)).thenReturn(1);

        int result = bizTrainLecturerService.deleteBizTrainLecturerByLecturerId(1L);

        assertEquals(1, result);
    }
}
