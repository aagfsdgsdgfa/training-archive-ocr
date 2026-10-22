package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.BizTrainOcrResultMapper;
import com.ruoyi.biz.domain.BizTrainOcrResult;
import com.ruoyi.biz.service.IBizTrainOcrResultService;

/**
 * OCR识别结果暂存明细Service业务层处理
 * 
 * @author admin
 * @date 2026-03-04
 */
@Service
public class BizTrainOcrResultServiceImpl implements IBizTrainOcrResultService 
{
    @Autowired
    private BizTrainOcrResultMapper bizTrainOcrResultMapper;

    @Autowired
    private com.ruoyi.biz.mapper.BizTrainOcrBatchMapper bizTrainOcrBatchMapper;

    @Autowired
    private com.ruoyi.biz.mapper.BizTrainArchiveMapper bizTrainArchiveMapper;

    /**
     * 查询OCR识别结果暂存明细
     * 
     * @param resultId OCR识别结果暂存明细主键
     * @return OCR识别结果暂存明细
     */
    @Override
    public BizTrainOcrResult selectBizTrainOcrResultByResultId(Long resultId)
    {
        return bizTrainOcrResultMapper.selectBizTrainOcrResultByResultId(resultId);
    }

    /**
     * 查询OCR识别结果暂存明细列表
     * 
     * @param bizTrainOcrResult OCR识别结果暂存明细
     * @return OCR识别结果暂存明细
     */
    @Override
    public List<BizTrainOcrResult> selectBizTrainOcrResultList(BizTrainOcrResult bizTrainOcrResult)
    {
        return bizTrainOcrResultMapper.selectBizTrainOcrResultList(bizTrainOcrResult);
    }

    /**
     * 新增OCR识别结果暂存明细
     * * @param bizTrainOcrResult OCR识别结果暂存明细
     * @return 结果
     */
    @Override
    public int insertBizTrainOcrResult(BizTrainOcrResult bizTrainOcrResult)
    {
        // 1. 获取该数据归属的批次信息，提取“培训主题”和“培训日期”
        com.ruoyi.biz.domain.BizTrainOcrBatch batch = bizTrainOcrBatchMapper.selectBizTrainOcrBatchByBatchId(bizTrainOcrResult.getBatchId());

        if (batch != null && com.ruoyi.common.utils.StringUtils.isNotEmpty(batch.getOcrSubject())
                && com.ruoyi.common.utils.StringUtils.isNotEmpty(batch.getOcrDate())) {

            // 2. 构造查询条件：查个人档案表
            com.ruoyi.biz.domain.BizTrainArchive queryArchive = new com.ruoyi.biz.domain.BizTrainArchive();
            queryArchive.setUserName(bizTrainOcrResult.getParsedName());
            queryArchive.setSubject(batch.getOcrSubject());
            queryArchive.setTrainDate(batch.getOcrDate());

            // 3. 执行查询，判断是否存在相同的培训记录
            java.util.List<com.ruoyi.biz.domain.BizTrainArchive> existList = bizTrainArchiveMapper.selectBizTrainArchiveList(queryArchive);

            if (existList != null && !existList.isEmpty()) {
                // 4. 如果查到记录，说明该员工在这天已经录入过这个培训，直接抛出业务异常阻断
                throw new com.ruoyi.common.exception.ServiceException(
                        "查重拦截：员工 [" + bizTrainOcrResult.getParsedName() +
                                "] 在 [" + batch.getOcrDate() + "] 的 [" + batch.getOcrSubject() +
                                "] 培训档案已存在，请勿重复扫表或上传！"
                );
            }
        }

        // 5. 校验通过，正常执行插入暂存表
        return bizTrainOcrResultMapper.insertBizTrainOcrResult(bizTrainOcrResult);
    }
    /**
     * 修改OCR识别结果暂存明细
     * 
     * @param bizTrainOcrResult OCR识别结果暂存明细
     * @return 结果
     */
    @Override
    public int updateBizTrainOcrResult(BizTrainOcrResult bizTrainOcrResult)
    {
        return bizTrainOcrResultMapper.updateBizTrainOcrResult(bizTrainOcrResult);
    }

    /**
     * 批量删除OCR识别结果暂存明细
     * 
     * @param resultIds 需要删除的OCR识别结果暂存明细主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainOcrResultByResultIds(Long[] resultIds)
    {
        return bizTrainOcrResultMapper.deleteBizTrainOcrResultByResultIds(resultIds);
    }

    /**
     * 删除OCR识别结果暂存明细信息
     *
     * @param resultId OCR识别结果暂存明细主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainOcrResultByResultId(Long resultId)
    {
        return bizTrainOcrResultMapper.deleteBizTrainOcrResultByResultId(resultId);
    }

    /**
     * 按批次ID删除OCR识别结果暂存明细
     */
    @Override
    public int deleteBizTrainOcrResultByBatchId(Long batchId)
    {
        return bizTrainOcrResultMapper.deleteBizTrainOcrResultByBatchId(batchId);
    }
}
