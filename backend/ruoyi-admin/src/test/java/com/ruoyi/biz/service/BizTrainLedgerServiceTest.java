package com.ruoyi.biz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ruoyi.biz.domain.BizTrainLedger;
import com.ruoyi.biz.service.impl.BizTrainLedgerServiceImpl;
import com.ruoyi.biz.util.SerialNoGenerator;
import com.ruoyi.biz.util.SerialNoResult;
import java.time.LocalDate;

/**
 * BizTrainLedgerService 单元测试
 *
 * 测试范围：培训台账总表的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainLedgerServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainLedgerMapper bizTrainLedgerMapper;

    @Mock
    private SerialNoGenerator serialNoGenerator;

    @InjectMocks
    private BizTrainLedgerServiceImpl bizTrainLedgerService;

    private BizTrainLedger testLedger;

    @BeforeEach
    void setUp() {
        // 流水号生成器 Stub：避免 archiveSession/insertBizTrainLedger 触发 NPE
        // 用 lenient() 是因为并非所有测试都会触发该 mock 调用，
        // STRICT_STUBS 模式会对未使用的 stub 报错
        // any() 同时匹配 String 和 null（anyString() 不会匹配 null，archiveSession/insertBizTrainLedger
        // 在 trainDate 为空时会传 null，所以必须用 any()）
        lenient().when(serialNoGenerator.generate(any(), any()))
                .thenReturn(new SerialNoResult("20260617-001", LocalDate.of(2026, 6, 17)));

        testLedger = new BizTrainLedger();
        testLedger.setLedgerId(1L);
        testLedger.setPlanId(100L);
        testLedger.setSubject("安全培训");
        testLedger.setHostDeptName("培训部");
        testLedger.setTrainDate("2026-06-15");
        testLedger.setTrainHours("2");
        testLedger.setSpeaker("张老师");
        testLedger.setLocationName("会议室A");
        testLedger.setTargetCount(50);
        testLedger.setActualCount(48);
        testLedger.setAttendeeNames("张三,李四,王五");
    }

    // ===== selectBizTrainLedgerByLedgerId =====

    @Test
    @DisplayName("selectBizTrainLedgerByLedgerId: 台账存在时返回对象")
    void selectBizTrainLedgerByLedgerId_存在_返回对象() {
        when(bizTrainLedgerMapper.selectBizTrainLedgerByLedgerId(1L)).thenReturn(testLedger);

        BizTrainLedger result = bizTrainLedgerService.selectBizTrainLedgerByLedgerId(1L);

        assertNotNull(result);
        assertEquals("安全培训", result.getSubject());
        assertEquals(48, result.getActualCount());
    }

    @Test
    @DisplayName("selectBizTrainLedgerByLedgerId: 台账不存在时返回null")
    void selectBizTrainLedgerByLedgerId_不存在_返回null() {
        when(bizTrainLedgerMapper.selectBizTrainLedgerByLedgerId(999L)).thenReturn(null);

        BizTrainLedger result = bizTrainLedgerService.selectBizTrainLedgerByLedgerId(999L);

        assertNull(result);
    }

    // ===== selectBizTrainLedgerList =====

    @Test
    @DisplayName("selectBizTrainLedgerList: 返回台账列表")
    void selectBizTrainLedgerList_返回列表() {
        BizTrainLedger ledger2 = new BizTrainLedger();
        ledger2.setLedgerId(2L);
        ledger2.setSubject("技能培训");

        List<BizTrainLedger> mockList = Arrays.asList(testLedger, ledger2);
        when(bizTrainLedgerMapper.selectBizTrainLedgerList(any())).thenReturn(mockList);

        List<BizTrainLedger> result = bizTrainLedgerService.selectBizTrainLedgerList(new BizTrainLedger());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("selectBizTrainLedgerList: 按部门筛选")
    void selectBizTrainLedgerList_按部门筛选() {
        when(bizTrainLedgerMapper.selectBizTrainLedgerList(any())).thenReturn(Arrays.asList(testLedger));

        BizTrainLedger query = new BizTrainLedger();
        query.setHostDeptName("培训部");
        List<BizTrainLedger> result = bizTrainLedgerService.selectBizTrainLedgerList(query);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectBizTrainLedgerList: 无数据时返回空列表")
    void selectBizTrainLedgerList_空列表() {
        when(bizTrainLedgerMapper.selectBizTrainLedgerList(any())).thenReturn(Arrays.asList());

        List<BizTrainLedger> result = bizTrainLedgerService.selectBizTrainLedgerList(new BizTrainLedger());

        assertTrue(result.isEmpty());
    }

    // ===== insertBizTrainLedger =====

    @Test
    @DisplayName("insertBizTrainLedger: 成功插入")
    void insertBizTrainLedger_成功() {
        when(bizTrainLedgerMapper.insertBizTrainLedger(any())).thenReturn(1);

        BizTrainLedger newLedger = new BizTrainLedger();
        newLedger.setSubject("新培训");
        int result = bizTrainLedgerService.insertBizTrainLedger(newLedger);

        assertEquals(1, result);
    }

    // ===== updateBizTrainLedger =====

    @Test
    @DisplayName("updateBizTrainLedger: 成功更新")
    void updateBizTrainLedger_成功() {
        when(bizTrainLedgerMapper.updateBizTrainLedger(any())).thenReturn(1);

        testLedger.setActualCount(50);
        int result = bizTrainLedgerService.updateBizTrainLedger(testLedger);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainLedger: 人工校正单值日期后,serialDate 同步重算")
    void updateBizTrainLedger_单值日期_同步serialDate() {
        when(bizTrainLedgerMapper.updateBizTrainLedger(any())).thenReturn(1);

        // 原始 serial_date=2026-07-01(假设创建时是 7-1),人工校正为 2026-04-13
        testLedger.setSerialDate(LocalDate.of(2026, 7, 1));
        testLedger.setTrainDate("2026-04-13");

        bizTrainLedgerService.updateBizTrainLedger(testLedger);

        ArgumentCaptor<BizTrainLedger> captor = ArgumentCaptor.forClass(BizTrainLedger.class);
        verify(bizTrainLedgerMapper).updateBizTrainLedger(captor.capture());
        BizTrainLedger passed = captor.getValue();
        assertEquals(LocalDate.of(2026, 4, 13), passed.getSerialDate(),
                "serialDate 应被同步为校正后 trainDate 的起始日");
        assertNotNull(passed.getUpdateTime(), "updateTime 应被填充");
    }

    @Test
    @DisplayName("updateBizTrainLedger: 区间日期时,serialDate 取起始日")
    void updateBizTrainLedger_区间日期_serialDate取起始日() {
        when(bizTrainLedgerMapper.updateBizTrainLedger(any())).thenReturn(1);

        testLedger.setSerialDate(LocalDate.of(2026, 1, 1));
        testLedger.setTrainDate("2026-04-13 — 2026-04-15");

        bizTrainLedgerService.updateBizTrainLedger(testLedger);

        ArgumentCaptor<BizTrainLedger> captor = ArgumentCaptor.forClass(BizTrainLedger.class);
        verify(bizTrainLedgerMapper).updateBizTrainLedger(captor.capture());
        assertEquals(LocalDate.of(2026, 4, 13), captor.getValue().getSerialDate(),
                "区间 trainDate 应取起始日 2026-04-13");
    }

    @Test
    @DisplayName("updateBizTrainLedger: trainDate 非法时保留原 serialDate,update 不报错")
    void updateBizTrainLedger_trainDate非法_保留原serialDate() {
        when(bizTrainLedgerMapper.updateBizTrainLedger(any())).thenReturn(1);

        LocalDate originalSerialDate = LocalDate.of(2026, 7, 1);
        testLedger.setSerialDate(originalSerialDate);
        testLedger.setTrainDate("not a date");

        int result = bizTrainLedgerService.updateBizTrainLedger(testLedger);

        assertEquals(1, result, "解析失败不应阻塞 update");
        ArgumentCaptor<BizTrainLedger> captor = ArgumentCaptor.forClass(BizTrainLedger.class);
        verify(bizTrainLedgerMapper).updateBizTrainLedger(captor.capture());
        assertEquals(originalSerialDate, captor.getValue().getSerialDate(),
                "解析失败时 serialDate 应保留原值");
    }

    @Test
    @DisplayName("updateBizTrainLedger: trainDate 为空时保留原 serialDate,update 不报错")
    void updateBizTrainLedger_trainDate为空_保留原serialDate() {
        when(bizTrainLedgerMapper.updateBizTrainLedger(any())).thenReturn(1);

        LocalDate originalSerialDate = LocalDate.of(2026, 7, 1);
        testLedger.setSerialDate(originalSerialDate);
        testLedger.setTrainDate(null);

        bizTrainLedgerService.updateBizTrainLedger(testLedger);

        ArgumentCaptor<BizTrainLedger> captor = ArgumentCaptor.forClass(BizTrainLedger.class);
        verify(bizTrainLedgerMapper).updateBizTrainLedger(captor.capture());
        assertEquals(originalSerialDate, captor.getValue().getSerialDate());
    }

    // ===== deleteBizTrainLedgerByLedgerIds =====

    @Test
    @DisplayName("deleteBizTrainLedgerByLedgerIds: 批量删除成功")
    void deleteBizTrainLedgerByLedgerIds_成功() {
        when(bizTrainLedgerMapper.deleteBizTrainLedgerByLedgerIds(any())).thenReturn(3);

        int result = bizTrainLedgerService.deleteBizTrainLedgerByLedgerIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainLedgerByLedgerId: 单个删除成功")
    void deleteBizTrainLedgerByLedgerId_成功() {
        when(bizTrainLedgerMapper.deleteBizTrainLedgerByLedgerId(1L)).thenReturn(1);

        int result = bizTrainLedgerService.deleteBizTrainLedgerByLedgerId(1L);

        assertEquals(1, result);
    }

    // ===== 业务场景测试 =====

    @Test
    @DisplayName("台账统计: 实到人数计算")
    void 台账统计_实到人数计算() {
        assertEquals(48, testLedger.getActualCount());
        assertEquals(50, testLedger.getTargetCount());
        // 出勤率
        double attendanceRate = (double) testLedger.getActualCount() / testLedger.getTargetCount();
        assertEquals(0.96, attendanceRate, 0.01);
    }

    @Test
    @DisplayName("受众范围: 全体员工显示")
    void 受众范围_全体员工显示() {
        testLedger.setAttendeeNames("全体员工");
        assertEquals("全体员工", testLedger.getAttendeeNames());
    }
}
