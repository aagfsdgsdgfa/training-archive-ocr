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

import com.ruoyi.biz.domain.BizTrainLocation;
import com.ruoyi.biz.service.impl.BizTrainLocationServiceImpl;

/**
 * BizTrainLocationService 单元测试
 *
 * 测试范围：培训地点与会议室的CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class BizTrainLocationServiceTest {

    @Mock
    private com.ruoyi.biz.mapper.BizTrainLocationMapper bizTrainLocationMapper;

    @InjectMocks
    private BizTrainLocationServiceImpl bizTrainLocationService;

    private BizTrainLocation testLocation;

    @BeforeEach
    void setUp() {
        testLocation = new BizTrainLocation();
        testLocation.setLocationId(1L);
        testLocation.setLocationName("会议室A");
        testLocation.setCapacity(50);
    }

    @Test
    @DisplayName("selectBizTrainLocationByLocationId: 地点存在时返回对象")
    void selectBizTrainLocationByLocationId_存在_返回对象() {
        when(bizTrainLocationMapper.selectBizTrainLocationByLocationId(1L)).thenReturn(testLocation);

        BizTrainLocation result = bizTrainLocationService.selectBizTrainLocationByLocationId(1L);

        assertNotNull(result);
        assertEquals("会议室A", result.getLocationName());
    }

    @Test
    @DisplayName("selectBizTrainLocationByLocationId: 地点不存在时返回null")
    void selectBizTrainLocationByLocationId_不存在_返回null() {
        when(bizTrainLocationMapper.selectBizTrainLocationByLocationId(999L)).thenReturn(null);

        BizTrainLocation result = bizTrainLocationService.selectBizTrainLocationByLocationId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("selectBizTrainLocationList: 返回地点列表")
    void selectBizTrainLocationList_返回列表() {
        List<BizTrainLocation> mockList = Arrays.asList(testLocation);
        when(bizTrainLocationMapper.selectBizTrainLocationList(any())).thenReturn(mockList);

        List<BizTrainLocation> result = bizTrainLocationService.selectBizTrainLocationList(new BizTrainLocation());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectBizTrainLocationList: 按名称筛选")
    void selectBizTrainLocationList_按名称筛选() {
        when(bizTrainLocationMapper.selectBizTrainLocationList(any())).thenReturn(Arrays.asList(testLocation));

        BizTrainLocation query = new BizTrainLocation();
        query.setLocationName("会议");
        List<BizTrainLocation> result = bizTrainLocationService.selectBizTrainLocationList(query);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("insertBizTrainLocation: 成功插入")
    void insertBizTrainLocation_成功() {
        when(bizTrainLocationMapper.insertBizTrainLocation(any())).thenReturn(1);

        BizTrainLocation newLocation = new BizTrainLocation();
        newLocation.setLocationName("新会议室");
        int result = bizTrainLocationService.insertBizTrainLocation(newLocation);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateBizTrainLocation: 成功更新")
    void updateBizTrainLocation_成功() {
        when(bizTrainLocationMapper.updateBizTrainLocation(any())).thenReturn(1);

        testLocation.setCapacity(100);
        int result = bizTrainLocationService.updateBizTrainLocation(testLocation);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("deleteBizTrainLocationByLocationIds: 批量删除成功")
    void deleteBizTrainLocationByLocationIds_成功() {
        when(bizTrainLocationMapper.deleteBizTrainLocationByLocationIds(any())).thenReturn(3);

        int result = bizTrainLocationService.deleteBizTrainLocationByLocationIds(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("deleteBizTrainLocationByLocationId: 单个删除成功")
    void deleteBizTrainLocationByLocationId_成功() {
        when(bizTrainLocationMapper.deleteBizTrainLocationByLocationId(1L)).thenReturn(1);

        int result = bizTrainLocationService.deleteBizTrainLocationByLocationId(1L);

        assertEquals(1, result);
    }
}
