package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysDeptHistory;

/**
 * 部门信息历史Mapper接口
 *
 * @author admin
 */
public interface SysDeptHistoryMapper
{
    /**
     * 查询部门历史记录
     *
     * @param historyId 历史记录ID
     * @return 部门历史记录
     */
    public SysDeptHistory selectSysDeptHistoryById(Long historyId);

    /**
     * 查询部门历史记录列表
     *
     * @param sysDeptHistory 部门历史记录
     * @return 部门历史记录集合
     */
    public List<SysDeptHistory> selectSysDeptHistoryList(SysDeptHistory sysDeptHistory);

    /**
     * 新增部门历史记录
     *
     * @param sysDeptHistory 部门历史记录
     * @return 结果
     */
    public int insertSysDeptHistory(SysDeptHistory sysDeptHistory);

    /**
     * 修改部门历史记录
     *
     * @param sysDeptHistory 部门历史记录
     * @return 结果
     */
    public int updateSysDeptHistory(SysDeptHistory sysDeptHistory);

    /**
     * 删除部门历史记录
     *
     * @param historyId 历史记录ID
     * @return 结果
     */
    public int deleteSysDeptHistoryById(Long historyId);

    /**
     * 批量删除部门历史记录
     *
     * @param historyIds 需要删除的历史记录ID
     * @return 结果
     */
    public int deleteSysDeptHistoryByIds(Long[] historyIds);

    /**
     * 关闭部门旧的历史记录（设置end_date）
     *
     * @param deptId 部门ID
     * @param endDate 失效时间
     * @return 结果
     */
    public int closeDeptHistoryByDeptId(@Param("deptId") Long deptId, @Param("endDate") Date endDate);

    /**
     * 查询部门在某时间点的有效历史记录
     *
     * @param deptId 部门ID
     * @param targetDate 目标时间
     * @return 历史记录
     */
    public SysDeptHistory selectDeptSnapshot(@Param("deptId") Long deptId, @Param("targetDate") Date targetDate);
}
