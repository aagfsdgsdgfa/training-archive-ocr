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

import com.ruoyi.biz.domain.BizDevice;
import com.ruoyi.biz.mapper.BizDeviceMapper;
import com.ruoyi.biz.service.impl.BizDeviceServiceImpl;

/**
 * BizDeviceService 单元测试（TDD 风格）
 *
 * RED: 先写测试，验证功能需求
 * GREEN: 实现最小代码让测试通过
 * REFACTOR: 重构改进
 */
@ExtendWith(MockitoExtension.class)
class BizDeviceServiceTest {

    @Mock
    private BizDeviceMapper bizDeviceMapper;

    @InjectMocks
    private BizDeviceServiceImpl bizDeviceService;

    private BizDevice testDevice;

    @BeforeEach
    void setUp() {
        testDevice = new BizDevice();
        testDevice.setDeviceId(1L);
        testDevice.setDeviceName("测试设备");
        testDevice.setDeviceType("高拍仪");
        testDevice.setDeviceSn("SN-001");
        testDevice.setDeptId(100L);
        testDevice.setLocationId(200L);
        testDevice.setIpAddress("192.168.1.1");
        testDevice.setStatus("0");
    }

    // ===== selectBizDeviceByDeviceId =====

    @Test
    @DisplayName("selectBizDeviceByDeviceId: 设备存在时返回设备对象")
    void selectBizDeviceByDeviceId_existingDevice_returnsDevice() {
        when(bizDeviceMapper.selectBizDeviceByDeviceId(1L)).thenReturn(testDevice);

        BizDevice result = bizDeviceService.selectBizDeviceByDeviceId(1L);

        assertNotNull(result);
        assertEquals("测试设备", result.getDeviceName());
        assertEquals("SN-001", result.getDeviceSn());
        verify(bizDeviceMapper, times(1)).selectBizDeviceByDeviceId(1L);
    }

    @Test
    @DisplayName("selectBizDeviceByDeviceId: 设备不存在时返回null")
    void selectBizDeviceByDeviceId_nonExisting_returnsNull() {
        when(bizDeviceMapper.selectBizDeviceByDeviceId(999L)).thenReturn(null);

        BizDevice result = bizDeviceService.selectBizDeviceByDeviceId(999L);

        assertNull(result);
        verify(bizDeviceMapper, times(1)).selectBizDeviceByDeviceId(999L);
    }

    // ===== selectBizDeviceList =====

    @Test
    @DisplayName("selectBizDeviceList: 返回所有设备列表")
    void selectBizDeviceList_noFilter_returnsAllDevices() {
        BizDevice device2 = new BizDevice();
        device2.setDeviceId(2L);
        device2.setDeviceName("设备2");
        device2.setDeviceSn("SN-002");

        List<BizDevice> mockList = Arrays.asList(testDevice, device2);
        when(bizDeviceMapper.selectBizDeviceList(any(BizDevice.class))).thenReturn(mockList);

        List<BizDevice> result = bizDeviceService.selectBizDeviceList(new BizDevice());

        assertEquals(2, result.size());
        verify(bizDeviceMapper, times(1)).selectBizDeviceList(any(BizDevice.class));
    }

    @Test
    @DisplayName("selectBizDeviceList: 按设备名称筛选")
    void selectBizDeviceList_withNameFilter_returnsFilteredList() {
        List<BizDevice> mockList = Arrays.asList(testDevice);
        when(bizDeviceMapper.selectBizDeviceList(any(BizDevice.class))).thenReturn(mockList);

        BizDevice filter = new BizDevice();
        filter.setDeviceName("测试设备");
        List<BizDevice> result = bizDeviceService.selectBizDeviceList(filter);

        assertEquals(1, result.size());
        assertEquals("测试设备", result.get(0).getDeviceName());
    }

    @Test
    @DisplayName("selectBizDeviceList: 无数据时返回空列表")
    void selectBizDeviceList_noData_returnsEmptyList() {
        when(bizDeviceMapper.selectBizDeviceList(any(BizDevice.class))).thenReturn(Arrays.asList());

        List<BizDevice> result = bizDeviceService.selectBizDeviceList(new BizDevice());

        assertTrue(result.isEmpty());
    }

    // ===== insertBizDevice =====

    @Test
    @DisplayName("insertBizDevice: 成功插入设备")
    void insertBizDevice_validDevice_returns1() {
        when(bizDeviceMapper.insertBizDevice(any(BizDevice.class))).thenReturn(1);

        BizDevice newDevice = new BizDevice();
        newDevice.setDeviceName("新设备");
        newDevice.setDeviceSn("SN-NEW");
        newDevice.setDeviceType("考勤机");

        int result = bizDeviceService.insertBizDevice(newDevice);

        assertEquals(1, result);
        assertNotNull(newDevice.getCreateTime());
        verify(bizDeviceMapper, times(1)).insertBizDevice(any(BizDevice.class));
    }

    @Test
    @DisplayName("insertBizDevice: 插入失败返回0")
    void insertBizDevice_insertFails_returns0() {
        when(bizDeviceMapper.insertBizDevice(any(BizDevice.class))).thenReturn(0);

        BizDevice newDevice = new BizDevice();
        newDevice.setDeviceName("失败设备");

        int result = bizDeviceService.insertBizDevice(newDevice);

        assertEquals(0, result);
    }

    // ===== updateBizDevice =====

    @Test
    @DisplayName("updateBizDevice: 成功更新设备")
    void updateBizDevice_validDevice_returns1() {
        when(bizDeviceMapper.updateBizDevice(any(BizDevice.class))).thenReturn(1);

        testDevice.setDeviceName("更新后的设备名");
        int result = bizDeviceService.updateBizDevice(testDevice);

        assertEquals(1, result);
        assertNotNull(testDevice.getUpdateTime());
        verify(bizDeviceMapper, times(1)).updateBizDevice(any(BizDevice.class));
    }

    // ===== deleteBizDeviceByDeviceIds =====

    @Test
    @DisplayName("deleteBizDeviceByDeviceIds: 批量删除成功")
    void deleteBizDeviceByDeviceIds_validIds_returnsDeleteCount() {
        Long[] deviceIds = {1L, 2L, 3L};
        when(bizDeviceMapper.deleteBizDeviceByDeviceIds(deviceIds)).thenReturn(3);

        int result = bizDeviceService.deleteBizDeviceByDeviceIds(deviceIds);

        assertEquals(3, result);
        verify(bizDeviceMapper, times(1)).deleteBizDeviceByDeviceIds(deviceIds);
    }

    @Test
    @DisplayName("deleteBizDeviceByDeviceIds: 空数组返回0")
    void deleteBizDeviceByDeviceIds_emptyArray_returns0() {
        when(bizDeviceMapper.deleteBizDeviceByDeviceIds(new Long[]{})).thenReturn(0);

        int result = bizDeviceService.deleteBizDeviceByDeviceIds(new Long[]{});

        assertEquals(0, result);
    }

    // ===== deleteBizDeviceByDeviceId =====

    @Test
    @DisplayName("deleteBizDeviceByDeviceId: 单个删除成功")
    void deleteBizDeviceByDeviceId_validId_returns1() {
        when(bizDeviceMapper.deleteBizDeviceByDeviceId(1L)).thenReturn(1);

        int result = bizDeviceService.deleteBizDeviceByDeviceId(1L);

        assertEquals(1, result);
        verify(bizDeviceMapper, times(1)).deleteBizDeviceByDeviceId(1L);
    }
}
