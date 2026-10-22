package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainAttendance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 线上考勤与申诉明细Mapper接口
 * 
 * @author admin
 * @date 2026-03-03
 */
@Mapper
public interface BizTrainAttendanceMapper 
{
    /**
     * 查询线上考勤与申诉明细
     * 
     * @param recordId 线上考勤与申诉明细主键
     * @return 线上考勤与申诉明细
     */
    public BizTrainAttendance selectBizTrainAttendanceByRecordId(Long recordId);

    /**
     * 查询线上考勤与申诉明细列表
     * 
     * @param bizTrainAttendance 线上考勤与申诉明细
     * @return 线上考勤与申诉明细集合
     */
    public List<BizTrainAttendance> selectBizTrainAttendanceList(BizTrainAttendance bizTrainAttendance);

    /**
     * 新增线上考勤与申诉明细
     * 
     * @param bizTrainAttendance 线上考勤与申诉明细
     * @return 结果
     */
    public int insertBizTrainAttendance(BizTrainAttendance bizTrainAttendance);

    /**
     * 修改线上考勤与申诉明细
     * 
     * @param bizTrainAttendance 线上考勤与申诉明细
     * @return 结果
     */
    public int updateBizTrainAttendance(BizTrainAttendance bizTrainAttendance);

    /**
     * 删除线上考勤与申诉明细
     * 
     * @param recordId 线上考勤与申诉明细主键
     * @return 结果
     */
    public int deleteBizTrainAttendanceByRecordId(Long recordId);

    /**
     * 批量删除线上考勤与申诉明细
     *
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTrainAttendanceByRecordIds(Long[] recordIds);

    /**
     * 根据场次ID查询考勤记录列表
     * @param sessionId 场次ID
     * @return 考勤记录列表
     */
    public List<BizTrainAttendance> selectBizTrainAttendanceBySessionId(Long sessionId);
}
