package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysUserHistory;

/**
 * 员工信息历史Mapper接口
 *
 * @author admin
 */
public interface SysUserHistoryMapper
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
     * 关闭员工旧的历史记录（设置end_date）
     *
     * @param userId 员工ID
     * @param endDate 失效时间
     * @return 结果
     */
    public int closeUserHistoryByUserId(@Param("userId") Long userId, @Param("endDate") Date endDate);

    /**
     * 查询员工在某时间点的有效历史记录
     *
     * @param userId 员工ID
     * @param targetDate 目标时间
     * @return 历史记录
     */
    public SysUserHistory selectUserSnapshot(@Param("userId") Long userId, @Param("targetDate") Date targetDate);
}
