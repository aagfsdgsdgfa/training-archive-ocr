package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainOcrBatch;
import com.ruoyi.biz.domain.BizTrainOcrResult;
import com.ruoyi.biz.domain.vo.OcrBatchUploadReq;

/**
 * OCR扫描批次主Service接口
 * 
 * @author admin
 * @date 2026-03-04
 */
public interface IBizTrainOcrBatchService
{
    /**
     * 确认并生成考勤台账
     */
    void confirmAndGenerate(Long batchId, List<BizTrainOcrResult> editedList);

    /**
     * 查询OCR扫描批次主
     * 
     * @param batchId OCR扫描批次主主键
     * @return OCR扫描批次主
     */
    public BizTrainOcrBatch selectBizTrainOcrBatchByBatchId(Long batchId);

    /**
     * 查询OCR扫描批次主列表
     * 
     * @param bizTrainOcrBatch OCR扫描批次主
     * @return OCR扫描批次主集合
     */
    public List<BizTrainOcrBatch> selectBizTrainOcrBatchList(BizTrainOcrBatch bizTrainOcrBatch);

    /**
     * 新增OCR扫描批次主
     * 
     * @param bizTrainOcrBatch OCR扫描批次主
     * @return 结果
     */
    public int insertBizTrainOcrBatch(BizTrainOcrBatch bizTrainOcrBatch);

    /**
     * 修改OCR扫描批次主
     * 
     * @param bizTrainOcrBatch OCR扫描批次主
     * @return 结果
     */
    public int updateBizTrainOcrBatch(BizTrainOcrBatch bizTrainOcrBatch);

    /**
     * 批量删除OCR扫描批次主
     * 
     * @param batchIds 需要删除的OCR扫描批次主主键集合
     * @return 结果
     */
    public int deleteBizTrainOcrBatchByBatchIds(Long[] batchIds);

    /**
     * 删除OCR扫描批次主信息
     * 
     * @param batchId OCR扫描批次主主键
     * @return 结果
     */
    public int deleteBizTrainOcrBatchByBatchId(Long batchId);

    /**
     * 处理高拍仪批量上传
     */
    Long uploadAndCreateBatch(OcrBatchUploadReq req);

    /**
     * 基于源文件重新 OCR 提取元数据
     * 用于历史批次补提应到/实到/培训日期等字段
     *
     * @param batchId 批次ID
     * @return 1=成功 0=失败（批次不存在/无源文件/非待校对状态）
     */
    int reextractBatch(Long batchId);
}
