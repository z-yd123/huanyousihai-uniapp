package com.ruoyi.hys.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 两级佣金引擎（纯函数，无 Spring / 无数据库依赖，可直接单元测试）。
 *
 * <p>
 * 口径来源：AGENT.md §6「佣金算法（2026-11-13 已冻结）」。三条不可协商的约束：
 * </p>
 * <ol>
 * <li><b>只有两级</b>：直推（DIRECT）+ 间推（INDIRECT），层级上限 {@value #MAX_DEPTH} 是常量而非配置，
 * 不提供“第三级开关”；</li>
 * <li><b>每单拨满 200 元</b>：任何 398 升级订单的佣金合计恒为 {@value #PAYOUT_TOTAL} 分，不多拨、不少拨；</li>
 * <li><b>禁止顺延 / 改派 / 静默少发</b>：推荐链缺失属数据异常，中断计佣并落 {@code hys_payout_gap} 告警。</li>
 * </ol>
 *
 * <p>
 * 关于 AGENT.md 自身的一处矛盾（本实现的口径选择，已写入 docs/01-需求覆盖矩阵.md）：
 * §6.3 伪代码断言“每单受益人恒为 2 人”，但 §6.1 表格与 §8.3 用例又规定“第 3 个及以后直推：直推人 200、间推 0”。
 * 二者不可能同时成立。本引擎按 <b>§6.1 表格 + §8.3 用例</b>（可执行口径）实现：拨比恒 200 元，
 * 受益人数量为 2（第 1、2 个直推）或 1（第 3 个起）。断言因此改为
 * “合计恒 200 + 层级 ≤ 2 + 有间推奖时恰好两条”。
 * </p>
 */
public final class CommissionEngine {

    /** COMPLIANCE：分销层级上限，国家级红线，常量不可配置放大 */
    public static final int MAX_DEPTH = 2;

    /** COMPLIANCE：单笔 398 升级订单的拨比总额（分）= 200 元 */
    public static final long PAYOUT_TOTAL = 20_000L;

    /** 直推奖（第 1、2 个直推） */
    public static final String LEVEL_DIRECT = "DIRECT";

    /** 间推奖（拨给“推荐的推荐”） */
    public static final String LEVEL_INDIRECT = "INDIRECT";

    /** 异常原因：会员自身没有推荐人（正常注册链路不可能出现） */
    public static final String GAP_REFERRER_MISSING = "referrer_missing";

    /** 异常原因：直推人没有推荐人（即新会员没有间推人） */
    public static final String GAP_INDIRECT_MISSING = "indirect_missing";

    private CommissionEngine() {
    }

    /**
     * 拨比规则（来自 hys_config，可审计、可配置，但受 {@link #validateRule} 强校验）。
     *
     * @param total           单笔拨比总额（分），必须等于 {@link #PAYOUT_TOTAL}
     * @param directFirstTwo  第 1、2 个直推的直推奖（分）
     * @param indirectFirstTwo 第 1、2 个直推的间推奖（分）
     * @param directAfter     第 3 个起直推的直推奖（分）
     */
    public record PayoutRule(long total, long directFirstTwo, long indirectFirstTwo, long directAfter) {

        /** 默认口径：200 = 100 + 100（前两单）/ 200 + 0（第三单起） */
        public static PayoutRule defaults() {
            return new PayoutRule(PAYOUT_TOTAL, 10_000L, 10_000L, 20_000L);
        }
    }

    /**
     * 计佣入参。金额与序号都来自已落库的订单/会员快照，引擎不再回查数据库。
     *
     * @param orderNo            来源订单号（用于备注与对账）
     * @param newMemberId        刚升级的会员
     * @param referrerId         直推人（新会员的推荐人），正常业务必不为空
     * @param directSeq          新会员是直推人的第几个直推（按直推人独立计数，从 1 开始）
     * @param referrerReferrerId 间推人（直推人的推荐人），正常业务必不为空
     */
    public record Subject(String orderNo, long newMemberId, Long referrerId, int directSeq, Long referrerReferrerId) {
    }

    /**
     * 一条佣金明细。
     *
     * @param memberId 受益人
     * @param level    DIRECT / INDIRECT
     * @param amount   金额（分）
     * @param depth    层级深度（1=直推，2=间推）
     * @param remark   明细说明
     */
    public record Leg(long memberId, String level, long amount, int depth, String remark) {
    }

    /**
     * 异常对账记录（正常业务不产生）。
     *
     * @param reason 异常原因
     * @param amount 未发放金额（分）
     * @param detail 说明
     */
    public record Gap(String reason, long amount, String detail) {
    }

    /**
     * 计佣结果。
     *
     * @param settled 是否完成计佣；false 表示已中断并落对账
     * @param legs    佣金明细（settled=true 时非空）
     * @param gap     对账记录（settled=true 时为 null）
     */
    public record Settlement(boolean settled, List<Leg> legs, Gap gap) {

        public static Settlement aborted(Gap gap) {
            return new Settlement(false, List.of(), gap);
        }

        public long totalAmount() {
            return legs.stream().mapToLong(Leg::amount).sum();
        }
    }

    /**
     * 校验拨比规则：任何被篡改的配置都必须在这里被拦住，避免线上“悄悄少发/多发”。
     */
    public static void validateRule(PayoutRule rule) {
        if (rule == null) {
            throw new IllegalStateException("[COMPLIANCE] 拨比规则缺失");
        }
        if (rule.total() != PAYOUT_TOTAL) {
            throw new IllegalStateException("[COMPLIANCE] 拨比总额必须为 " + PAYOUT_TOTAL + " 分，当前 " + rule.total());
        }
        if (rule.directFirstTwo() + rule.indirectFirstTwo() != rule.total()) {
            throw new IllegalStateException("[COMPLIANCE] 前两单拆分之和必须等于拨比总额");
        }
        if (rule.directAfter() != rule.total()) {
            throw new IllegalStateException("[COMPLIANCE] 第 3 单起直推奖必须占满拨比总额（上级不再另发）");
        }
        if (rule.directFirstTwo() < 0 || rule.indirectFirstTwo() < 0) {
            throw new IllegalStateException("[COMPLIANCE] 拨比不能为负");
        }
    }

    /**
     * 计算一笔 398 升级订单的佣金分配。
     *
     * @param subject 计佣入参
     * @param rule    拨比规则
     * @return 计佣结果；推荐链断裂时返回中断结果（调用方须落对账表并告警，禁止改派或顺延）
     */
    public static Settlement settle(Subject subject, PayoutRule rule) {
        validateRule(rule);
        if (subject == null) {
            throw new IllegalArgumentException("subject 不能为空");
        }
        // COMPLIANCE：第 1 级都不存在 —— 只可能来自绕过注册建档的脏数据
        if (subject.referrerId() == null) {
            return Settlement.aborted(new Gap(GAP_REFERRER_MISSING, rule.total(),
                    "会员 " + subject.newMemberId() + " 无推荐人，订单 " + subject.orderNo() + " 计佣中断，待补绑后重算"));
        }

        boolean firstTwo = subject.directSeq() >= 1 && subject.directSeq() <= 2;
        long directAmount = firstTwo ? rule.directFirstTwo() : rule.directAfter();
        long indirectAmount = firstTwo ? rule.indirectFirstTwo() : 0L;

        // COMPLIANCE：间推奖 > 0 却取不到间推人 —— 脏数据，整单中断，绝不允许只发 100 元
        if (indirectAmount > 0 && subject.referrerReferrerId() == null) {
            return Settlement.aborted(new Gap(GAP_INDIRECT_MISSING, rule.total(),
                    "会员 " + subject.newMemberId() + " 的直推人 " + subject.referrerId() + " 无推荐人，订单 "
                            + subject.orderNo() + " 无法确定间推受益人，计佣中断"));
        }

        List<Leg> legs = new ArrayList<>(2);
        legs.add(new Leg(subject.referrerId(), LEVEL_DIRECT, directAmount, 1,
                "直推奖 · 该会员是您的第 " + subject.directSeq() + " 个直推"));
        if (indirectAmount > 0) {
            legs.add(new Leg(subject.referrerReferrerId(), LEVEL_INDIRECT, indirectAmount, 2,
                    "间推奖 · 您的直推会员 " + subject.referrerId() + " 推荐的新会员升级"));
        }

        Settlement settlement = new Settlement(true, List.copyOf(legs), null);
        assertCompliance(settlement, rule);
        return settlement;
    }

    /**
     * COMPLIANCE 断言：合计恒等于拨比总额、层级不超过 2、受益人不得重复、有间推奖时恰好两条。
     *
     * @throws IllegalStateException 任一红线被突破
     */
    public static void assertCompliance(Settlement settlement, PayoutRule rule) {
        if (settlement.totalAmount() != rule.total()) {
            throw new IllegalStateException("[COMPLIANCE] 佣金合计 " + settlement.totalAmount() + " ≠ 拨比 " + rule.total());
        }
        long beneficiaries = settlement.legs().stream().map(Leg::memberId).distinct().count();
        if (beneficiaries != settlement.legs().size()) {
            throw new IllegalStateException("[COMPLIANCE] 同一受益人出现多条佣金记录");
        }
        for (Leg leg : settlement.legs()) {
            if (leg.depth() > MAX_DEPTH) {
                throw new IllegalStateException("[COMPLIANCE] 出现第 " + leg.depth() + " 级受益人，层级上限为 " + MAX_DEPTH);
            }
            if (leg.amount() <= 0) {
                throw new IllegalStateException("[COMPLIANCE] 佣金金额必须为正");
            }
        }
        boolean hasIndirect = settlement.legs().stream().anyMatch(leg -> LEVEL_INDIRECT.equals(leg.level()));
        if (hasIndirect && settlement.legs().size() != 2) {
            throw new IllegalStateException("[COMPLIANCE] 有间推奖时必须恰好两条明细（直推 + 间推）");
        }
    }

    /**
     * 续费订单计佣（Q-03 未确认，默认比例 0 表示不计佣）。
     *
     * @param renewAmount 续费实付金额（分）
     * @param rate        计佣比例，0~1
     * @return 应发佣金（分），向下取整
     */
    public static long renewCommission(long renewAmount, BigDecimal rate) {
        if (rate == null || rate.signum() <= 0 || renewAmount <= 0) {
            return 0L;
        }
        return BigDecimal.valueOf(renewAmount).multiply(rate).setScale(0, RoundingMode.DOWN).longValue();
    }

    /**
     * 提现手续费：按分向下取整（1000 元 → 60 元；33.33 元 → 1.99 元）。
     *
     * @param amount 提现金额（分）
     * @param rate   手续费率，例如 0.06
     * @return 手续费（分）
     */
    public static long withdrawFee(long amount, BigDecimal rate) {
        if (amount <= 0) {
            return 0L;
        }
        BigDecimal safeRate = rate == null || rate.signum() < 0 ? BigDecimal.ZERO : rate;
        return BigDecimal.valueOf(amount).multiply(safeRate).setScale(0, RoundingMode.DOWN).longValue();
    }
}
