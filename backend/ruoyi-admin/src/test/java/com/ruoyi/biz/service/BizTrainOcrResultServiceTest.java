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

import com.ruoyi.biz.domain.BizTrainOcrResult;
import com.ruoyi.biz.domain.BizTrainOcrBatch;
import com.ruoyi.biz.service.impl.BizTrainOcrResultServiceImpl;
import com.ruoyi.common.exception.ServiceException;

/**
 * BizTrainOcrResultService 单元测试
 *
 * 测试范围：OCR识别结果的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainOcrResultServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainOcrResultMapper bizTrainOcrResultMapper;

    @Mock
    private com.ruoyi.biz.mapper.BizTrainOcrBatchMapper bizTrainOcrBatchMapper;

    @Mock
    private com.ruoyi.biz.mapper.BizTrainArchiveMapper bizTrainArchiveMapper;

    @InjectMocks
    private BizTrainOcrResultServiceImpl bizTrainOcrResultService;

    private BizTrainOcrResult testResult;
    private BizTrainOcrBatch testBatch;

    @BeforeEach
    void setUp() {
        testResult = new BizTrainOcrResult();
        testResult.setResultId(1L);
        testResult.setBatchId(100L);
        testResult.setParsedName("张三");
        testResult.setParsedDept("培训部");
        testResult.setStatus("0"); // 正常待确认

        testBatch = new BizTrainOcrBatch();
        testBatch.setBatchId(100L);
        testBatch.setOcrSubject("安全生产培训");
        testBatch.setOcrDate("2026-06-15");
    }

    @Test
    @DisplayName("selectBizTrainOcrResultByResultId: 结果存在时返回对象")
    void selectBizTrainOcrResultByResultId_存在_返回对象() {
        when(bizTrainOcrResultMapper.selectBizTrainOcrResultByResultId(1L)).thenReturn(testResult);

        BizTrainOcrResult result = bizTrainOcrResultService.selectBizTrainOcrResultByResultId(1L);

        assertNotNull(result);
        assertEquals("张三", result.getParsedName());
    }

    @Test
    @DisplayName("selectBizTrainOcrResultByResultId: 结果不存在时返回null")
    void selectBizTrainOcrResultByResultId_不存在_返回null() {
        when(bizTrainOcrResultMapper.selectBizTrainOcrResultByResultId(999L)).thenReturn(null);

        BizTrainOcrResult result = bizTrainOcrResultService.selectBizTrainOcrResultByResultId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("selectBizTrainOcrResultList: 返回结果列表")
    void selectBizTrainOcrResultList_返回列表() {
        List<BizTrainOcrResult> mockList = Arrays.asList(testResult);
        when(bizTrainOcrResultMapper.selectBizTrainOcrResultList(any())).thenReturn(mockList);

        List<BizTrainOcrResult> result = bizTrainOcrResultService.selectBizTrainOcrResultList(new BizTrainOcrResult());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectBizTrainOcrResultList: 按批次ID筛选")
    void selectBizTrainOcrResultList_按批次筛选() {
        when(bizTrainOcrResultMapper.selectBizTrainOcrResultList(any())).thenReturn(Arrays.asList(testResult));

        BizTrainOcrResult query = new BizTrainOcrResult();
        query.setBatchId(100L);
        List<BizTrainOcrResult> result = bizTrainOcrResultService.selectBizTrainOcrResultList(query);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getBatchId());
    }

    @Test
    @DisplayName("insertBizTrainOcrResult: 成功插入（无重复档案）")
    void insertBizTrainOcrResult_成功_无重复档案() {
        when(bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(100L)).thenReturn(testBatch);
        when(bizTrainArchiveMapper.selectBizTrainArchiveList(any())).thenReturn(Arrays.asList());
        when(bizTrainOcrResultMapper.insertBizTrainOcrResult(any())).thenReturn(1);

        BizTrainOcrResult newResult = new BizTrainOcrResult();
        newResult.setBatchId(100L);
        newResult.setParsedName("李四");
        int result = bizTrainOcrResultService.insertBizTrainOcrResult(newResult);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("insertBizTrainOcrResult: 有重复档案时抛出异常")
    void insertBizTrainOcrResult_有重复档案_抛异常() {
        when(bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(100L)).thenReturn(testBatch);

        // 存在相同培训档案
        com.ruoyi.biz.domain.BizTrainArchive existingArchive = new com.ruoyi.biz.domain.BizTrainArchive();
        when(bizTrainArchiveMapper.selectBizTrainArchiveList(any())).thenReturn(Arrays.asList(existingArchive));

        BizTrainOcrResult newResult = new BizTrainOcrResult();
        newResult.setBatchId(100L);
        newResult.setParsedName("张三");

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            bizTrainOcrResultService.insertBizTrainOcrResult(newResult);
        });

        assertTrue(ex.getMessage().contains("查重拦截"));
    }

    @Test
    @DisplayName("updateBizTrainOcrResult: 成功更新")
    void updateBizTrainOcrResult_成功() {
        when(bizTrainOcrResultMapper.updateBizTrainOcrResult(any())).thenReturn(1);

        testResult.setParsedName("张三(已校正)");
        int result = bizTrainOcrResultService.updateBizTrainOcrResult(testResult);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("deleteBizTrainOcrResultByResultIds: 批量删除成功")
    void deleteBizTrainOcrResultByResultIds_成功() {
        when(bizTrainOcrResultMapper.deleteBizTrainOcrResultByResultIds(any())).thenReturn(3);

        int result = bizTrainOcrResultService.deleteBizTrainOcrResultByResultIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainOcrResultByResultId: 单个删除成功")
    void deleteBizTrainOcrResultByResultId_成功() {
        when(bizTrainOcrResultMapper.deleteBizTrainOcrResultByResultId(1L)).thenReturn(1);

        int result = bizTrainOcrResultService.deleteBizTrainOcrResultByResultId(1L);

        assertEquals(1, result);
    }
}
