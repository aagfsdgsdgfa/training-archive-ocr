package com.ruoyi.biz.event;

/**
 * OCR 批次已准备好（事务提交后触发），通知异步处理器开始扫描
 */
public class OcrBatchReadyEvent {
    private final Long batchId;

    public OcrBatchReadyEvent(Long batchId) {
        this.batchId = batchId;
    }

    public Long getBatchId() {
        return batchId;
    }
}
