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

import com.ruoyi.biz.domain.BizTrainArchive;
import com.ruoyi.biz.service.impl.BizTrainArchiveServiceImpl;

/**
 * BizTrainArchiveService 单元测试
 *
 * 测试范围：个人培训档案的CRUD和历史快照查询
 */
@ExtendWith(MockitoExtension.class)
class BizTrainArchiveServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainArchiveMapper bizTrainArchiveMapper;

    @InjectMocks
    private BizTrainArchiveServiceImpl bizTrainArchiveService;

    private BizTrainArchive testArchive;

    @BeforeEach
    void setUp() {
        testArchive = new BizTrainArchive();
        testArchive.setArchiveId(1L);
        testArchive.setUserId(100L);
        testArchive.setUserName("张三");
        testArchive.setDeptName("研发部");
        testArchive.setSubject("安全培训");
        testArchive.setTrainDate("2026-06-15");
        testArchive.setSpeaker("张老师");
        testArchive.setTrainHours("2");
        testArchive.setScore("合格");
        testArchive.setSignSeq("001");
    }

    // ===== selectBizTrainArchiveByArchiveId =====

    @Test
    @DisplayName("selectBizTrainArchiveByArchiveId: 档案存在时返回对象")
    void selectBizTrainArchiveByArchiveId_存在_返回对象() {
        when(bizTrainArchiveMapper.selectBizTrainArchiveByArchiveId(1L)).thenReturn(testArchive);

        BizTrainArchive result = bizTrainArchiveService.selectBizTrainArchiveByArchiveId(1L);

        assertNotNull(result);
        assertEquals("张三", result.getUserName());
        assertEquals("安全培训", result.getSubject());
        verify(bizTrainArchiveMapper, times(1)).selectBizTrainArchiveByArchiveId(1L);
    }

    @Test
    @DisplayName("selectBizTrainArchiveByArchiveId: 档案不存在时返回null")
    void selectBizTrainArchiveByArchiveId_不存在_返回null() {
        when(bizTrainArchiveMapper.selectBizTrainArchiveByArchiveId(999L)).thenReturn(null);

        BizTrainArchive result = bizTrainArchiveService.selectBizTrainArchiveByArchiveId(999L);

        assertNull(result);
    }

    // ===== selectArchiveListWithSnapshot =====

    @Test
    @DisplayName("selectArchiveListWithSnapshot: 返回档案列表（带历史快照）")
    void selectArchiveListWithSnapshot_返回列表() {
        BizTrainArchive archive2 = new BizTrainArchive();
        archive2.setArchiveId(2L);
        archive2.setUserName("李四");
        archive2.setSubject("技能培训");

        List<BizTrainArchive> mockList = Arrays.asList(testArchive, archive2);
        when(bizTrainArchiveMapper.selectArchiveListWithSnapshot(any())).thenReturn(mockList);

        List<BizTrainArchive> result = bizTrainArchiveService.selectArchiveListWithSnapshot(new BizTrainArchive());

        assertEquals(2, result.size());
        verify(bizTrainArchiveMapper, times(1)).selectArchiveListWithSnapshot(any());
    }

    @Test
    @DisplayName("selectArchiveListWithSnapshot: 按用户ID筛选")
    void selectArchiveListWithSnapshot_按用户ID筛选() {
        List<BizTrainArchive> mockList = Arrays.asList(testArchive);
        when(bizTrainArchiveMapper.selectArchiveListWithSnapshot(any())).thenReturn(mockList);

        BizTrainArchive query = new BizTrainArchive();
        query.setUserId(100L);
        List<BizTrainArchive> result = bizTrainArchiveService.selectArchiveListWithSnapshot(query);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getUserId());
    }

    @Test
    @DisplayName("selectArchiveListWithSnapshot: 按时间范围筛选")
    void selectArchiveListWithSnapshot_按时间范围筛选() {
        List<BizTrainArchive> mockList = Arrays.asList(testArchive);
        when(bizTrainArchiveMapper.selectArchiveListWithSnapshot(any())).thenReturn(mockList);

        BizTrainArchive query = new BizTrainArchive();
        query.setBeginTrainDate("2026-01-01");
        query.setEndTrainDate("2026-12-31");
        List<BizTrainArchive> result = bizTrainArchiveService.selectArchiveListWithSnapshot(query);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectArchiveListWithSnapshot: 无数据时返回空列表")
    void selectArchiveListWithSnapshot_空列表() {
        when(bizTrainArchiveMapper.selectArchiveListWithSnapshot(any())).thenReturn(Arrays.asList());

        List<BizTrainArchive> result = bizTrainArchiveService.selectArchiveListWithSnapshot(new BizTrainArchive());

        assertTrue(result.isEmpty());
    }

    // ===== insertBizTrainArchive =====

    @Test
    @DisplayName("insertBizTrainArchive: 成功插入")
    void insertBizTrainArchive_成功() {
        when(bizTrainArchiveMapper.insertBizTrainArchive(any())).thenReturn(1);

        BizTrainArchive newArchive = new BizTrainArchive();
        newArchive.setUserId(200L);
        newArchive.setUserName("王五");
        newArchive.setSubject("新培训");

        int result = bizTrainArchiveService.insertBizTrainArchive(newArchive);

        assertEquals(1, result);
        assertNotNull(newArchive.getCreateTime());
        verify(bizTrainArchiveMapper, times(1)).insertBizTrainArchive(any());
    }

    @Test
    @DisplayName("insertBizTrainArchive: 插入失败返回0")
    void insertBizTrainArchive_失败_返回0() {
        when(bizTrainArchiveMapper.insertBizTrainArchive(any())).thenReturn(0);

        BizTrainArchive newArchive = new BizTrainArchive();
        newArchive.setUserId(200L);

        int result = bizTrainArchiveService.insertBizTrainArchive(newArchive);

        assertEquals(0, result);
    }

    // ===== updateBizTrainArchive =====

    @Test
    @DisplayName("updateBizTrainArchive: 成功更新")
    void updateBizTrainArchive_成功() {
        when(bizTrainArchiveMapper.updateBizTrainArchive(any())).thenReturn(1);

        testArchive.setScore("优秀");
        int result = bizTrainArchiveService.updateBizTrainArchive(testArchive);

        assertEquals(1, result);
        assertNotNull(testArchive.getUpdateTime());
        verify(bizTrainArchiveMapper, times(1)).updateBizTrainArchive(any());
    }

    @Test
    @DisplayName("updateBizTrainArchive: 更新成绩")
    void updateBizTrainArchive_更新成绩() {
        when(bizTrainArchiveMapper.updateBizTrainArchive(any())).thenReturn(1);

        testArchive.setScore("不合格");
        int result = bizTrainArchiveService.updateBizTrainArchive(testArchive);

        assertEquals(1, result);
        assertEquals("不合格", testArchive.getScore());
    }

    // ===== deleteBizTrainArchiveByArchiveIds =====

    @Test
    @DisplayName("deleteBizTrainArchiveByArchiveIds: 批量删除成功")
    void deleteBizTrainArchiveByArchiveIds_成功() {
        when(bizTrainArchiveMapper.deleteBizTrainArchiveByArchiveIds(any())).thenReturn(3);

        int result = bizTrainArchiveService.deleteBizTrainArchiveByArchiveIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
        verify(bizTrainArchiveMapper, times(1)).deleteBizTrainArchiveByArchiveIds(any());
    }

    @Test
    @DisplayName("deleteBizTrainArchiveByArchiveIds: 空数组返回0")
    void deleteBizTrainArchiveByArchiveIds_空数组() {
        when(bizTrainArchiveMapper.deleteBizTrainArchiveByArchiveIds(any())).thenReturn(0);

        int result = bizTrainArchiveService.deleteBizTrainArchiveByArchiveIds(new Long[]{});

        assertEquals(0, result);
    }

    // ===== deleteBizTrainArchiveByArchiveId =====

    @Test
    @DisplayName("deleteBizTrainArchiveByArchiveId: 单个删除成功")
    void deleteBizTrainArchiveByArchiveId_成功() {
        when(bizTrainArchiveMapper.deleteBizTrainArchiveByArchiveId(1L)).thenReturn(1);

        int result = bizTrainArchiveService.deleteBizTrainArchiveByArchiveId(1L);

        assertEquals(1, result);
        verify(bizTrainArchiveMapper, times(1)).deleteBizTrainArchiveByArchiveId(1L);
    }

    // ===== 业务场景测试 =====

    @Test
    @DisplayName("补课档案: 申诉通过的补卡记录")
    void 补课档案_申诉通过() {
        BizTrainArchive supplementArchive = new BizTrainArchive();
        supplementArchive.setUserId(100L);
        supplementArchive.setUserName("张三");
        supplementArchive.setSignSeq("系统补卡");
        supplementArchive.setScore("免考/补卡");
        supplementArchive.setRemark("员工申诉补卡，系统自动同步入档");

        assertEquals("系统补卡", supplementArchive.getSignSeq());
        assertEquals("免考/补卡", supplementArchive.getScore());
        assertTrue(supplementArchive.getRemark().contains("申诉"));
    }

    @Test
    @DisplayName("档案查询: 使用时间范围过滤")
    void 档案查询_时间范围过滤() {
        BizTrainArchive query = new BizTrainArchive();
        query.setUserId(100L);
        query.setBeginTrainDate("2026-01-01");
        query.setEndTrainDate("2026-06-30");

        assertEquals("2026-01-01", query.getBeginTrainDate());
        assertEquals("2026-06-30", query.getEndTrainDate());
        assertEquals(100L, query.getUserId());
    }
}
