package com.ruoyi.biz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ruoyi.biz.domain.BizTrainSession;
import com.ruoyi.biz.domain.BizTrainAttendance;
import com.ruoyi.biz.domain.BizTrainLedger;
import com.ruoyi.biz.domain.BizTrainArchive;
import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.mapper.BizTrainSessionMapper;
import com.ruoyi.biz.mapper.BizTrainLedgerMapper;
import com.ruoyi.biz.mapper.BizTrainArchiveMapper;
import com.ruoyi.biz.mapper.BizTrainPlanMapper;
import com.ruoyi.biz.mapper.BizTrainAttendanceMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.biz.service.impl.BizTrainSessionServiceImpl;
import com.ruoyi.biz.util.SerialNoGenerator;
import com.ruoyi.biz.util.SerialNoResult;
import com.ruoyi.common.exception.ServiceException;
import java.time.LocalDate;

/**
 * BizTrainSessionService 单元测试（TDD 风格）
 *
 * 测试范围：
 * 1. 归档逻辑 archiveSession()
 * 2. 状态转换校验 updateBizTrainSession()
 * 3. 场地冲突校验 checkLocationConflict()
 * 4. CRUD 操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainSessionServiceTest {

    @Mock
    private BizTrainSessionMapper bizTrainSessionMapper;

    @Mock
    private BizTrainLedgerMapper bizTrainLedgerMapper;

    @Mock
    private BizTrainArchiveMapper bizTrainArchiveMapper;

    @Mock
    private BizTrainPlanMapper bizTrainPlanMapper;

    @Mock
    private BizTrainAttendanceMapper bizTrainAttendanceMapper;

    @Mock
    private com.ruoyi.system.mapper.SysUserMapper sysUserMapper;

    @Mock
    private SerialNoGenerator serialNoGenerator;

    @InjectMocks
    private BizTrainSessionServiceImpl bizTrainSessionService;

    private BizTrainSession testSession;
    private BizTrainPlan testPlan;
    private BizTrainAttendance testAttendance;
    private SysUser testUser;

    @BeforeEach
    void setUp() {
        // 流水号生成器 Stub：避免 archiveSession 触发 NPE
        // 用 lenient() 是因为并非所有测试都会触发该 mock 调用，
        // STRICT_STUBS 模式会对未使用的 stub 报错
        // any() 同时匹配 String 和 null（anyString() 不会匹配 null，archiveSession
        // 在 trainDate 为空时会传 null，所以必须用 any()）
        lenient().when(serialNoGenerator.generate(any(), any()))
                .thenReturn(new SerialNoResult("20260617-001", LocalDate.of(2026, 6, 17)));

        // 初始化测试计划
        testPlan = new BizTrainPlan();
        testPlan.setPlanId(1L);
        testPlan.setPlanType("1");
        testPlan.setTrainLevel("公司级");
        testPlan.setAudienceScope("0");
        testPlan.setStatus("0");

        // 初始化测试场次
        testSession = new BizTrainSession();
        testSession.setSessionId(1L);
        testSession.setPlanId(1L);
        testSession.setSubject("安全培训");
        testSession.setStatus("2"); // 待审归档状态
        testSession.setHostDeptName("培训部");
        testSession.setSpeaker("张老师");
        testSession.setTrainHours(new java.math.BigDecimal("2"));
        testSession.setLocationName("会议室A");
        testSession.setStartTime(new java.util.Date());
        testSession.setEndTime(new java.util.Date(System.currentTimeMillis() + 7200000)); // 2小时后

        // 初始化测试考勤
        testAttendance = new BizTrainAttendance();
        testAttendance.setRecordId(1L);
        testAttendance.setSessionId(1L);
        testAttendance.setUserId(100L);
        testAttendance.setSignStatus("0"); // 正常
        testAttendance.setSignTime(new java.util.Date());

        // 初始化测试用户
        testUser = new SysUser();
        testUser.setUserId(100L);
        testUser.setNickName("张三");
        testUser.setDeptId(10L);
        SysDept dept = new SysDept();
        dept.setDeptId(10L);
        dept.setDeptName("研发部");
        testUser.setDept(dept);
    }

    // ===== archiveSession 归档逻辑测试 =====

    @Test
    @DisplayName("archiveSession: 正常归档生成台账和个人档案")
    void archiveSession_正常归档_生成台账和档案() {
        // 准备数据
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainPlanMapper.selectBizTrainPlanByPlanId(1L)).thenReturn(testPlan);

        BizTrainAttendance att = new BizTrainAttendance();
        att.setRecordId(1L);
        att.setSessionId(1L);
        att.setUserId(100L);
        att.setSignStatus("0");
        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any())).thenReturn(Arrays.asList(att));
        when(sysUserMapper.selectUserById(100L)).thenReturn(testUser);
        when(bizTrainLedgerMapper.insertBizTrainLedger(any())).thenReturn(1);
        when(bizTrainArchiveMapper.insertBizTrainArchive(any())).thenReturn(1);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        // 执行
        bizTrainSessionService.archiveSession(1L, "admin");

        // 验证
        verify(bizTrainLedgerMapper).insertBizTrainLedger(any(BizTrainLedger.class));
        verify(bizTrainArchiveMapper).insertBizTrainArchive(any(BizTrainArchive.class));
        verify(bizTrainSessionMapper).updateBizTrainSession(any());
    }

    @Test
    @DisplayName("archiveSession: 场次状态非2时抛出异常")
    void archiveSession_状态非2_抛异常() {
        testSession.setStatus("1"); // 进行中状态
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            bizTrainSessionService.archiveSession(1L, "admin");
        });

        assertTrue(ex.getMessage().contains("待审归档"));
        verify(bizTrainLedgerMapper, never()).insertBizTrainLedger(any());
        verify(bizTrainArchiveMapper, never()).insertBizTrainArchive(any());
    }

    @Test
    @DisplayName("archiveSession: 场次不存在时抛出异常")
    void archiveSession_场次不存在_抛异常() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(999L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            bizTrainSessionService.archiveSession(999L, "admin");
        });

        assertTrue(ex.getMessage().contains("待审归档"));
    }

    @Test
    @DisplayName("archiveSession: 无合格人员时只生成空台账")
    void archiveSession_无合格人员_只生成空台账() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainPlanMapper.selectBizTrainPlanByPlanId(1L)).thenReturn(testPlan);
        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any())).thenReturn(new ArrayList<>());
        when(bizTrainLedgerMapper.insertBizTrainLedger(any())).thenReturn(1);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        bizTrainSessionService.archiveSession(1L, "admin");

        // 验证只插入了台账，没有插入档案
        verify(bizTrainLedgerMapper).insertBizTrainLedger(any(BizTrainLedger.class));
        verify(bizTrainArchiveMapper, never()).insertBizTrainArchive(any());
    }

    @Test
    @DisplayName("archiveSession: 迟到人员也计入合格人员")
    void archiveSession_迟到人员_计入合格人员() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainPlanMapper.selectBizTrainPlanByPlanId(1L)).thenReturn(testPlan);

        BizTrainAttendance normalAtt = new BizTrainAttendance();
        normalAtt.setRecordId(1L);
        normalAtt.setSessionId(1L);
        normalAtt.setUserId(100L);
        normalAtt.setSignStatus("0"); // 正常

        BizTrainAttendance lateAtt = new BizTrainAttendance();
        lateAtt.setRecordId(2L);
        lateAtt.setSessionId(1L);
        lateAtt.setUserId(101L);
        lateAtt.setSignStatus("1"); // 迟到

        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any())).thenReturn(Arrays.asList(normalAtt, lateAtt));
        when(sysUserMapper.selectUserById(100L)).thenReturn(testUser);
        when(sysUserMapper.selectUserById(101L)).thenReturn(testUser);
        when(bizTrainLedgerMapper.insertBizTrainLedger(any())).thenReturn(1);
        when(bizTrainArchiveMapper.insertBizTrainArchive(any())).thenReturn(1);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        bizTrainSessionService.archiveSession(1L, "admin");

        // 验证插入了2个档案（正常+迟到）
        verify(bizTrainArchiveMapper, times(2)).insertBizTrainArchive(any(BizTrainArchive.class));
    }

    @Test
    @DisplayName("archiveSession: 缺卡人员不计入合格人员")
    void archiveSession_缺卡人员_不计入() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainPlanMapper.selectBizTrainPlanByPlanId(1L)).thenReturn(testPlan);

        BizTrainAttendance absentAtt = new BizTrainAttendance();
        absentAtt.setRecordId(1L);
        absentAtt.setSessionId(1L);
        absentAtt.setUserId(100L);
        absentAtt.setSignStatus("2"); // 缺卡

        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any())).thenReturn(Arrays.asList(absentAtt));
        when(bizTrainLedgerMapper.insertBizTrainLedger(any())).thenReturn(1);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        bizTrainSessionService.archiveSession(1L, "admin");

        // 验证没有插入档案
        verify(bizTrainArchiveMapper, never()).insertBizTrainArchive(any());
    }

    @Test
    @DisplayName("archiveSession: 全部场次归档后计划状态变更为已完成")
    void archiveSession_全部场次归档_计划变已完成() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainPlanMapper.selectBizTrainPlanByPlanId(1L)).thenReturn(testPlan);

        BizTrainAttendance att = new BizTrainAttendance();
        att.setRecordId(1L);
        att.setSessionId(1L);
        att.setUserId(100L);
        att.setSignStatus("0");

        when(bizTrainAttendanceMapper.selectBizTrainAttendanceList(any())).thenReturn(Arrays.asList(att));
        when(sysUserMapper.selectUserById(100L)).thenReturn(testUser);
        when(bizTrainLedgerMapper.insertBizTrainLedger(any())).thenReturn(1);
        when(bizTrainArchiveMapper.insertBizTrainArchive(any())).thenReturn(1);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        // 只有一个场次且已归档
        BizTrainSession archivedSession = new BizTrainSession();
        archivedSession.setSessionId(1L);
        archivedSession.setStatus("3"); // 已归档
        when(bizTrainSessionMapper.selectBizTrainSessionList(any())).thenReturn(Arrays.asList(archivedSession));

        bizTrainSessionService.archiveSession(1L, "admin");

        // 验证计划状态被更新为已完成
        verify(bizTrainPlanMapper).updateBizTrainPlan(any());
    }

    // ===== updateBizTrainSession 状态转换测试 =====

    @Test
    @DisplayName("updateBizTrainSession: 已归档场次禁止修改")
    void updateBizTrainSession_已归档场次_抛异常() {
        testSession.setStatus("3"); // 已归档
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            bizTrainSessionService.updateBizTrainSession(testSession);
        });

        assertTrue(ex.getMessage().contains("已归档"));
        assertTrue(ex.getMessage().contains("禁止修改"));
    }

    @Test
    @DisplayName("updateBizTrainSession: 场次不存在时抛出异常")
    void updateBizTrainSession_场次不存在_抛异常() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(999L)).thenReturn(null);

        BizTrainSession session = new BizTrainSession();
        session.setSessionId(999L);

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            bizTrainSessionService.updateBizTrainSession(session);
        });

        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("updateBizTrainSession: 状态0转1允许")
    void updateBizTrainSession_未开始转进行中_允许() {
        testSession.setStatus("0"); // 未开始
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        BizTrainSession updateSession = new BizTrainSession();
        updateSession.setSessionId(1L);
        updateSession.setStatus("1"); // 进行中

        int result = bizTrainSessionService.updateBizTrainSession(updateSession);

        assertEquals(1, result);
        verify(bizTrainSessionMapper).updateBizTrainSession(any());
    }

    @Test
    @DisplayName("updateBizTrainSession: 状态1转2允许")
    void updateBizTrainSession_进行中转待审归档_允许() {
        testSession.setStatus("1"); // 进行中
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        BizTrainSession updateSession = new BizTrainSession();
        updateSession.setSessionId(1L);
        updateSession.setStatus("2"); // 待审归档

        int result = bizTrainSessionService.updateBizTrainSession(updateSession);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainSession: 状态2转3允许（手动归档）")
    void updateBizTrainSession_待审归档转已归档_允许() {
        testSession.setStatus("2"); // 待审归档
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        BizTrainSession updateSession = new BizTrainSession();
        updateSession.setSessionId(1L);
        updateSession.setStatus("3"); // 已归档

        int result = bizTrainSessionService.updateBizTrainSession(updateSession);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainSession: 状态3禁止转换为任何状态")
    void updateBizTrainSession_已归档禁止任何转换() {
        testSession.setStatus("3"); // 已归档
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);

        BizTrainSession updateSession = new BizTrainSession();
        updateSession.setSessionId(1L);
        updateSession.setStatus("1"); // 试图转为进行中

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            bizTrainSessionService.updateBizTrainSession(updateSession);
        });

        assertTrue(ex.getMessage().contains("已归档"));
    }

    @Test
    @DisplayName("updateBizTrainSession: 状态0禁止直接转为2")
    void updateBizTrainSession_未开始禁止直接转待审归档() {
        testSession.setStatus("0"); // 未开始
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);

        BizTrainSession updateSession = new BizTrainSession();
        updateSession.setSessionId(1L);
        updateSession.setStatus("2"); // 试图直接转为待审归档

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            bizTrainSessionService.updateBizTrainSession(updateSession);
        });

        assertTrue(ex.getMessage().contains("状态转换不允许"));
    }

    @Test
    @DisplayName("updateBizTrainSession: 状态2可以转回1")
    void updateBizTrainSession_待审归档可以转回进行中() {
        testSession.setStatus("2"); // 待审归档
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);
        when(bizTrainSessionMapper.updateBizTrainSession(any())).thenReturn(1);

        BizTrainSession updateSession = new BizTrainSession();
        updateSession.setSessionId(1L);
        updateSession.setStatus("1"); // 转回进行中

        int result = bizTrainSessionService.updateBizTrainSession(updateSession);

        assertEquals(1, result);
    }

    // ===== checkLocationConflict 场地冲突测试 =====

    @Test
    @DisplayName("checkLocationConflict: 存在冲突时返回1")
    void checkLocationConflict_存在冲突_返回1() {
        when(bizTrainSessionMapper.checkLocationConflict(any())).thenReturn(1);

        int result = bizTrainSessionService.checkLocationConflict(testSession);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("checkLocationConflict: 无冲突时返回0")
    void checkLocationConflict_无冲突_返回0() {
        when(bizTrainSessionMapper.checkLocationConflict(any())).thenReturn(0);

        int result = bizTrainSessionService.checkLocationConflict(testSession);

        assertEquals(0, result);
    }

    // ===== CRUD 测试 =====

    @Test
    @DisplayName("selectBizTrainSessionById: 场次存在时返回对象")
    void selectBizTrainSessionById_存在_返回对象() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(1L)).thenReturn(testSession);

        BizTrainSession result = bizTrainSessionService.selectBizTrainSessionBySessionId(1L);

        assertNotNull(result);
        assertEquals("安全培训", result.getSubject());
    }

    @Test
    @DisplayName("selectBizTrainSessionById: 场次不存在时返回null")
    void selectBizTrainSessionById_不存在_返回null() {
        when(bizTrainSessionMapper.selectBizTrainSessionBySessionId(999L)).thenReturn(null);

        BizTrainSession result = bizTrainSessionService.selectBizTrainSessionBySessionId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("selectBizTrainSessionList: 返回列表")
    void selectBizTrainSessionList_返回列表() {
        List<BizTrainSession> mockList = Arrays.asList(testSession);
        when(bizTrainSessionMapper.selectBizTrainSessionList(any())).thenReturn(mockList);

        List<BizTrainSession> result = bizTrainSessionService.selectBizTrainSessionList(new BizTrainSession());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("insertBizTrainSession: 成功插入")
    void insertBizTrainSession_成功() {
        when(bizTrainSessionMapper.insertBizTrainSession(any())).thenReturn(1);

        int result = bizTrainSessionService.insertBizTrainSession(testSession);

        assertEquals(1, result);
        assertNotNull(testSession.getCreateTime());
    }

    @Test
    @DisplayName("deleteBizTrainSessionByIds: 批量删除成功")
    void deleteBizTrainSessionByIds_成功() {
        when(bizTrainSessionMapper.deleteBizTrainSessionBySessionIds(any())).thenReturn(3);

        int result = bizTrainSessionService.deleteBizTrainSessionBySessionIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainSessionById: 单个删除成功")
    void deleteBizTrainSessionById_成功() {
        when(bizTrainSessionMapper.deleteBizTrainSessionBySessionId(1L)).thenReturn(1);

        int result = bizTrainSessionService.deleteBizTrainSessionBySessionId(1L);

        assertEquals(1, result);
    }
}
