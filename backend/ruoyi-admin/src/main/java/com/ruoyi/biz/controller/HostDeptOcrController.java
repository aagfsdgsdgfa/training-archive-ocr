package com.ruoyi.biz.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.biz.ocr.HostDeptOcrService;
import com.ruoyi.biz.ocr.OcrHostDeptResult;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.file.FileUtils;

/**
 * 培训部门 OCR 识别 Controller。
 *
 * <p>接收单张图片（jpg/png），调用 Baidu OCR 抽出"培训部门：xxx"字段，
 * 在 sys_dept 表中匹配后返回结果。前端依据 matchType 选择自动回填 / 候选弹窗 / 手动选择。
 */
@RestController
@RequestMapping("/biz/ocr")
public class HostDeptOcrController {

    private static final Logger log = LoggerFactory.getLogger(HostDeptOcrController.class);

    @Autowired
    private HostDeptOcrService hostDeptOcrService;

    @PostMapping("/hostDept")
    public AjaxResult hostDept(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要识别的图片");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String tmpDir = System.getProperty("java.io.tmpdir") + "/xd-ocr-tmp/";
        File dir = new File(tmpDir);
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("无法创建 OCR 临时目录: {}", tmpDir);
            return AjaxResult.error("服务器临时目录创建失败");
        }
        String tmpFile = tmpDir + UUID.randomUUID() + ext;
        try {
            file.transferTo(new File(tmpFile));
        } catch (IOException e) {
            log.error("OCR 图片落盘失败: {}", e.getMessage());
            return AjaxResult.error("图片保存失败: " + e.getMessage());
        }

        try {
            OcrHostDeptResult result = hostDeptOcrService.recognize(tmpFile);
            log.info("培训部门 OCR 识别完成: matchType={}, deptName={}",
                result.getMatchType(), result.getDeptName());
            // 百度接口业务错误（如未开通 OCR 服务/配额用尽）—— 给前端一个明确的错误提示
            if (result.getErrorCode() != null) {
                String hint = friendlyBaiduError(result.getErrorCode(), result.getErrorMsg());
                log.warn("OCR 返回百度业务错误: code={} msg={} → {}",
                    result.getErrorCode(), result.getErrorMsg(), hint);
                return AjaxResult.error(hint);
            }
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("OCR 识别异常: {}", e.getMessage(), e);
            return AjaxResult.error("OCR 识别失败: " + e.getMessage());
        } finally {
            // 临时文件不持久化，识别完即删
            try {
                FileUtils.deleteFile(tmpFile);
            } catch (Exception cleanupEx) {
                log.warn("OCR 临时文件清理失败: {}", cleanupEx.getMessage());
            }
        }
    }

    /**
     * 把百度接口的 error_code 转成对管理员/用户友好的中文提示。
     * 参考百度 OCR 错误码表：https://ai.baidu.com/ai-doc/OCR/ak193h3kk
     */
    private String friendlyBaiduError(int code, String rawMsg) {
        switch (code) {
            case 6:
                return "百度OCR服务未开通或无访问权限（error_code=6），请在百度智能云控制台开通「文字识别 OCR」服务";
            case 17:
                return "百度OCR日调用次数超限（error_code=17），请在控制台申请提额";
            case 18:
                return "百度OCR账号配额不足（error_code=18），请充值或申请提额";
            case 19:
                return "百度OCR客户端IP不在白名单（error_code=19），请在控制台添加本机 IP";
            case 100:
            case 110:
                return "百度OCR access_token 无效或过期（error_code=" + code + "），请检查 AK/SK 配置";
            case 216100:
                return "百度OCR参数异常（error_code=216100），图片可能不是标准 JPEG/PNG 格式";
            case 216201:
                return "百度OCR图片解码失败（error_code=216201），请检查图片是否完整";
            case 282000:
                return "百度OCR服务端系统错误（error_code=282000），请稍后重试";
            default:
                return "百度OCR调用失败（error_code=" + code + ", msg=" + rawMsg + "），请联系管理员";
        }
    }
}