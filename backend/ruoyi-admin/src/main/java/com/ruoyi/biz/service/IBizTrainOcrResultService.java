package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainOcrResult;

/**
 * OCR识别结果暂存明细Service接口
 * 
 * @author admin
 * @date 2026-03-04
 */
public interface IBizTrainOcrResultService 
{
    /**
     * 查询OCR识别结果暂存明细
     * 
     * @param resultId OCR识别结果暂存明细主键
     * @return OCR识别结果暂存明细
     */
    public BizTrainOcrResult selectBizTrainOcrResultByResultId(Long resultId);

    /**
     * 查询OCR识别结果暂存明细列表
     * 
     * @param bizTrainOcrResult OCR识别结果暂存明细
     * @return OCR识别结果暂存明细集合
     */
    public List<BizTrainOcrResult> selectBizTrainOcrResultList(BizTrainOcrResult bizTrainOcrResult);

    /**
     * 新增OCR识别结果暂存明细
     * 
     * @param bizTrainOcrResult OCR识别结果暂存明细
     * @return 结果
     */
    public int insertBizTrainOcrResult(BizTrainOcrResult bizTrainOcrResult);

    /**
     * 修改OCR识别结果暂存明细
     * 
     * @param bizTrainOcrResult OCR识别结果暂存明细
     * @return 结果
     */
    public int updateBizTrainOcrResult(BizTrainOcrResult bizTrainOcrResult);

    /**
     * 批量删除OCR识别结果暂存明细
     * 
     * @param resultIds 需要删除的OCR识别结果暂存明细主键集合
     * @return 结果
     */
    public int deleteBizTrainOcrResultByResultIds(Long[] resultIds);

    /**
     * 删除OCR识别结果暂存明细信息
     *
     * @param resultId OCR识别结果暂存明细主键
     * @return 结果
     */
    public int deleteBizTrainOcrResultByResultId(Long resultId);

    /**
     * 按批次ID删除OCR识别结果暂存明细
     * 用于 Excel 重传时的全量替换语义,避免同一批次内 row 数翻倍
     *
     * @param batchId 批次ID
     * @return 删除条数
     */
    public int deleteBizTrainOcrResultByBatchId(Long batchId);
}
