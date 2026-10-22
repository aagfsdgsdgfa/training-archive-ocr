package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.SysDeptHistory;
import com.ruoyi.system.mapper.SysDeptHistoryMapper;
import com.ruoyi.system.service.ISysDeptHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 部门信息历史Service实现
 *
 * @author admin
 */
@Service
public class SysDeptHistoryServiceImpl implements ISysDeptHistoryService
{
    @Autowired
    private SysDeptHistoryMapper sysDeptHistoryMapper;

    @Override
    public SysDeptHistory selectSysDeptHistoryById(Long historyId)
    {
        return sysDeptHistoryMapper.selectSysDeptHistoryById(historyId);
    }

    @Override
    public List<SysDeptHistory> selectSysDeptHistoryList(SysDeptHistory sysDeptHistory)
    {
        return sysDeptHistoryMapper.selectSysDeptHistoryList(sysDeptHistory);
    }

    @Override
    public int insertSysDeptHistory(SysDeptHistory sysDeptHistory)
    {
        return sysDeptHistoryMapper.insertSysDeptHistory(sysDeptHistory);
    }

    @Override
    public int updateSysDeptHistory(SysDeptHistory sysDeptHistory)
    {
        return sysDeptHistoryMapper.updateSysDeptHistory(sysDeptHistory);
    }

    @Override
    public int deleteSysDeptHistoryById(Long historyId)
    {
        return sysDeptHistoryMapper.deleteSysDeptHistoryById(historyId);
    }

    @Override
    public int deleteSysDeptHistoryByIds(Long[] historyIds)
    {
        return sysDeptHistoryMapper.deleteSysDeptHistoryByIds(historyIds);
    }

    @Override
    public SysDeptHistory selectDeptSnapshot(Long deptId, Date targetDate)
    {
        return sysDeptHistoryMapper.selectDeptSnapshot(deptId, targetDate);
    }

    @Override
    @Transactional
    public int initDeptHistory(Long deptId, String deptName, Date startDate)
    {
        SysDeptHistory history = new SysDeptHistory();
        history.setDeptId(deptId);
        history.setDeptName(deptName);
        history.setStartDate(startDate);
        // endDate = null 表示当前有效
        return sysDeptHistoryMapper.insertSysDeptHistory(history);
    }

    @Override
    @Transactional
    public int recordDeptChange(Long deptId, String deptName, Date changeDate)
    {
        // 1. 关闭该部门的所有旧记录
        sysDeptHistoryMapper.closeDeptHistoryByDeptId(deptId, changeDate);

        // 2. 插入新记录
        SysDeptHistory newHistory = new SysDeptHistory();
        newHistory.setDeptId(deptId);
        newHistory.setDeptName(deptName);
        newHistory.setStartDate(changeDate);
        // endDate = null 表示当前有效
        return sysDeptHistoryMapper.insertSysDeptHistory(newHistory);
    }
}
