package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainOcrLog;

/**
 * OCR防重与拦截记录Service接口
 * 
 * @author admin
 * @date 2026-03-04
 */
public interface IBizTrainOcrLogService 
{
    /**
     * 查询OCR防重与拦截记录
     * 
     * @param logId OCR防重与拦截记录主键
     * @return OCR防重与拦截记录
     */
    public BizTrainOcrLog selectBizTrainOcrLogByLogId(Long logId);

    /**
     * 查询OCR防重与拦截记录列表
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return OCR防重与拦截记录集合
     */
    public List<BizTrainOcrLog> selectBizTrainOcrLogList(BizTrainOcrLog bizTrainOcrLog);

    /**
     * 新增OCR防重与拦截记录
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return 结果
     */
    public int insertBizTrainOcrLog(BizTrainOcrLog bizTrainOcrLog);

    /**
     * 修改OCR防重与拦截记录
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return 结果
     */
    public int updateBizTrainOcrLog(BizTrainOcrLog bizTrainOcrLog);

    /**
     * 批量删除OCR防重与拦截记录
     * 
     * @param logIds 需要删除的OCR防重与拦截记录主键集合
     * @return 结果
     */
    public int deleteBizTrainOcrLogByLogIds(Long[] logIds);

    /**
     * 删除OCR防重与拦截记录信息
     * 
     * @param logId OCR防重与拦截记录主键
     * @return 结果
     */
    public int deleteBizTrainOcrLogByLogId(Long logId);
}
