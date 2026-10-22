package com.ruoyi.biz.domain.vo;

import java.util.List;
import javax.validation.constraints.NotEmpty;

/**
 * 高拍仪批量上传请求参数
 */
public class OcrBatchUploadReq {

    // 扫表前管理员选择绑定的年度计划ID
    private Long planId;

    // 高拍仪传来的 Base64 图片数组
    @NotEmpty(message = "扫描图片不能为空")
    private List<String> base64Images;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public List<String> getBase64Images() {
        return base64Images;
    }

    public void setBase64Images(List<String> base64Images) {
        this.base64Images = base64Images;
    }
}