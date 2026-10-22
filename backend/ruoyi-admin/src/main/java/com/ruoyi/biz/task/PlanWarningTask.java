package com.ruoyi.biz.task;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;
import com.ruoyi.biz.domain.BizTrainPlan;
import com.ruoyi.biz.domain.BizTrainLedger;
import com.ruoyi.biz.domain.BizTrainOcrBatch;
import com.ruoyi.biz.service.IBizTrainPlanService;
import com.ruoyi.biz.service.IBizTrainLedgerService;
import com.ruoyi.biz.service.IBizTrainOcrBatchService;

/**
 * 培训计划智能预警与自动核销定时任务
 */
@Component("planWarningTask")
public class PlanWarningTask {

    private static final Logger log = LoggerFactory.getLogger(PlanWarningTask.class);

    @Autowired
    private IBizTrainPlanService bizTrainPlanService;

    @Autowired
    private IBizTrainLedgerService ledgerService;

    @Autowired
    private IBizTrainOcrBatchService ocrBatchService;

    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 智能扫描未完成的计划：
     * 1. 自动检测是否有 OCR 批次或台账关联了该计划，若有则自动标记完成
     * 2. 扫描所有历史逾期及本月的未完成计划，统一发送预警通知
     */
    public void checkUnfinishedPlans() {
        log.info("========== 开始执行【培训计划智能核销与预警】定时任务 ==========");

        // 1. 查找所有标记为“未完成 (0)”的计划
        BizTrainPlan query = new BizTrainPlan();
        query.setStatus("0"); // 0-未完成
        List<BizTrainPlan> unfinishedPlans = bizTrainPlanService.selectBizTrainPlanList(query);

        if (StringUtils.isNull(unfinishedPlans) || unfinishedPlans.isEmpty()) {
            log.info("当前系统内没有任何未完成的培训计划。");
            return;
        }

        // 2. 自动状态核销 (Auto-Reconciliation)
        int autoFinishedCount = 0;
        for (int i = unfinishedPlans.size() - 1; i >= 0; i--) {
            BizTrainPlan plan = unfinishedPlans.get(i);
            boolean isActuallyFinished = false;

            // 规则 A: 检查是否已有最终台账关联了此计划
            BizTrainLedger ledgerQuery = new BizTrainLedger();
            ledgerQuery.setPlanId(plan.getPlanId());
            List<BizTrainLedger> linkedLedgers = ledgerService.selectBizTrainLedgerList(ledgerQuery);
            if (linkedLedgers != null && !linkedLedgers.isEmpty()) {
                isActuallyFinished = true;
            }

            // 规则 B: 如果没有台账，检查是否正在进行 OCR 批次解析
            if (!isActuallyFinished) {
                BizTrainOcrBatch ocrQuery = new BizTrainOcrBatch();
                ocrQuery.setPlanId(plan.getPlanId());
                List<BizTrainOcrBatch> linkedBatches = ocrBatchService.selectBizTrainOcrBatchList(ocrQuery);
                // 只要有批次记录 (无论它是在解析中、待校对还是已结算)，都说明这个计划已经进入执行流
                if (linkedBatches != null && !linkedBatches.isEmpty()) {
                    isActuallyFinished = true;
                }
            }

            // 如果确实已经在执行了，自动修正数据库状态，并将其移出本轮报警名单
            if (isActuallyFinished) {
                plan.setStatus("1"); // 1-已完成
                plan.setUpdateBy("system_task");
                bizTrainPlanService.updateBizTrainPlan(plan);

                unfinishedPlans.remove(i);
                autoFinishedCount++;
                log.debug("计划 [ID: {}, 主题: {}] 检测到已有业务关联，系统自动核销为已完成。", plan.getPlanId(), plan.getSubject());
            }
        }
        log.info("本轮自动核销了 {} 个人工遗忘标记的培训计划。", autoFinishedCount);

        // 3. 过滤出真正需要报警的计划：仅报警当前年月 及 以前的历史遗留计划
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;

        List<BizTrainPlan> targetAlertPlans = unfinishedPlans.stream().filter(p -> {
            Integer pYear = p.getPlanYear();
            Integer pMonth = parsePlanMonthNumber(p.getPlanMonth());
            if (pYear == null || pMonth == null) return false;

            // 历史年份 或者 同年但月份小于等于当前月
            return pYear < currentYear || (pYear == currentYear && pMonth <= currentMonth);
        }).collect(Collectors.toList());

        if (targetAlertPlans.isEmpty()) {
            log.info("核销过滤后，截至 {}年{}月，无积压或逾期计划需要预警。", currentYear, currentMonth);
            return;
        }

        // 4. 拼装推送到系统的预警通知
        StringBuilder content = new StringBuilder();
        content.append("<p style='color: #fa3534; font-size: 16px; font-weight: bold;'>系统检测到以下培训计划已逾期或本月尚未执行，请抓紧组织培训或推进台账入库：</p><ul style='line-height: 1.8;'>");

        for (BizTrainPlan plan : targetAlertPlans) {
            String planTypeStr = (plan.getPlanType() != null && plan.getPlanType().equals("1")) ? "计划内" : "计划外";
            // 标注是严重逾期还是本月待办
            String timeTag = (plan.getPlanYear() < currentYear
                    || (plan.getPlanYear() == currentYear && parsePlanMonthNumber(plan.getPlanMonth()) < currentMonth))
                    ? "<span style='color: red; font-weight: bold;'>[已逾期]</span> "
                    : "<span style='color: #f9ae3d; font-weight: bold;'>[本月待办]</span> ";

            content.append("<li>")
                    .append(timeTag)
                    .append("【").append(planTypeStr).append("】 ")
                    .append(plan.getSubject())
                    .append(" <span style='color: #909399;'>(原定: ").append(plan.getPlanYear()).append("年").append(plan.getPlanMonth()).append("月)</span>")
                    .append("</li>");
        }
        content.append("</ul><p style='color: #606266; font-size: 12px; margin-top: 20px;'>* 注：若培训已完成，请及时在移动端【拍表入库】或 PC端关联计划并入账，系统将自动核销该计划。</p>");

        // 5. 生成系统级公告
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle("培训计划积压/逾期预警通知 (" + currentYear + "-" + currentMonth + ")");
        notice.setNoticeType("1"); // 1为通知
        notice.setNoticeContent(content.toString());
        notice.setStatus("0"); // 0正常
        notice.setCreateBy("system_task");

        noticeService.insertNotice(notice);

        log.info("========== 预警任务执行完毕，共推送了 {} 条需要关注的计划 ==========", targetAlertPlans.size());
    }

    /**
     * 从 plan_month 字符串中提取月份数字。
     * 支持 "2026年6月" / "2026年06月" / "6月" / "06月"，返回 1-12；其他返回 null。
     */
    private Integer parsePlanMonthNumber(String s) {
        if (s == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2})月").matcher(s);
        if (m.find()) {
            try {
                int v = Integer.parseInt(m.group(1));
                return (v >= 1 && v <= 12) ? v : null;
            } catch (NumberFormatException ignore) {}
        }
        return null;
    }
}