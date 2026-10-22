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

import com.ruoyi.biz.domain.BizTrainAttendance;
import com.ruoyi.biz.service.impl.BizTrainAttendanceServiceImpl;

/**
 * BizTrainAttendanceService 单元测试
 *
 * 测试范围：考勤记录的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainAttendanceServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainAttendanceMapper bizTrainAttendanceMapper;

    @InjectMocks
    private BizTrainAttendanceServiceImpl bizTrainAttendanceService;

    private BizTrainAttendance testAttendance;

    @BeforeEach
    void setUp() {
        testAttendance = new BizTrainAttendance();
        testAttendance.setRecordId(1L);
        testAttendance.setSessionId(100L);
        testAttendance.setUserId(200L);
        testAttendance.setSignStatus("0"); // 正常
        testAttendance.setSignTime(new java.util.Date());
        testAttendance.setAppealStatus("0"); // 无申诉
    }

    // ===== selectBizTrainAttendanceByRecordId =====

    @Test
    @DisplayName("selectBizTrainAttendanceByRecordId: 记录存在时返回对象")
    void selectBizTrainAttendanceByRecordId_存在_返回对象() {
        when(bizTrainAttendanceMapper.selectBizTrainAttendanceByRecordId(1L)).thenReturn(testAttendance);

        BizTrainAttendance result = bizTrainAttendanceService.selectBizTrainAttendanceByRecordId(1L);

        assertNotNull(result);
        assertEquals("0", result.getSignStatus());
        verify(bizTrainAttendanceMapper, times(1)).selectBizTrainAttendanceByRecordId(1L);
    }

    @Test
    @DisplayName("selectBizTrainAttendanceByRecordId: 记录不存在时返回null")
    void selectBizTrainAttendanceByRecordId_不存在_返回null() {
        when(bizTrainAttendanceMapper.selectBizTrainAttendanceByRecordId(999L)).thenReturn(null);

        BizTrainAttendance result = bizTrainAttendanceService.selectBizTrainAttendanceByRecordId(999L);

        assertNull(result);
    }

    // ===== selectBizTrainAttendanceList =====

    @Test
    @DisplayName("selectBizTrainAttendanceList: 返回所有考勤列表")
    void selectBizTrainAttendanceList_返回列表() {
        BizTrainAttendance att2 = new BizTrainAttendance();
        att2.setRecordId(2L);
        att2.setSignStatus("1"); // 迟到

        List<BizTrainAttendance> mockList = Arrays.asList(testAttendance, att2);
        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any(BizTrainAttendance.class))).thenReturn(mockList);

        List<BizTrainAttendance> result = bizTrainAttendanceService.selectBizTrainAttendanceList(new BizTrainAttendance());

        assertEquals(2, result.size());
        verify(bizTrainAttendanceMapper, times(1)).selectBizTrainAttendanceList(any());
    }

    @Test
    @DisplayName("selectBizTrainAttendanceList: 按sessionId筛选")
    void selectBizTrainAttendanceList_按sessionId筛选() {
        List<BizTrainAttendance> mockList = Arrays.asList(testAttendance);
        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any(BizTrainAttendance.class))).thenReturn(mockList);

        BizTrainAttendance query = new BizTrainAttendance();
        query.setSessionId(100L);
        List<BizTrainAttendance> result = bizTrainAttendanceService.selectBizTrainAttendanceList(query);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getSessionId());
    }

    @Test
    @DisplayName("selectBizTrainAttendanceList: 无数据时返回空列表")
    void selectBizTrainAttendanceList_空列表() {
        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any())).thenReturn(Arrays.asList());

        List<BizTrainAttendance> result = bizTrainAttendanceService.selectBizTrainAttendanceList(new BizTrainAttendance());

        assertTrue(result.isEmpty());
    }

    // ===== insertBizTrainAttendance =====

    @Test
    @DisplayName("insertBizTrainAttendance: 成功插入")
    void insertBizTrainAttendance_成功() {
        when(bizTrainAttendanceMapper.insertBizTrainAttendance(any())).thenReturn(1);

        BizTrainAttendance newAtt = new BizTrainAttendance();
        newAtt.setSessionId(100L);
        newAtt.setUserId(200L);
        newAtt.setSignStatus("0");

        int result = bizTrainAttendanceService.insertBizTrainAttendance(newAtt);

        assertEquals(1, result);
        assertNotNull(newAtt.getCreateTime());
        verify(bizTrainAttendanceMapper, times(1)).insertBizTrainAttendance(any());
    }

    @Test
    @DisplayName("insertBizTrainAttendance: 插入失败返回0")
    void insertBizTrainAttendance_失败_返回0() {
        when(bizTrainAttendanceMapper.insertBizTrainAttendance(any())).thenReturn(0);

        BizTrainAttendance newAtt = new BizTrainAttendance();
        int result = bizTrainAttendanceService.insertBizTrainAttendance(newAtt);

        assertEquals(0, result);
    }

    // ===== updateBizTrainAttendance =====

    @Test
    @DisplayName("updateBizTrainAttendance: 成功更新")
    void updateBizTrainAttendance_成功() {
        when(bizTrainAttendanceMapper.updateBizTrainAttendance(any())).thenReturn(1);

        testAttendance.setSignStatus("1"); // 改为迟到
        int result = bizTrainAttendanceService.updateBizTrainAttendance(testAttendance);

        assertEquals(1, result);
        assertNotNull(testAttendance.getUpdateTime());
        verify(bizTrainAttendanceMapper, times(1)).updateBizTrainAttendance(any());
    }

    @Test
    @DisplayName("updateBizTrainAttendance: 申诉通过更新为正常")
    void updateBizTrainAttendance_申诉通过() {
        when(bizTrainAttendanceMapper.updateBizTrainAttendance(any())).thenReturn(1);

        // 原来是迟到状态
        testAttendance.setSignStatus("1");
        // 申诉后变为正常
        testAttendance.setSignStatus("0");
        testAttendance.setRemark("管理员审核同意补卡");

        int result = bizTrainAttendanceService.updateBizTrainAttendance(testAttendance);

        assertEquals(1, result);
        assertEquals("0", testAttendance.getSignStatus());
    }

    // ===== deleteBizTrainAttendanceByRecordIds =====

    @Test
    @DisplayName("deleteBizTrainAttendanceByRecordIds: 批量删除成功")
    void deleteBizTrainAttendanceByRecordIds_成功() {
        when(bizTrainAttendanceMapper.deleteBizTrainAttendanceByRecordIds(any())).thenReturn(3);

        int result = bizTrainAttendanceService.deleteBizTrainAttendanceByRecordIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
        verify(bizTrainAttendanceMapper, times(1)).deleteBizTrainAttendanceByRecordIds(any());
    }

    @Test
    @DisplayName("deleteBizTrainAttendanceByRecordIds: 空数组返回0")
    void deleteBizTrainAttendanceByRecordIds_空数组() {
        when(bizTrainAttendanceMapper.deleteBizTrainAttendanceByRecordIds(new Long[]{})).thenReturn(0);

        int result = bizTrainAttendanceService.deleteBizTrainAttendanceByRecordIds(new Long[]{});

        assertEquals(0, result);
    }

    // ===== deleteBizTrainAttendanceByRecordId =====

    @Test
    @DisplayName("deleteBizTrainAttendanceByRecordId: 单个删除成功")
    void deleteBizTrainAttendanceByRecordId_成功() {
        when(bizTrainAttendanceMapper.deleteBizTrainAttendanceByRecordId(1L)).thenReturn(1);

        int result = bizTrainAttendanceService.deleteBizTrainAttendanceByRecordId(1L);

        assertEquals(1, result);
        verify(bizTrainAttendanceMapper, times(1)).deleteBizTrainAttendanceByRecordId(1L);
    }
}
