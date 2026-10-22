package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainAttendanceMapper;
import com.ruoyi.biz.domain.BizTrainAttendance;
import com.ruoyi.biz.service.IBizTrainAttendanceService;

/**
 * 线上考勤与申诉明细Service业务层处理
 * 
 * @author admin
 * @date 2026-03-03
 */
@Service
public class BizTrainAttendanceServiceImpl implements IBizTrainAttendanceService 
{
    @Autowired
    private BizTrainAttendanceMapper bizTrainAttendanceMapper;

    /**
     * 查询线上考勤与申诉明细
     * 
     * @param recordId 线上考勤与申诉明细主键
     * @return 线上考勤与申诉明细
     */
    @Override
    public BizTrainAttendance selectBizTrainAttendanceByRecordId(Long recordId)
    {
        return bizTrainAttendanceMapper.selectBizTrainAttendanceByRecordId(recordId);
    }

    /**
     * 查询线上考勤与申诉明细列表
     * 
     * @param bizTrainAttendance 线上考勤与申诉明细
     * @return 线上考勤与申诉明细
     */
    @Override
    public List<BizTrainAttendance> selectBizTrainAttendanceList(BizTrainAttendance bizTrainAttendance)
    {
        return bizTrainAttendanceMapper.selectBizTrainAttendanceList(bizTrainAttendance);
    }

    /**
     * 新增线上考勤与申诉明细
     * 
     * @param bizTrainAttendance 线上考勤与申诉明细
     * @return 结果
     */
    @Override
    public int insertBizTrainAttendance(BizTrainAttendance bizTrainAttendance)
    {
        bizTrainAttendance.setCreateTime(DateUtils.getNowDate());
        return bizTrainAttendanceMapper.insertBizTrainAttendance(bizTrainAttendance);
    }

    /**
     * 修改线上考勤与申诉明细
     * 
     * @param bizTrainAttendance 线上考勤与申诉明细
     * @return 结果
     */
    @Override
    public int updateBizTrainAttendance(BizTrainAttendance bizTrainAttendance)
    {
        bizTrainAttendance.setUpdateTime(DateUtils.getNowDate());
        return bizTrainAttendanceMapper.updateBizTrainAttendance(bizTrainAttendance);
    }

    /**
     * 批量删除线上考勤与申诉明细
     * 
     * @param recordIds 需要删除的线上考勤与申诉明细主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainAttendanceByRecordIds(Long[] recordIds)
    {
        return bizTrainAttendanceMapper.deleteBizTrainAttendanceByRecordIds(recordIds);
    }

    /**
     * 删除线上考勤与申诉明细信息
     * 
     * @param recordId 线上考勤与申诉明细主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainAttendanceByRecordId(Long recordId)
    {
        return bizTrainAttendanceMapper.deleteBizTrainAttendanceByRecordId(recordId);
    }
}
