package com.ruoyi.biz.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.ruoyi.biz.util.SerialNoGenerator;
import com.ruoyi.biz.util.SerialNoResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.mapper.BizTrainLedgerMapper;
import com.ruoyi.biz.domain.BizTrainLedger;
import com.ruoyi.biz.service.IBizTrainLedgerService;

/**
 * 培训台账总Service业务层处理
 *
 * @author admin
 * @date 2026-03-03
 */
@Service
public class BizTrainLedgerServiceImpl implements IBizTrainLedgerService
{
    private static final String SERIAL_BIZ_TYPE = "biz_train_ledger";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String RANGE_DELIMITER = " — ";

    @Autowired
    private BizTrainLedgerMapper bizTrainLedgerMapper;

    @Autowired
    private SerialNoGenerator serialNoGenerator;

    /**
     * 查询培训台账总
     *
     * @param ledgerId 培训台账总主键
     * @return 培训台账总
     */
    @Override
    public BizTrainLedger selectBizTrainLedgerByLedgerId(Long ledgerId)
    {
        return bizTrainLedgerMapper.selectBizTrainLedgerByLedgerId(ledgerId);
    }

    /**
     * 查询培训台账总列表
     *
     * @param bizTrainLedger 培训台账总
     * @return 培训台账总
     */
    @Override
    public List<BizTrainLedger> selectBizTrainLedgerList(BizTrainLedger bizTrainLedger)
    {
        return bizTrainLedgerMapper.selectBizTrainLedgerList(bizTrainLedger);
    }

    /**
     * 新增培训台账总
     * 自动生成业务流水号(基于培训日期,每天重置)
     *
     * @param bizTrainLedger 培训台账总
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBizTrainLedger(BizTrainLedger bizTrainLedger)
    {
        bizTrainLedger.setCreateTime(DateUtils.getNowDate());
        if (bizTrainLedger.getSerialNo() == null || bizTrainLedger.getSerialNo().isEmpty())
        {
            SerialNoResult sn = serialNoGenerator.generate(SERIAL_BIZ_TYPE, bizTrainLedger.getTrainDate());
            bizTrainLedger.setSerialNo(sn.getSerialNo());
            bizTrainLedger.setSerialDate(sn.getSerialDate());
        }
        return bizTrainLedgerMapper.insertBizTrainLedger(bizTrainLedger);
    }

    /**
     * 修改培训台账总
     *
     * 人工校正 train_date 后,根据新的 train_date 同步重算 serial_date,
     * 否则列表 ORDER BY serial_date desc 仍按原 serial_date 排,
     * 校正后的行会停留在原位置上,看起来"排序错乱"。
     * serialNo(业务流水号)保留原值,便于追溯创建时间和外部引用。
     *
     * @param bizTrainLedger 培训台账总
     * @return 结果
     */
    @Override
    public int updateBizTrainLedger(BizTrainLedger bizTrainLedger)
    {
        bizTrainLedger.setUpdateTime(DateUtils.getNowDate());
        String trainDate = bizTrainLedger.getTrainDate();
        if (StringUtils.isNotEmpty(trainDate))
        {
            try
            {
                String start = trainDate.split(RANGE_DELIMITER)[0].trim();
                LocalDate parsed = LocalDate.parse(start, DATE_FMT);
                bizTrainLedger.setSerialDate(parsed);
            }
            catch (Exception ignore)
            {
                // trainDate 不是合法 yyyy-MM-dd(也非区间起始),保留原 serial_date
            }
        }
        return bizTrainLedgerMapper.updateBizTrainLedger(bizTrainLedger);
    }

    /**
     * 批量删除培训台账总
     * 
     * @param ledgerIds 需要删除的培训台账总主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainLedgerByLedgerIds(Long[] ledgerIds)
    {
        return bizTrainLedgerMapper.deleteBizTrainLedgerByLedgerIds(ledgerIds);
    }

    /**
     * 删除培训台账总信息
     * 
     * @param ledgerId 培训台账总主键
     * @return 结果
     */
    @Override
    public int deleteBizTrainLedgerByLedgerId(Long ledgerId)
    {
        return bizTrainLedgerMapper.deleteBizTrainLedgerByLedgerId(ledgerId);
    }
}
