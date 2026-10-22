package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainSession;

/**
 * 培训执行场次Service接口
 * 
 * @author admin
 * @date 2026-03-03
 */
public interface IBizTrainSessionService 
{
    /**
     * 查询培训执行场次
     * 
     * @param sessionId 培训执行场次主键
     * @return 培训执行场次
     */
    public BizTrainSession selectBizTrainSessionBySessionId(Long sessionId);

    /**
     * 查询培训执行场次列表
     * 
     * @param bizTrainSession 培训执行场次
     * @return 培训执行场次集合
     */
    public List<BizTrainSession> selectBizTrainSessionList(BizTrainSession bizTrainSession);

    /**
     * 新增培训执行场次
     * 
     * @param bizTrainSession 培训执行场次
     * @return 结果
     */
    public int insertBizTrainSession(BizTrainSession bizTrainSession);

    /**
     * 修改培训执行场次
     * 
     * @param bizTrainSession 培训执行场次
     * @return 结果
     */
    public int updateBizTrainSession(BizTrainSession bizTrainSession);

    /**
     * 批量删除培训执行场次
     * 
     * @param sessionIds 需要删除的培训执行场次主键集合
     * @return 结果
     */
    public int deleteBizTrainSessionBySessionIds(Long[] sessionIds);

    /**
     * 删除培训执行场次信息
     * 
     * @param sessionId 培训执行场次主键
     * @return 结果
     */
    public int deleteBizTrainSessionBySessionId(Long sessionId);

    /**
     * 校验场地在指定时间段内是否被占用
     * * @param bizTrainSession 培训场次信息
     * @return 结果
     */
    public int checkLocationConflict(BizTrainSession bizTrainSession);

    public void archiveSession(Long sessionId, String operName);

    /**
     * 检查该计划下所有场次是否都已归档，若全部归档则把 plan.status 自动置为 '1'（已完成）。
     * 计划为 null / 计划已被取消 / 计划本来就已完成 → 不动。
     * 用于 OCR 批次结算、线上考勤归档 等多条归档入口统一收口。
     */
    void checkAndMarkPlanCompleted(Long planId, String operName);
}
