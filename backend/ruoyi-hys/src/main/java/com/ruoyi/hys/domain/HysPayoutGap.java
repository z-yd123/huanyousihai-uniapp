package com.ruoyi.hys.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 佣金异常对账。正常业务<b>恒为空</b>，只有推荐链断裂（referrer 为空等脏数据）才会写入。
 */
public class HysPayoutGap implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long gapId;
    private Long orderId;
    private String orderNo;
    private Long amount;
    private String reason;
    private String detail;
    private String handled;
    private String handleRemark;
    private LocalDateTime createTime;
    private LocalDateTime handleTime;

    public Long getGapId() {
        return gapId;
    }

    public void setGapId(Long gapId) {
        this.gapId = gapId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getHandled() {
        return handled;
    }

    public void setHandled(String handled) {
        this.handled = handled;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }
}
