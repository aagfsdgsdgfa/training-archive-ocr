package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainOcrBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * OCR扫描批次主Mapper接口
 * 
 * @author admin
 * @date 2026-03-04
 */
@Mapper
public interface BizTrainOcrBatchMapper 
{
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
     * 查询当天未结算的同 planId 批次（高拍仪分次上传自动追加时复用）
     * @param bizTrainOcrBatch 查询条件（只用 planId）
     * @return 命中按 create_time desc 排序的 batch 列表
     */
    public List<BizTrainOcrBatch> selectOpenBatchByKey(BizTrainOcrBatch bizTrainOcrBatch);

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
     * 删除OCR扫描批次主
     * 
     * @param batchId OCR扫描批次主主键
     * @return 结果
     */
    public int deleteBizTrainOcrBatchByBatchId(Long batchId);

    /**
     * 批量删除OCR扫描批次主
     * 
     * @param batchIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTrainOcrBatchByBatchIds(Long[] batchIds);
}
