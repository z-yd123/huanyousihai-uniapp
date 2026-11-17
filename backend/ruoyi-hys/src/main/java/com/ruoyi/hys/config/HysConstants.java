package com.ruoyi.hys.config;

/**
 * 环游四海业务常量。状态值集中在此，避免魔法数字散落在 controller / mapper / 前端。
 */
public final class HysConstants {

    private HysConstants() {
    }

    // ---------------- 会员 ----------------
    /** 会员状态：正常 */
    public static final String MEMBER_NORMAL = "0";
    /** 会员状态：停用 */
    public static final String MEMBER_DISABLED = "1";
    /** 会员状态：推荐关系异常，停止计佣待补绑 */
    public static final String MEMBER_ABNORMAL = "2";

    /** 会员性质：普通 */
    public static final String LEVEL_NORMAL = "0";
    /** 会员性质：VIP */
    public static final String LEVEL_VIP = "1";

    // ---------------- 商品 ----------------
    public static final String PRODUCT_ROUTE = "ROUTE";
    public static final String PRODUCT_SPECIAL = "SPECIAL";
    public static final String PRODUCT_VIP_RIGHTS = "VIP_RIGHTS";
    public static final String ROUTE_GROUP = "GROUP";
    public static final String ROUTE_SELF_DRIVE = "SELF_DRIVE";
    /** 上架 */
    public static final String ON_SALE = "0";

    // ---------------- 订单 ----------------
    /** 待付款 */
    public static final int ORDER_UNPAID = 0;
    /** 待收货 / 待出行 */
    public static final int ORDER_PAID = 1;
    /** 已完成 */
    public static final int ORDER_FINISHED = 2;
    /** 售后中 */
    public static final int ORDER_AFTER_SALE = 3;
    /** 已退款 */
    public static final int ORDER_REFUNDED = 4;
    /** 已取消 */
    public static final int ORDER_CANCELLED = 5;

    /** 售后状态：无 */
    public static final int AFTER_SALE_NONE = 0;
    /** 售后状态：申请中 */
    public static final int AFTER_SALE_APPLYING = 1;
    /** 售后状态：已退款 */
    public static final int AFTER_SALE_REFUNDED = 2;
    /** 售后状态：已驳回 */
    public static final int AFTER_SALE_REJECTED = 3;

    // ---------------- 资金 ----------------
    public static final String APPLY_RECHARGE = "RECHARGE";
    public static final String APPLY_WITHDRAW = "WITHDRAW";
    public static final int APPLY_PENDING = 0;
    public static final int APPLY_PASSED = 1;
    public static final int APPLY_REJECTED = 2;

    public static final String TXN_RECHARGE = "RECHARGE";
    public static final String TXN_TRANSFER_IN = "TRANSFER_IN";
    public static final String TXN_TRANSFER_OUT = "TRANSFER_OUT";
    public static final String TXN_WITHDRAW = "WITHDRAW";
    public static final String TXN_COMMISSION = "COMMISSION";
    public static final String TXN_COMMISSION_REVERSE = "COMMISSION_REVERSE";
    public static final String TXN_ORDER_PAY = "ORDER_PAY";
    public static final String TXN_REFUND = "REFUND";
    /** VIP 续费（99 元/年，Q-03 默认不计佣） */
    public static final String TXN_VIP_RENEW = "VIP_RENEW";
    /** 提现被驳回，冻结金额解冻回可用 */
    public static final String TXN_WITHDRAW_UNFREEZE = "WITHDRAW_UNFREEZE";

    /** 佣金状态：已入账 */
    public static final int COMMISSION_SETTLED = 1;
    /** 佣金状态：已回冲 */
    public static final int COMMISSION_REVERSED = 2;

    // ---------------- 配置键（hys_config） ----------------
    public static final String CFG_VIP_PRICE = "vip.price";
    public static final String CFG_VIP_RENEW_PRICE = "vip.renew_price";
    public static final String CFG_VIP_RENEW_YEARS = "vip.renew_years";
    public static final String CFG_VIP_EXPIRE_AFFECTS_COMMISSION = "vip.expire_affects_commission";
    public static final String CFG_PAYOUT_TOTAL = "payout.total";
    public static final String CFG_PAYOUT_DIRECT_FIRST_TWO = "payout.direct_first_two";
    public static final String CFG_PAYOUT_INDIRECT_FIRST_TWO = "payout.indirect_first_two";
    public static final String CFG_PAYOUT_DIRECT_AFTER = "payout.direct_after";
    public static final String CFG_WITHDRAW_FEE_RATE = "payout.withdraw_fee_rate";
    public static final String CFG_RENEW_COMMISSION_RATE = "payout.renew_commission_rate";
    public static final String CFG_RECHARGE_LIMIT = "wallet.recharge_limit";
    public static final String CFG_TRANSFER_DAILY_LIMIT = "wallet.transfer_daily_limit";
    public static final String CFG_TRANSFER_DAILY_COUNT = "wallet.transfer_daily_count";
    public static final String CFG_APP_NOTICE = "app.notice";

    /** 默认兜底值（配置表缺失时仍可运行，但会打 WARN 日志） */
    public static final long DEFAULT_RECHARGE_LIMIT = 500_000L;
    public static final long DEFAULT_TRANSFER_DAILY_LIMIT = 2_000_000L;
    public static final int DEFAULT_TRANSFER_DAILY_COUNT = 20;

    /** 提现最低金额（分）：1 元，避免 0 元刷单 */
    public static final long MIN_WITHDRAW = 100L;
}
