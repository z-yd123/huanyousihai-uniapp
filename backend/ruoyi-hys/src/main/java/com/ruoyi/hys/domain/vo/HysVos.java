package com.ruoyi.hys.domain.vo;

import java.util.List;

/**
 * 小程序端 / 管理端响应视图。
 *
 * <p>
 * 只暴露前端真正需要的字段：会员实体里的 {@code referrerPath}、{@code tokenVersion}、{@code password}
 * 一律不出现在任何视图里，从源头杜绝越权信息泄露（数据边界）。
 * 手机号统一脱敏为 {@code 138****0004}。
 * </p>
 */
public final class HysVos {

    private HysVos() {
    }

    /**
     * 当前会员信息（个人中心 / 会员卡）。
     */
    public record Member(Long memberId, String phone, String nickname, String province, String city, String district,
            String region, String level, boolean vipActive, String vipExpireAt, Long balance, Long frozen,
            Integer greenPoints, String referrerName, String referrerPhone, String inviteCode, String createTime) {
    }

    /**
     * 我的分享 · 概览。
     */
    public record ShareOverview(String referrerName, String referrerPhone, Long teamCount, Long directCount,
            Long monthNew, String inviteCode, String notice) {
    }

    /**
     * 我的分享 · 直推名单一项（字段对齐 AGENT.md 4.4：手机号、团队人数、个人人数、会员性质、注册时间）。
     */
    public record DirectItem(Long memberId, String nickname, String phone, String level, boolean vipActive,
            Long teamCount, Long directCount, String createTime) {
    }

    /**
     * 商品/线路卡片（双价展示）。
     *
     * @param price           当前身份适用价（分）
     * @param vipPriceApplied 是否已按 VIP 价生效
     */
    public record ProductCard(Long productId, String productType, String routeType, String title, String subtitle,
            String cover, Long price, Long normalPrice, Long vipPrice, boolean vipPriceApplied, Integer sales,
            Integer days, Integer nights, String tag) {
    }

    /**
     * 行程分段。
     */
    public record DayItem(Integer dayNo, String title, String content, List<String> images, String gatherTime,
            String gatherPlace) {
    }

    /**
     * 线路/商品详情。
     */
    public record ProductDetail(Long productId, String productType, String routeType, String title, String subtitle,
            String cover, List<String> images, String videoUrl, String detail, Long price, Long normalPrice,
            Long vipPrice, boolean vipPriceApplied, Integer stock, Integer sales, Integer days, Integer nights,
            String departCity, String gatherTime, String gatherPlace, String includes, String excludes,
            List<DayItem> dayList) {
    }

    /**
     * 首页聚合（一次请求拿全，避免小程序首屏串行四次请求）。
     */
    public record Home(ProductCard vipRights, List<ProductCard> routes, List<ProductCard> specials, String notice) {
    }

    /**
     * 钱包概览。
     */
    public record WalletSummary(Long balance, Long frozen, Long commissionTotal, Long rechargeTotal,
            Long withdrawTotal, Integer greenPoints, String notice) {
    }

    /**
     * 提现试算结果（前端明示手续费与到账金额）。
     */
    public record WithdrawQuote(Long amount, Long fee, Long actualAmount, String feeRateText) {
    }

    /**
     * 佣金概览。
     */
    public record CommissionSummary(Long total, Long direct, Long indirect, Long pending, Integer count,
            String notice) {
    }

    /**
     * 运营看板。
     */
    public record Dashboard(Long memberTotal, Long vipTotal, Long orderTotal, Long orderToday, Long gmvTotal,
            Long commissionTotal, Long pendingAudit, Long gapCount, List<TrendPoint> trend) {
    }

    /**
     * 看板趋势点（近 7 日订单量与成交额）。
     */
    public record TrendPoint(String date, Long orders, Long amount) {
    }

    /**
     * 邀请二维码（base64，前端可直接放进 image 的 src）。
     */
    public record InviteQr(String inviteCode, String content, String dataUrl) {
    }
}
