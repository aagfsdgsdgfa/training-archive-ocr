package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 培训执行场次Mapper接口
 * 
 * @author admin
 * @date 2026-03-03
 */
@Mapper
public interface BizTrainSessionMapper 
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
     * 删除培训执行场次
     * 
     * @param sessionId 培训执行场次主键
     * @return 结果
     */
    public int deleteBizTrainSessionBySessionId(Long sessionId);

    /**
     * 批量删除培训执行场次
     * 
     * @param sessionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTrainSessionBySessionIds(Long[] sessionIds);

    /**
     * 校验场地在指定时间段内是否被占用
     */
    public int checkLocationConflict(BizTrainSession session);
}
