package com.ruoyi.hys.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 佣金引擎单元测试，逐条覆盖 AGENT.md §8.2（合规用例）与 §8.3（佣金计算用例）。
 * 纯函数测试，不启动 Spring、不连数据库。
 */
class CommissionEngineTest {

    private static final CommissionEngine.PayoutRule RULE = CommissionEngine.PayoutRule.defaults();

    /** 构造一笔升级订单的计佣入参：newMemberId 固定 900，直推人 800，间推人 700 */
    private static CommissionEngine.Subject subject(int directSeq, Long referrer, Long referrerReferrer) {
        return new CommissionEngine.Subject("HYS-TEST-0001", 900L, referrer, directSeq, referrerReferrer);
    }

    private static long amountOf(CommissionEngine.Settlement settlement, String level) {
        return settlement.legs().stream()
                .filter(leg -> level.equals(leg.level()))
                .mapToLong(CommissionEngine.Leg::amount)
                .findFirst()
                .orElse(0L);
    }

    @Nested
    @DisplayName("§8.3 佣金计算用例")
    class PayoutCases {

        @Test
        @DisplayName("我推荐第 1 人升级：我得 100，我的上级得 100")
        void firstDirect() {
            var result = CommissionEngine.settle(subject(1, 800L, 700L), RULE);
            assertThat(result.settled()).isTrue();
            assertThat(amountOf(result, "DIRECT")).isEqualTo(10_000L);
            assertThat(amountOf(result, "INDIRECT")).isEqualTo(10_000L);
            assertThat(result.totalAmount()).isEqualTo(20_000L);
            assertThat(result.legs()).extracting(CommissionEngine.Leg::memberId).containsExactly(800L, 700L);
        }

        @Test
        @DisplayName("我推荐第 2 人升级：我得 100，我的上级得 100")
        void secondDirect() {
            var result = CommissionEngine.settle(subject(2, 800L, 700L), RULE);
            assertThat(amountOf(result, "DIRECT")).isEqualTo(10_000L);
            assertThat(amountOf(result, "INDIRECT")).isEqualTo(10_000L);
        }

        @Test
        @DisplayName("我推荐第 3 人升级：我得 200，我的上级 0")
        void thirdDirect() {
            var result = CommissionEngine.settle(subject(3, 800L, 700L), RULE);
            assertThat(result.legs()).hasSize(1);
            assertThat(amountOf(result, "DIRECT")).isEqualTo(20_000L);
            assertThat(amountOf(result, "INDIRECT")).isZero();
            assertThat(result.totalAmount()).isEqualTo(20_000L);
        }

        @Test
        @DisplayName("我推荐第 9 人升级：我得 200，我的上级 0")
        void ninthDirect() {
            var result = CommissionEngine.settle(subject(9, 800L, 700L), RULE);
            assertThat(amountOf(result, "DIRECT")).isEqualTo(20_000L);
            assertThat(result.totalAmount()).isEqualTo(20_000L);
        }

        @Test
        @DisplayName("第 3 个直推的下级再推荐一人：下级得 100，我得 100（其序号独立从 1 计）")
        void grandChildUpgrade() {
            var result = CommissionEngine.settle(subject(1, 800L, 700L), RULE);
            assertThat(amountOf(result, "DIRECT")).isEqualTo(10_000L);
            assertThat(amountOf(result, "INDIRECT")).isEqualTo(10_000L);
        }
    }

    @Nested
    @DisplayName("§8.2 合规用例")
    class ComplianceCases {

        @Test
        @DisplayName("A→B→C→D 四层链路：D 升级只惠及 C 与 B，A 不参与（层级 ≤ 2）")
        void fourLevelChainOnlyTwoBenefit() {
            // A=1, B=2, C=3, D=4。D 升级：直推人 C=3，间推人 B=2，A=1 不参与
            var result = CommissionEngine.settle(
                    new CommissionEngine.Subject("HYS-4LEVEL", 4L, 3L, 1, 2L), RULE);
            assertThat(result.legs()).extracting(CommissionEngine.Leg::memberId).containsExactly(3L, 2L);
            assertThat(result.legs()).allMatch(leg -> leg.depth() <= CommissionEngine.MAX_DEPTH);
            assertThat(result.legs()).extracting(CommissionEngine.Leg::memberId).doesNotContain(1L);
        }

        @Test
        @DisplayName("脏数据：会员无推荐人 → 中断计佣并落 referrer_missing，不发 100，不改派他人")
        void referrerMissingAborts() {
            var result = CommissionEngine.settle(subject(1, null, 700L), RULE);
            assertThat(result.settled()).isFalse();
            assertThat(result.legs()).isEmpty();
            assertThat(result.gap().reason()).isEqualTo("referrer_missing");
            assertThat(result.gap().amount()).isEqualTo(20_000L);
        }

        @Test
        @DisplayName("脏数据：直推人无推荐人（无间推人）→ 整单中断，不允许只发 100 元")
        void indirectMissingAborts() {
            var result = CommissionEngine.settle(subject(1, 800L, null), RULE);
            assertThat(result.settled()).isFalse();
            assertThat(result.legs()).isEmpty();
            assertThat(result.gap().reason()).isEqualTo("indirect_missing");
        }

        @Test
        @DisplayName("第 3 个直推起不再需要间推人：仍只发一条 200 元，不因缺间推人而中断")
        void thirdDirectWithoutIndirectIsLegal() {
            var result = CommissionEngine.settle(subject(5, 800L, null), RULE);
            assertThat(result.settled()).isTrue();
            assertThat(result.legs()).hasSize(1);
            assertThat(result.totalAmount()).isEqualTo(20_000L);
        }

        @Test
        @DisplayName("1~30 号直推订单：每单合计恒为 200 元，且受益人层级不超过 2")
        void everyOrderPaysExactly200() {
            for (int seq = 1; seq <= 30; seq++) {
                var result = CommissionEngine.settle(subject(seq, 800L, 700L), RULE);
                assertThat(result.settled()).as("第 %d 个直推", seq).isTrue();
                assertThat(result.totalAmount()).as("第 %d 个直推的拨比", seq).isEqualTo(20_000L);
                assertThat(result.legs()).as("第 %d 个直推的层级", seq)
                        .allMatch(leg -> leg.depth() <= CommissionEngine.MAX_DEPTH);
            }
        }

        @Test
        @DisplayName("拨比规则被篡改（总额不是 200 元）→ 直接抛错，不允许上线")
        void tamperedRuleRejected() {
            var bad = new CommissionEngine.PayoutRule(30_000L, 10_000L, 20_000L, 20_000L);
            assertThatThrownBy(() -> CommissionEngine.settle(subject(1, 800L, 700L), bad))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("[COMPLIANCE]");
        }

        @Test
        @DisplayName("伪造三条明细（含第三级）→ assertCompliance 拦截")
        void fakeThirdLevelRejected() {
            var fake = new CommissionEngine.Settlement(true, List.of(
                    new CommissionEngine.Leg(1L, "DIRECT", 10_000L, 1, ""),
                    new CommissionEngine.Leg(2L, "INDIRECT", 5_000L, 2, ""),
                    new CommissionEngine.Leg(3L, "THIRD", 5_000L, 3, "")), null);
            assertThatThrownBy(() -> CommissionEngine.assertCompliance(fake, RULE))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("层级上限");
        }

        @Test
        @DisplayName("合计不等于拨比 → 断言拦截")
        void wrongTotalRejected() {
            var fake = new CommissionEngine.Settlement(true, List.of(
                    new CommissionEngine.Leg(1L, "DIRECT", 10_000L, 1, "")), null);
            assertThatThrownBy(() -> CommissionEngine.assertCompliance(fake, RULE))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("佣金合计");
        }
    }

    @Nested
    @DisplayName("提现手续费与续费计佣")
    class FeeCases {

        @Test
        @DisplayName("提现 1000 元：手续费 60 元、到账 940 元")
        void withdrawFee1000() {
            long fee = CommissionEngine.withdrawFee(100_000L, new BigDecimal("0.06"));
            assertThat(fee).isEqualTo(6_000L);
            assertThat(100_000L - fee).isEqualTo(94_000L);
        }

        @Test
        @DisplayName("提现 33.33 元：手续费 1.99 元（按分向下取整），到账 31.34 元")
        void withdrawFeeRoundsDown() {
            long fee = CommissionEngine.withdrawFee(3_333L, new BigDecimal("0.06"));
            assertThat(fee).isEqualTo(199L);
            assertThat(3_333L - fee).isEqualTo(3_134L);
        }

        @Test
        @DisplayName("提现 0 元 / 比例为 0：手续费为 0")
        void withdrawFeeZero() {
            assertThat(CommissionEngine.withdrawFee(0L, new BigDecimal("0.06"))).isZero();
            assertThat(CommissionEngine.withdrawFee(100_000L, BigDecimal.ZERO)).isZero();
        }

        @Test
        @DisplayName("Q-03 续费计佣默认比例 0：不静默发钱")
        void renewCommissionDisabled() {
            assertThat(CommissionEngine.renewCommission(9_900L, BigDecimal.ZERO)).isZero();
        }
    }
}
