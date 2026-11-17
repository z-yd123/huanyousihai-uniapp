package com.ruoyi.hys.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单（商品与线路统一入订单中心）。
 *
 * <p>
 * 状态机：0 待付款 → 1 待收货/待出行 → 2 已完成；1/2 → 3 售后中 → 4 已退款；0 → 5 已取消。
 * 所有流转在 {@code HysOrderService} 内校验前置状态，禁止跨状态跳变。
 * </p>
 */
public class HysOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNo;
    private Long memberId;
    private Long productId;
    private String productType;
    private String productTitle;
    private String productCover;
    private Long unitPrice;
    private Integer quantity;
    private Long amount;
    private Long payAmount;
    private Integer status;
    private String payType;
    private LocalDateTime payTime;
    private LocalDate travelDate;
    private String contactName;
    private String contactPhone;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime refundTime;
    private String refundReason;
    private Integer afterSale;

    /** 下单会员昵称（联查展示） */
    private String memberName;
    /** 下单会员手机号（脱敏展示） */
    private String memberPhone;

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

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductCover() {
        return productCover;
    }

    public void setProductCover(String productCover) {
        this.productCover = productCover;
    }

    public Long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Long payAmount) {
        this.payAmount = payAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public LocalDateTime getShipTime() {
        return shipTime;
    }

    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public Integer getAfterSale() {
        return afterSale;
    }

    public void setAfterSale(Integer afterSale) {
        this.afterSale = afterSale;
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
}
