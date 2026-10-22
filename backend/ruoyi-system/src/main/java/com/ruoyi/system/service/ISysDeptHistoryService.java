package com.ruoyi.system.service;

import com.ruoyi.system.domain.SysDeptHistory;
import java.util.Date;
import java.util.List;

/**
 * 部门信息历史Service接口
 *
 * @author admin
 */
public interface ISysDeptHistoryService
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
     * 查询部门在某时间点的有效历史记录（快照）
     *
     * @param deptId 部门ID
     * @param targetDate 目标时间
     * @return 历史记录
     */
    public SysDeptHistory selectDeptSnapshot(Long deptId, Date targetDate);

    /**
     * 初始化部门历史记录（将当前sys_dept数据写入历史表）
     *
     * @param deptId 部门ID
     * @param deptName 部门名称
     * @param startDate 生效时间
     * @return 结果
     */
    public int initDeptHistory(Long deptId, String deptName, Date startDate);

    /**
     * 记录部门变更（关闭旧记录，插入新记录）
     *
     * @param deptId 部门ID
     * @param deptName 新部门名称
     * @param changeDate 变更时间
     * @return 结果
     */
    public int recordDeptChange(Long deptId, String deptName, Date changeDate);
}
