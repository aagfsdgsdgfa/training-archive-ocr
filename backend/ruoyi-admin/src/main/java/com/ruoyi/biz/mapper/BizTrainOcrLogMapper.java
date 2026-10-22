package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizTrainOcrLog;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * OCR防重与拦截记录Mapper接口
 * 
 * @author admin
 * @date 2026-03-04
 */
@Mapper
public interface BizTrainOcrLogMapper 
{
    /**
     * 查询OCR防重与拦截记录
     * 
     * @param logId OCR防重与拦截记录主键
     * @return OCR防重与拦截记录
     */
    public BizTrainOcrLog selectBizTrainOcrLogByLogId(Long logId);

    /**
     * 查询OCR防重与拦截记录列表
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return OCR防重与拦截记录集合
     */
    public List<BizTrainOcrLog> selectBizTrainOcrLogList(BizTrainOcrLog bizTrainOcrLog);

    /**
     * 新增OCR防重与拦截记录
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return 结果
     */
    public int insertBizTrainOcrLog(BizTrainOcrLog bizTrainOcrLog);

    /**
     * 修改OCR防重与拦截记录
     * 
     * @param bizTrainOcrLog OCR防重与拦截记录
     * @return 结果
     */
    public int updateBizTrainOcrLog(BizTrainOcrLog bizTrainOcrLog);

    /**
     * 删除OCR防重与拦截记录
     * 
     * @param logId OCR防重与拦截记录主键
     * @return 结果
     */
    public int deleteBizTrainOcrLogByLogId(Long logId);

    /**
     * 批量删除OCR防重与拦截记录
     * 
     * @param logIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTrainOcrLogByLogIds(Long[] logIds);

   /**
    * 根据 MD5 校验图片是否已存在（防重）
    */
    @Select("SELECT * FROM biz_train_ocr_log WHERE file_md5 = #{fileMd5} AND del_flag = '0' LIMIT 1")
    BizTrainOcrLog checkMd5Unique(@Param("fileMd5") String fileMd5);
}
