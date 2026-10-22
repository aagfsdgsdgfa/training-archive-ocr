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

import com.ruoyi.biz.domain.BizTrainOcrLog;
import com.ruoyi.biz.service.impl.BizTrainOcrLogServiceImpl;

/**
 * BizTrainOcrLogService 单元测试
 *
 * 测试范围：OCR防重与拦截记录的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainOcrLogServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainOcrLogMapper bizTrainOcrLogMapper;

    @InjectMocks
    private BizTrainOcrLogServiceImpl bizTrainOcrLogService;

    private BizTrainOcrLog testLog;

    @BeforeEach
    void setUp() {
        testLog = new BizTrainOcrLog();
        testLog.setLogId(1L);
        testLog.setBatchId(100L);
        testLog.setFileMd5("abc123");
        testLog.setFileUrl("/uploads/image.jpg");
        testLog.setStatus("1"); // 正常入库解析通过
    }

    @Test
    @DisplayName("selectBizTrainOcrLogByLogId: 日志存在时返回对象")
    void selectBizTrainOcrLogByLogId_存在_返回对象() {
        when(bizTrainOcrLogMapper.selectBizTrainOcrLogByLogId(1L)).thenReturn(testLog);

        BizTrainOcrLog result = bizTrainOcrLogService.selectBizTrainOcrLogByLogId(1L);

        assertNotNull(result);
        assertEquals("abc123", result.getFileMd5());
    }

    @Test
    @DisplayName("selectBizTrainOcrLogByLogId: 日志不存在时返回null")
    void selectBizTrainOcrLogByLogId_不存在_返回null() {
        when(bizTrainOcrLogMapper.selectBizTrainOcrLogByLogId(999L)).thenReturn(null);

        BizTrainOcrLog result = bizTrainOcrLogService.selectBizTrainOcrLogByLogId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("selectBizTrainOcrLogList: 返回日志列表")
    void selectBizTrainOcrLogList_返回列表() {
        List<BizTrainOcrLog> mockList = Arrays.asList(testLog);
        when(bizTrainOcrLogMapper.selectBizTrainOcrLogList(any())).thenReturn(mockList);

        List<BizTrainOcrLog> result = bizTrainOcrLogService.selectBizTrainOcrLogList(new BizTrainOcrLog());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectBizTrainOcrLogList: 按批次ID筛选")
    void selectBizTrainOcrLogList_按批次筛选() {
        when(bizTrainOcrLogMapper.selectBizTrainOcrLogList(any())).thenReturn(Arrays.asList(testLog));

        BizTrainOcrLog query = new BizTrainOcrLog();
        query.setBatchId(100L);
        List<BizTrainOcrLog> result = bizTrainOcrLogService.selectBizTrainOcrLogList(query);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("insertBizTrainOcrLog: 成功插入")
    void insertBizTrainOcrLog_成功() {
        when(bizTrainOcrLogMapper.insertBizTrainOcrLog(any())).thenReturn(1);

        BizTrainOcrLog newLog = new BizTrainOcrLog();
        newLog.setBatchId(100L);
        newLog.setFileMd5("def456");
        int result = bizTrainOcrLogService.insertBizTrainOcrLog(newLog);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainOcrLog: 成功更新")
    void updateBizTrainOcrLog_成功() {
        when(bizTrainOcrLogMapper.updateBizTrainOcrLog(any())).thenReturn(1);

        testLog.setStatus("0"); // 拦截阻断重复
        int result = bizTrainOcrLogService.updateBizTrainOcrLog(testLog);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("deleteBizTrainOcrLogByLogIds: 批量删除成功")
    void deleteBizTrainOcrLogByLogIds_成功() {
        when(bizTrainOcrLogMapper.deleteBizTrainOcrLogByLogIds(any())).thenReturn(3);

        int result = bizTrainOcrLogService.deleteBizTrainOcrLogByLogIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainOcrLogByLogId: 单个删除成功")
    void deleteBizTrainOcrLogByLogId_成功() {
        when(bizTrainOcrLogMapper.deleteBizTrainOcrLogByLogId(1L)).thenReturn(1);

        int result = bizTrainOcrLogService.deleteBizTrainOcrLogByLogId(1L);

        assertEquals(1, result);
    }
}
