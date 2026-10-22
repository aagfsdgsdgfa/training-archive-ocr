package com.ruoyi.biz.service;

import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Workbook;
import com.ruoyi.biz.domain.BizTrainPlan;

/**
 * 年度培训计划Service接口
 * 
 * @author admin
 * @date 2026-03-03
 */
public interface IBizTrainPlanService 
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
     * 批量删除年度培训计划
     * 
     * @param planIds 需要删除的年度培训计划主键集合
     * @return 结果
     */
    public int deleteBizTrainPlanByPlanIds(Long[] planIds);

    /**
     * 删除年度培训计划信息
     *
     * @param planId 年度培训计划主键
     * @return 结果
     */
    public int deleteBizTrainPlanByPlanId(Long planId);

    /**
     * 从 Workbook 解析年度培训计划（不入库，仅预览）。
     * 兼容 Excel 上传和 OCR 图片识别（图片转 Workbook 后调用）。
     * @param wb 已加载的工作簿
     * @return Map: { plans, companyCount, deptCount }
     */
    public Map<String, Object> previewPlansFromWorkbook(Workbook wb);
}
