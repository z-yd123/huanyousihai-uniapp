package com.ruoyi.hys.domain;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 会员（小程序端账号，与若依后台 sys_user 完全隔离）。
 *
 * <p>
 * 合规要点：{@code referrerId} 在正常注册链路中<b>不允许为空</b>，它是两级佣金链路的前置条件；
 * 只有平台根账号与历史脏数据会出现空值，后者会被置为“异常待补绑”状态并停止计佣。
 * </p>
 */
public class HysMember extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long memberId;
    private String phone;
    private String nickname;

    /** 密码只允许写入，不允许序列化回前端 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String province;
    private String city;
    private String district;
    private Long referrerId;
    private String referrerPath;
    private Integer directSeq;
    private String level;
    private LocalDateTime vipExpireAt;
    private Long balance;
    private Long frozen;
    private Integer greenPoints;
    private String status;
    private Integer tokenVersion;
    private LocalDateTime lastLoginAt;

    // ------- 非持久化展示字段（列表联查 / 详情统计） -------
    /** 推荐人名称 */
    private String referrerName;
    /** 推荐人手机号（脱敏） */
    private String referrerPhone;
    /** 直推人数 */
    private Long directCount;
    /** 团队人数（全部后代） */
    private Long teamCount;
    /** 累计佣金（分） */
    private Long commissionTotal;

    /** VIP 是否在有效期内（由服务层按当前时间计算，前端不再自行判断） */
    @JsonIgnore
    public boolean isVipActive() {
        return "1".equals(level) && vipExpireAt != null && vipExpireAt.isAfter(LocalDateTime.now());
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Long getReferrerId() {
        return referrerId;
    }

    public void setReferrerId(Long referrerId) {
        this.referrerId = referrerId;
    }

    public String getReferrerPath() {
        return referrerPath;
    }

    public void setReferrerPath(String referrerPath) {
        this.referrerPath = referrerPath;
    }

    public Integer getDirectSeq() {
        return directSeq;
    }

    public void setDirectSeq(Integer directSeq) {
        this.directSeq = directSeq;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public LocalDateTime getVipExpireAt() {
        return vipExpireAt;
    }

    public void setVipExpireAt(LocalDateTime vipExpireAt) {
        this.vipExpireAt = vipExpireAt;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getFrozen() {
        return frozen;
    }

    public void setFrozen(Long frozen) {
        this.frozen = frozen;
    }

    public Integer getGreenPoints() {
        return greenPoints;
    }

    public void setGreenPoints(Integer greenPoints) {
        this.greenPoints = greenPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(Integer tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getReferrerName() {
        return referrerName;
    }

    public void setReferrerName(String referrerName) {
        this.referrerName = referrerName;
    }

    public String getReferrerPhone() {
        return referrerPhone;
    }

    public void setReferrerPhone(String referrerPhone) {
        this.referrerPhone = referrerPhone;
    }

    public Long getDirectCount() {
        return directCount;
    }

    public void setDirectCount(Long directCount) {
        this.directCount = directCount;
    }

    public Long getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(Long teamCount) {
        this.teamCount = teamCount;
    }

    public Long getCommissionTotal() {
        return commissionTotal;
    }

    public void setCommissionTotal(Long commissionTotal) {
        this.commissionTotal = commissionTotal;
    }
}
