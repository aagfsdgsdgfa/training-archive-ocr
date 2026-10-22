package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.SysUserHistory;
import com.ruoyi.system.mapper.SysUserHistoryMapper;
import com.ruoyi.system.service.ISysUserHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 员工信息历史Service实现
 *
 * @author admin
 */
@Service
public class SysUserHistoryServiceImpl implements ISysUserHistoryService
{
    @Autowired
    private SysUserHistoryMapper sysUserHistoryMapper;

    @Override
    public SysUserHistory selectSysUserHistoryById(Long historyId)
    {
        return sysUserHistoryMapper.selectSysUserHistoryById(historyId);
    }

    @Override
    public List<SysUserHistory> selectSysUserHistoryList(SysUserHistory sysUserHistory)
    {
        return sysUserHistoryMapper.selectSysUserHistoryList(sysUserHistory);
    }

    @Override
    public int insertSysUserHistory(SysUserHistory sysUserHistory)
    {
        return sysUserHistoryMapper.insertSysUserHistory(sysUserHistory);
    }

    @Override
    public int updateSysUserHistory(SysUserHistory sysUserHistory)
    {
        return sysUserHistoryMapper.updateSysUserHistory(sysUserHistory);
    }

    @Override
    public int deleteSysUserHistoryById(Long historyId)
    {
        return sysUserHistoryMapper.deleteSysUserHistoryById(historyId);
    }

    @Override
    public int deleteSysUserHistoryByIds(Long[] historyIds)
    {
        return sysUserHistoryMapper.deleteSysUserHistoryByIds(historyIds);
    }

    @Override
    public SysUserHistory selectUserSnapshot(Long userId, Date targetDate)
    {
        return sysUserHistoryMapper.selectUserSnapshot(userId, targetDate);
    }

    @Override
    @Transactional
    public int initUserHistory(Long userId, String userName, Long deptId, String deptName, Long postId, String postName, Date startDate)
    {
        SysUserHistory history = new SysUserHistory();
        history.setUserId(userId);
        history.setUserName(userName);
        history.setDeptId(deptId);
        history.setDeptName(deptName);
        history.setPostId(postId);
        history.setPostName(postName);
        history.setStartDate(startDate);
        // endDate = null 表示当前有效
        return sysUserHistoryMapper.insertSysUserHistory(history);
    }

    @Override
    @Transactional
    public int recordUserChange(Long userId, String userName, Long deptId, String deptName, Long postId, String postName, Date changeDate)
    {
        // 1. 关闭该员工的所有旧记录
        sysUserHistoryMapper.closeUserHistoryByUserId(userId, changeDate);

        // 2. 插入新记录
        SysUserHistory newHistory = new SysUserHistory();
        newHistory.setUserId(userId);
        newHistory.setUserName(userName);
        newHistory.setDeptId(deptId);
        newHistory.setDeptName(deptName);
        newHistory.setPostId(postId);
        newHistory.setPostName(postName);
        newHistory.setStartDate(changeDate);
        // endDate = null 表示当前有效
        return sysUserHistoryMapper.insertSysUserHistory(newHistory);
    }
}
