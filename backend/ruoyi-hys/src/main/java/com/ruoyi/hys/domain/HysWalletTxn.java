package com.ruoyi.hys.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 钱包流水。{@code amount} 带符号（收入正、支出负），{@code bizKey} 唯一索引保证幂等。
 */
public class HysWalletTxn extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long txnId;
    private Long memberId;
    private String txnType;
    private Long amount;
    private Long fee;
    private Long balanceAfter;
    private Long counterpartyId;
    private String bizKey;
    /** 对手方昵称（联查展示） */
    private String counterpartyName;

    public Long getTxnId() {
        return txnId;
    }

    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Long getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Long balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Long getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(Long counterpartyId) {
        this.counterpartyId = counterpartyId;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }
}
