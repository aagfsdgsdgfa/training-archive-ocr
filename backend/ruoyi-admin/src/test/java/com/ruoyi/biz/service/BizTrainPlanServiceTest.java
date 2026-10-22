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

import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.service.impl.BizTrainPlanServiceImpl;

/**
 * BizTrainPlanService 单元测试
 *
 * 测试范围：年度培训计划的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainPlanServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainPlanMapper bizTrainPlanMapper;

    @InjectMocks
    private BizTrainPlanServiceImpl bizTrainPlanService;

    private BizTrainPlan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new BizTrainPlan();
        testPlan.setPlanId(1L);
        testPlan.setPlanYear(2026);
        testPlan.setPlanType("1");
        testPlan.setTrainLevel("公司级");
        testPlan.setSubject("安全培训");
        testPlan.setPlanMonth("6月");
        testPlan.setProjectCategory("必修");
        testPlan.setAudienceScope("0");
        testPlan.setStatus("0");
    }

    // ===== selectBizTrainPlanByPlanId =====

    @Test
    @DisplayName("selectBizTrainPlanByPlanId: 计划存在时返回对象")
    void selectBizTrainPlanByPlanId_存在_返回对象() {
        when(bizTrainPlanMapper.selectBizTrainPlanByPlanId(1L)).thenReturn(testPlan);

        BizTrainPlan result = bizTrainPlanService.selectBizTrainPlanByPlanId(1L);

        assertNotNull(result);
        assertEquals("安全培训", result.getSubject());
        assertEquals(2026, result.getPlanYear());
        verify(bizTrainPlanMapper, times(1)).selectBizTrainPlanByPlanId(1L);
    }

    @Test
    @DisplayName("selectBizTrainPlanByPlanId: 计划不存在时返回null")
    void selectBizTrainPlanByPlanId_不存在_返回null() {
        when(bizTrainPlanMapper.selectBizTrainPlanByPlanId(999L)).thenReturn(null);

        BizTrainPlan result = bizTrainPlanService.selectBizTrainPlanByPlanId(999L);

        assertNull(result);
    }

    // ===== selectBizTrainPlanList =====

    @Test
    @DisplayName("selectBizTrainPlanList: 返回所有计划列表")
    void selectBizTrainPlanList_返回列表() {
        BizTrainPlan plan2 = new BizTrainPlan();
        plan2.setPlanId(2L);
        plan2.setSubject("技能培训");

        List<BizTrainPlan> mockList = Arrays.asList(testPlan, plan2);
        when(bizTrainPlanMapper.selectBizTrainPlanList(any(BizTrainPlan.class))).thenReturn(mockList);

        List<BizTrainPlan> result = bizTrainPlanService.selectBizTrainPlanList(new BizTrainPlan());

        assertEquals(2, result.size());
        verify(bizTrainPlanMapper, times(1)).selectBizTrainPlanList(any());
    }

    @Test
    @DisplayName("selectBizTrainPlanList: 按年份筛选")
    void selectBizTrainPlanList_按年份筛选() {
        List<BizTrainPlan> mockList = Arrays.asList(testPlan);
        when(bizTrainPlanMapper.selectBizTrainPlanList(any(BizTrainPlan.class))).thenReturn(mockList);

        BizTrainPlan query = new BizTrainPlan();
        query.setPlanYear(2026);
        List<BizTrainPlan> result = bizTrainPlanService.selectBizTrainPlanList(query);

        assertEquals(1, result.size());
        assertEquals(2026, result.get(0).getPlanYear());
    }

    @Test
    @DisplayName("selectBizTrainPlanList: 按培训内容关键词筛选")
    void selectBizTrainPlanList_按关键词筛选() {
        List<BizTrainPlan> mockList = Arrays.asList(testPlan);
        when(bizTrainPlanMapper.selectBizTrainPlanList(any(BizTrainPlan.class))).thenReturn(mockList);

        BizTrainPlan query = new BizTrainPlan();
        query.setSubject("安全");
        List<BizTrainPlan> result = bizTrainPlanService.selectBizTrainPlanList(query);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectBizTrainPlanList: 无数据时返回空列表")
    void selectBizTrainPlanList_空列表() {
        when(bizTrainPlanMapper.selectBizTrainPlanList(any())).thenReturn(Arrays.asList());

        List<BizTrainPlan> result = bizTrainPlanService.selectBizTrainPlanList(new BizTrainPlan());

        assertTrue(result.isEmpty());
    }

    // ===== insertBizTrainPlan =====

    @Test
    @DisplayName("insertBizTrainPlan: 成功插入")
    void insertBizTrainPlan_成功() {
        when(bizTrainPlanMapper.insertBizTrainPlan(any())).thenReturn(1);

        BizTrainPlan newPlan = new BizTrainPlan();
        newPlan.setSubject("新培训计划");
        newPlan.setPlanYear(2026);
        newPlan.setPlanType("1");

        int result = bizTrainPlanService.insertBizTrainPlan(newPlan);

        assertEquals(1, result);
        assertNotNull(newPlan.getCreateTime());
        verify(bizTrainPlanMapper, times(1)).insertBizTrainPlan(any());
    }

    @Test
    @DisplayName("insertBizTrainPlan: 插入失败返回0")
    void insertBizTrainPlan_失败_返回0() {
        when(bizTrainPlanMapper.insertBizTrainPlan(any())).thenReturn(0);

        BizTrainPlan newPlan = new BizTrainPlan();
        newPlan.setSubject("失败计划");

        int result = bizTrainPlanService.insertBizTrainPlan(newPlan);

        assertEquals(0, result);
    }

    // ===== updateBizTrainPlan =====

    @Test
    @DisplayName("updateBizTrainPlan: 成功更新")
    void updateBizTrainPlan_成功() {
        when(bizTrainPlanMapper.updateBizTrainPlan(any())).thenReturn(1);

        testPlan.setSubject("更新后的培训");
        int result = bizTrainPlanService.updateBizTrainPlan(testPlan);

        assertEquals(1, result);
        assertNotNull(testPlan.getUpdateTime());
        verify(bizTrainPlanMapper, times(1)).updateBizTrainPlan(any());
    }

    @Test
    @DisplayName("updateBizTrainPlan: 更新状态为已完成")
    void updateBizTrainPlan_更新为已完成() {
        when(bizTrainPlanMapper.updateBizTrainPlan(any())).thenReturn(1);

        // 将状态从"进行中"更新为"已完成"
        testPlan.setStatus("1"); // 假设1是已完成
        int result = bizTrainPlanService.updateBizTrainPlan(testPlan);

        assertEquals(1, result);
        assertEquals("1", testPlan.getStatus());
    }

    // ===== deleteBizTrainPlanByPlanIds =====

    @Test
    @DisplayName("deleteBizTrainPlanByPlanIds: 批量删除成功")
    void deleteBizTrainPlanByPlanIds_成功() {
        when(bizTrainPlanMapper.deleteBizTrainPlanByPlanIds(any())).thenReturn(3);

        int result = bizTrainPlanService.deleteBizTrainPlanByPlanIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
        verify(bizTrainPlanMapper, times(1)).deleteBizTrainPlanByPlanIds(any());
    }

    @Test
    @DisplayName("deleteBizTrainPlanByPlanIds: 空数组返回0")
    void deleteBizTrainPlanByPlanIds_空数组() {
        when(bizTrainPlanMapper.deleteBizTrainPlanByPlanIds(any())).thenReturn(0);

        int result = bizTrainPlanService.deleteBizTrainPlanByPlanIds(new Long[]{});

        assertEquals(0, result);
    }

    // ===== deleteBizTrainPlanByPlanId =====

    @Test
    @DisplayName("deleteBizTrainPlanByPlanId: 单个删除成功")
    void deleteBizTrainPlanByPlanId_成功() {
        when(bizTrainPlanMapper.deleteBizTrainPlanByPlanId(1L)).thenReturn(1);

        int result = bizTrainPlanService.deleteBizTrainPlanByPlanId(1L);

        assertEquals(1, result);
        verify(bizTrainPlanMapper, times(1)).deleteBizTrainPlanByPlanId(1L);
    }

    // ===== 业务逻辑测试 =====

    @Test
    @DisplayName("计划类型: 公司级和部门级区分")
    void planType_公司级和部门级() {
        BizTrainPlan companyPlan = new BizTrainPlan();
        companyPlan.setPlanType("1"); // 公司级
        companyPlan.setTrainLevel("公司级");

        BizTrainPlan deptPlan = new BizTrainPlan();
        deptPlan.setPlanType("2"); // 部门级
        deptPlan.setTrainLevel("部门级");

        assertEquals("公司级", companyPlan.getTrainLevel());
        assertEquals("部门级", deptPlan.getTrainLevel());
    }

    @Test
    @DisplayName("受众范围: 全体/部门/个人")
    void audienceScope_三种类型() {
        BizTrainPlan allPlan = new BizTrainPlan();
        allPlan.setAudienceScope("0"); // 全体

        BizTrainPlan deptPlan = new BizTrainPlan();
        deptPlan.setAudienceScope("1"); // 部门

        BizTrainPlan personPlan = new BizTrainPlan();
        personPlan.setAudienceScope("2"); // 个人

        assertEquals("0", allPlan.getAudienceScope());
        assertEquals("1", deptPlan.getAudienceScope());
        assertEquals("2", personPlan.getAudienceScope());
    }
}
