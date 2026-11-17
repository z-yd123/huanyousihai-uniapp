package com.ruoyi.hys.domain;

import java.time.LocalDateTime;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 佣金明细。{@code level} 只允许 DIRECT / INDIRECT 两个值，数据库与引擎双层约束（COMPLIANCE）。
 */
public class HysCommission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long commissionId;
    private Long memberId;
    private Long orderId;
    private String orderNo;
    private String level;
    private Long amount;
    private Integer status;
    private LocalDateTime settleTime;

    /** 受益人昵称（联查展示） */
    private String memberName;
    /** 受益人手机号（脱敏展示） */
    private String memberPhone;
    /** 来源订单商品标题（联查展示） */
    private String productTitle;

    public Long getCommissionId() {
        return commissionId;
    }

    public void setCommissionId(Long commissionId) {
        this.commissionId = commissionId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(LocalDateTime settleTime) {
        this.settleTime = settleTime;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }
}
