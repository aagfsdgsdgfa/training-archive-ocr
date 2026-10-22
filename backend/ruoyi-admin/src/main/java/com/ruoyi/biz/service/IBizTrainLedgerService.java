package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainLedger;

/**
 * 培训台账总Service接口
 * 
 * @author admin
 * @date 2026-03-03
 */
public interface IBizTrainLedgerService 
{
    /**
     * 查询培训台账总
     * 
     * @param ledgerId 培训台账总主键
     * @return 培训台账总
     */
    public BizTrainLedger selectBizTrainLedgerByLedgerId(Long ledgerId);

    /**
     * 查询培训台账总列表
     * 
     * @param bizTrainLedger 培训台账总
     * @return 培训台账总集合
     */
    public List<BizTrainLedger> selectBizTrainLedgerList(BizTrainLedger bizTrainLedger);

    /**
     * 新增培训台账总
     * 
     * @param bizTrainLedger 培训台账总
     * @return 结果
     */
    public int insertBizTrainLedger(BizTrainLedger bizTrainLedger);

    /**
     * 修改培训台账总
     * 
     * @param bizTrainLedger 培训台账总
     * @return 结果
     */
    public int updateBizTrainLedger(BizTrainLedger bizTrainLedger);

    /**
     * 批量删除培训台账总
     * 
     * @param ledgerIds 需要删除的培训台账总主键集合
     * @return 结果
     */
    public int deleteBizTrainLedgerByLedgerIds(Long[] ledgerIds);

    /**
     * 删除培训台账总信息
     * 
     * @param ledgerId 培训台账总主键
     * @return 结果
     */
    public int deleteBizTrainLedgerByLedgerId(Long ledgerId);
}
