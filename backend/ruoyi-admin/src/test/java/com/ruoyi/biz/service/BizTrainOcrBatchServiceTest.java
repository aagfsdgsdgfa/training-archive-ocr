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

import com.ruoyi.biz.domain.BizTrainOcrBatch;
import com.ruoyi.biz.service.impl.BizTrainOcrBatchServiceImpl;

/**
 * BizTrainOcrBatchService 单元测试
 *
 * 测试范围：OCR扫描批次的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainOcrBatchServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainOcrBatchMapper bizTrainOcrBatchMapper;

    @InjectMocks
    private BizTrainOcrBatchServiceImpl bizTrainOcrBatchService;

    private BizTrainOcrBatch testBatch;

    @BeforeEach
    void setUp() {
        testBatch = new BizTrainOcrBatch();
        testBatch.setBatchId(1L);
        testBatch.setBatchNo("OCR20260304001");
        testBatch.setStatus("0"); // 处理中
        testBatch.setTotalPages(10);
    }

    @Test
    @DisplayName("selectBizTrainOcrBatchByBatchId: 批次存在时返回对象")
    void selectBizTrainOcrBatchByBatchId_存在_返回对象() {
        when(bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(1L)).thenReturn(testBatch);

        BizTrainOcrBatch result = bizTrainOcrBatchService.selectBizTrainOcrBatchByBatchId(1L);

        assertNotNull(result);
        assertEquals("OCR20260304001", result.getBatchNo());
    }

    @Test
    @DisplayName("selectBizTrainOcrBatchByBatchId: 批次不存在时返回null")
    void selectBizTrainOcrBatchByBatchId_不存在_返回null() {
        when(bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(999L)).thenReturn(null);

        BizTrainOcrBatch result = bizTrainOcrBatchService.selectBizTrainOcrBatchByBatchId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("selectBizTrainOcrBatchList: 返回批次列表")
    void selectBizTrainOcrBatchList_返回列表() {
        List<BizTrainOcrBatch> mockList = Arrays.asList(testBatch);
        when(bizTrainOcrBatchMapper.selectBizTrainOcrBatchList(any())).thenReturn(mockList);

        List<BizTrainOcrBatch> result = bizTrainOcrBatchService.selectBizTrainOcrBatchList(new BizTrainOcrBatch());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectBizTrainOcrBatchList: 按状态筛选")
    void selectBizTrainOcrBatchList_按状态筛选() {
        when(bizTrainOcrBatchMapper.selectBizTrainOcrBatchList(any())).thenReturn(Arrays.asList(testBatch));

        BizTrainOcrBatch query = new BizTrainOcrBatch();
        query.setStatus("0");
        List<BizTrainOcrBatch> result = bizTrainOcrBatchService.selectBizTrainOcrBatchList(query);

        assertEquals(1, result.size());
        assertEquals("0", result.get(0).getStatus());
    }

    @Test
    @DisplayName("insertBizTrainOcrBatch: 成功插入")
    void insertBizTrainOcrBatch_成功() {
        when(bizTrainOcrBatchMapper.insertBizTrainOcrBatch(any())).thenReturn(1);

        BizTrainOcrBatch newBatch = new BizTrainOcrBatch();
        newBatch.setBatchNo("OCR20260305001");
        int result = bizTrainOcrBatchService.insertBizTrainOcrBatch(newBatch);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainOcrBatch: 成功更新")
    void updateBizTrainOcrBatch_成功() {
        when(bizTrainOcrBatchMapper.updateBizTrainOcrBatch(any())).thenReturn(1);

        testBatch.setStatus("1"); // 已完成
        int result = bizTrainOcrBatchService.updateBizTrainOcrBatch(testBatch);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("deleteBizTrainOcrBatchByBatchIds: 批量删除成功")
    void deleteBizTrainOcrBatchByBatchIds_成功() {
        when(bizTrainOcrBatchMapper.deleteBizTrainOcrBatchByBatchIds(any())).thenReturn(3);

        int result = bizTrainOcrBatchService.deleteBizTrainOcrBatchByBatchIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainOcrBatchByBatchId: 单个删除成功")
    void deleteBizTrainOcrBatchByBatchId_成功() {
        when(bizTrainOcrBatchMapper.deleteBizTrainOcrBatchByBatchId(1L)).thenReturn(1);

        int result = bizTrainOcrBatchService.deleteBizTrainOcrBatchByBatchId(1L);

        assertEquals(1, result);
    }

    // ============================================================
    // isAbsentRemark 工具方法单测
    // 命中关键字:未参加/缺席/缺考/授课人 → 视为缺席(不计入实到)
    // ============================================================

    @Test
    @DisplayName("isAbsentRemark: 授课人 视为缺席")
    void isAbsentRemark_授课人_returnsTrue() {
        assertTrue(BizTrainOcrBatchServiceImpl.isAbsentRemark("授课人"));
    }

    @Test
    @DisplayName("isAbsentRemark: 未参加 视为缺席")
    void isAbsentRemark_未参加_returnsTrue() {
        assertTrue(BizTrainOcrBatchServiceImpl.isAbsentRemark("未参加"));
    }

    @Test
    @DisplayName("isAbsentRemark: 缺席 视为缺席")
    void isAbsentRemark_缺席_returnsTrue() {
        assertTrue(BizTrainOcrBatchServiceImpl.isAbsentRemark("缺席"));
    }

    @Test
    @DisplayName("isAbsentRemark: 缺考 视为缺席")
    void isAbsentRemark_缺考_returnsTrue() {
        assertTrue(BizTrainOcrBatchServiceImpl.isAbsentRemark("缺考"));
    }

    @Test
    @DisplayName("isAbsentRemark: null 视为非缺席")
    void isAbsentRemark_null_returnsFalse() {
        assertFalse(BizTrainOcrBatchServiceImpl.isAbsentRemark(null));
    }

    @Test
    @DisplayName("isAbsentRemark: 空字符串 视为非缺席")
    void isAbsentRemark_empty_returnsFalse() {
        assertFalse(BizTrainOcrBatchServiceImpl.isAbsentRemark(""));
    }

    @Test
    @DisplayName("isAbsentRemark: 普通文字 视为非缺席")
    void isAbsentRemark_普通文字_returnsFalse() {
        assertFalse(BizTrainOcrBatchServiceImpl.isAbsentRemark("优秀"));
    }

    @Test
    @DisplayName("isAbsentRemark: 包含关键字的混合文字 视为缺席")
    void isAbsentRemark_混合文字_returnsTrue() {
        assertTrue(BizTrainOcrBatchServiceImpl.isAbsentRemark("已签到/未参加"));
    }
}
