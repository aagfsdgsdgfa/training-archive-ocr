package com.ruoyi.biz.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainOcrLogMapper;
import com.ruoyi.biz.domain.BizTrainOcrLog;
import com.ruoyi.biz.service.IBizTrainOcrLogService;

/**
 * OCR防重与拦截记录Service业务层处理
 * 
 * @author admin
 * @date 2026-03-04
 */
@Service
public class BizTrainOcrLogServiceImpl implements IBizTrainOcrLogService 
{
    @Autowired
    private BizTrainOcrLogMapper bizTrainOcrLogMapper;

    /**
     * 查询OCR防重与拦截记录
     * 
     * @param logId OCR防重与拦截记录主键
     * @return OCR防重与拦截记录
     */
    @Override
    public BizTrainOcrLog selectBizTrainOcrLogByLogId(Long logId)
    {
        return bizTrainOcrLogMapper.selectBizTrainOcrLogByLogId(logId);
    }

    /**
     * 查询OCR防重与拦截记录列表
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return OCR防重与拦截记录
     */
    @Override
    public List<BizTrainOcrLog> selectBizTrainOcrLogList(BizTrainOcrLog bizTrainOcrLog)
    {
        return bizTrainOcrLogMapper.selectBizTrainOcrLogList(bizTrainOcrLog);
    }

    /**
     * 新增OCR防重与拦截记录
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return 结果
     */
    @Override
    public int insertBizTrainOcrLog(BizTrainOcrLog bizTrainOcrLog)
    {
        bizTrainOcrLog.setCreateTime(DateUtils.getNowDate());
        return bizTrainOcrLogMapper.insertBizTrainOcrLog(bizTrainOcrLog);
    }

    /**
     * 修改OCR防重与拦截记录
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return 结果
     */
    @Override
    public int updateBizTrainOcrLog(BizTrainOcrLog bizTrainOcrLog)
    {
        bizTrainOcrLog.setUpdateTime(DateUtils.getNowDate());
        return bizTrainOcrLogMapper.updateBizTrainOcrLog(bizTrainOcrLog);
    }

    /**
     * 批量删除OCR防重与拦截记录
     * 
     * @param logIds 需要删除的OCR防重与拦截记录主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainOcrLogByLogIds(Long[] logIds)
    {
        return bizTrainOcrLogMapper.deleteBizTrainOcrLogByLogIds(logIds);
    }

    /**
     * 删除OCR防重与拦截记录信息
     * 
     * @param logId OCR防重与拦截记录主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainOcrLogByLogId(Long logId)
    {
        return bizTrainOcrLogMapper.deleteBizTrainOcrLogByLogId(logId);
    }
}
