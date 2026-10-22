package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 年度培训计划Mapper接口
 * 
 * @author admin
 * @date 2026-03-03
 */
@Mapper
public interface BizTrainPlanMapper 
{
    /**
     * 查询年度培训计划
     * 
     * @param planId 年度培训计划主键
     * @return 年度培训计划
     */
    public BizTrainPlan selectBizTrainPlanByPlanId(Long planId);

    /**
     * 查询年度培训计划列表
     * 
     * @param bizTrainPlan 年度培训计划
     * @return 年度培训计划集合
     */
    public List<BizTrainPlan> selectBizTrainPlanList(BizTrainPlan bizTrainPlan);

    /**
     * 新增年度培训计划
     * 
     * @param bizTrainPlan 年度培训计划
     * @return 结果
     */
    public int insertBizTrainPlan(BizTrainPlan bizTrainPlan);

    /**
     * 修改年度培训计划
     * 
     * @param bizTrainPlan 年度培训计划
     * @return 结果
     */
    public int updateBizTrainPlan(BizTrainPlan bizTrainPlan);

    /**
     * 删除年度培训计划
     * 
     * @param planId 年度培训计划主键
     * @return 结果
     */
    public int deleteBizTrainPlanByPlanId(Long planId);

    /**
     * 批量删除年度培训计划
     * 
     * @param planIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTrainPlanByPlanIds(Long[] planIds);
}
