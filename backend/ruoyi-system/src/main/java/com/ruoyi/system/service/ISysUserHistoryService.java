package com.ruoyi.system.service;

import com.ruoyi.system.domain.SysUserHistory;
import java.util.Date;
import java.util.List;

/**
 * 员工信息历史Service接口
 *
 * @author admin
 */
public interface ISysUserHistoryService
{
    /**
     * 查询员工历史记录
     *
     * @param historyId 历史记录ID
     * @return 员工历史记录
     */
    public SysUserHistory selectSysUserHistoryById(Long historyId);

    /**
     * 查询员工历史记录列表
     *
     * @param sysUserHistory 员工历史记录
     * @return 员工历史记录集合
     */
    public List<SysUserHistory> selectSysUserHistoryList(SysUserHistory sysUserHistory);

    /**
     * 新增员工历史记录
     *
     * @param sysUserHistory 员工历史记录
     * @return 结果
     */
    public int insertSysUserHistory(SysUserHistory sysUserHistory);

    /**
     * 修改员工历史记录
     *
     * @param sysUserHistory 员工历史记录
     * @return 结果
     */
    public int updateSysUserHistory(SysUserHistory sysUserHistory);

    /**
     * 删除员工历史记录
     *
     * @param historyId 历史记录ID
     * @return 结果
     */
    public int deleteSysUserHistoryById(Long historyId);

    /**
     * 批量删除员工历史记录
     *
     * @param historyIds 需要删除的历史记录ID
     * @return 结果
     */
    public int deleteSysUserHistoryByIds(Long[] historyIds);

    /**
     * 查询员工在某时间点的有效历史记录（快照）
     *
     * @param userId 员工ID
     * @param targetDate 目标时间
     * @return 历史记录
     */
    public SysUserHistory selectUserSnapshot(Long userId, Date targetDate);

    /**
     * 初始化员工历史记录（将当前sys_user数据写入历史表）
     *
     * @param userId 员工ID
     * @param userName 姓名
     * @param deptId 部门ID
     * @param deptName 部门名称
     * @param postId 岗位ID
     * @param postName 岗位名称
     * @param startDate 生效时间
     * @return 结果
     */
    public int initUserHistory(Long userId, String userName, Long deptId, String deptName, Long postId, String postName, Date startDate);

    /**
     * 记录员工变更（关闭旧记录，插入新记录）
     *
     * @param userId 员工ID
     * @param userName 新姓名
     * @param deptId 新部门ID
     * @param deptName 新部门名称
     * @param postId 新岗位ID
     * @param postName 新岗位名称
     * @param changeDate 变更时间
     * @return 结果
     */
    public int recordUserChange(Long userId, String userName, Long deptId, String deptName, Long postId, String postName, Date changeDate);
}
