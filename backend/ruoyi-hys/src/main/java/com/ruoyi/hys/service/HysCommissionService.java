package com.ruoyi.hys.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.domain.HysCommission;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.domain.HysPayoutGap;
import com.ruoyi.hys.domain.vo.HysVos;
import com.ruoyi.hys.engine.CommissionEngine;
import com.ruoyi.hys.mapper.HysCommissionMapper;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.mapper.HysOrderMapper;
import com.ruoyi.hys.mapper.HysPayoutGapMapper;

/**
 * 佣金服务：结算、明细、售后退回、异常补绑重算。
 *
 * <p>
 * 拨比计算全部委托给 {@link CommissionEngine}（纯函数 + 单测覆盖），本类只负责：
 * 取参与人快照 → 调引擎 → 落库 → 入账 → 异常落对账表。这样
 * 「算法口径」与「数据读写」分离，改口径只改引擎与单测，不会误伤资金链路。
 * </p>
 */
@Service
public class HysCommissionService {

    private static final Logger log = LoggerFactory.getLogger(HysCommissionService.class);

    private final HysCommissionMapper commissionMapper;

    private final HysPayoutGapMapper gapMapper;

    private final HysMemberMapper memberMapper;

    private final HysOrderMapper orderMapper;

    private final HysWalletService walletService;

    private final HysConfigService configService;

    public HysCommissionService(HysCommissionMapper commissionMapper, HysPayoutGapMapper gapMapper,
            HysMemberMapper memberMapper, HysOrderMapper orderMapper, HysWalletService walletService,
            HysConfigService configService) {
        this.commissionMapper = commissionMapper;
        this.gapMapper = gapMapper;
        this.memberMapper = memberMapper;
        this.orderMapper = orderMapper;
        this.walletService = walletService;
        this.configService = configService;
    }

    /**
     * 升级订单完成支付后结算佣金。
     *
     * <p>
     * 幂等：{@code hys_commission} 上有 (order_id, member_id, level) 唯一键，
     * 重复回调不会重复发钱；流水侧还有 biz_key 唯一键兜底。
     * </p>
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleVipUpgrade(Long orderId) {
        HysOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在，无法计佣");
        }
        if (!commissionMapper.selectByOrderId(orderId).isEmpty()) {
            log.info("订单 {} 已计佣，跳过", order.getOrderNo());
            return;
        }
        HysMember buyer = memberMapper.selectMemberById(order.getMemberId());
        if (buyer == null) {
            throw new ServiceException("下单会员不存在，无法计佣");
        }

        HysMember referrer = buyer.getReferrerId() == null ? null
                : memberMapper.selectMemberById(buyer.getReferrerId());
        Long indirectId = referrer == null ? null : referrer.getReferrerId();

        CommissionEngine.Subject subject = new CommissionEngine.Subject(
                order.getOrderNo(),
                buyer.getMemberId(),
                buyer.getReferrerId(),
                buyer.getDirectSeq() == null ? 0 : buyer.getDirectSeq(),
                indirectId);

        CommissionEngine.Settlement settlement;
        try {
            settlement = CommissionEngine.settle(subject, configService.payoutRule());
        } catch (IllegalStateException e) {
            // 配置被改坏属于系统故障，必须阻断而不是少发
            log.error("[COMPLIANCE] 订单 {} 计佣失败：{}", order.getOrderNo(), e.getMessage());
            throw new ServiceException("佣基配置异常，已阻止计佣并告警，请联系技术处理");
        }

        if (!settlement.settled()) {
            recordGap(order, settlement.gap());
            return;
        }

        for (CommissionEngine.Leg leg : settlement.legs()) {
            HysCommission commission = new HysCommission();
            commission.setMemberId(leg.memberId());
            commission.setOrderId(order.getOrderId());
            commission.setOrderNo(order.getOrderNo());
            commission.setLevel(leg.level());
            commission.setAmount(leg.amount());
            commission.setStatus(HysConstants.COMMISSION_SETTLED);
            commission.setRemark(leg.remark());
            commission.setSettleTime(LocalDateTime.now());
            commissionMapper.insertCommission(commission);

            walletService.credit(leg.memberId(), leg.amount(), HysConstants.TXN_COMMISSION, 0L, buyer.getMemberId(),
                    "COMM:" + order.getOrderId() + ":" + leg.memberId() + ":" + leg.level(),
                    "佣金入账 · " + leg.remark());
        }
        log.info("订单 {} 计佣完成，合计 {} 分，受益人 {} 人", order.getOrderNo(), settlement.totalAmount(),
                settlement.legs().size());
    }

    /**
     * 售后退款时回冲佣金：已入账的佣金置为「已回冲」，并从受益人余额扣回（允许扣成负数，后续收入抵扣）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void reverseByOrder(Long orderId, String reason) {
        List<HysCommission> list = commissionMapper.selectByOrderId(orderId);
        for (HysCommission item : list) {
            if (item.getStatus() != null && item.getStatus() == HysConstants.COMMISSION_SETTLED) {
                commissionMapper.updateStatus(item.getCommissionId(), HysConstants.COMMISSION_REVERSED);
                memberMapper.changeBalanceAny(item.getMemberId(), -item.getAmount());
                HysMember member = memberMapper.selectMemberById(item.getMemberId());
                walletService.creditReverse(item.getMemberId(), item.getAmount(), "COMM_REV:" + item.getCommissionId(),
                        "订单 " + item.getOrderNo() + " 退款，佣金回冲：" + (reason == null ? "" : reason),
                        member == null ? 0L : member.getBalance());
            }
        }
    }

    /**
     * 漏发记录（对账表）落库并告警。
     */
    private void recordGap(HysOrder order, CommissionEngine.Gap gap) {
        HysPayoutGap record = new HysPayoutGap();
        record.setOrderId(order.getOrderId());
        record.setOrderNo(order.getOrderNo());
        record.setAmount(gap.amount());
        record.setReason(gap.reason());
        record.setDetail(gap.detail());
        try {
            gapMapper.insertGap(record);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("订单 {} 的异常对账记录已存在，忽略重复写入", order.getOrderNo());
            return;
        }
        // 生产环境这里应接企业微信/短信告警；本项目以 ERROR 日志 + 后台对账页呈现
        log.error("[COMPLIANCE] 计佣中断，已写入异常对账：订单 {} 原因 {} 金额 {} 分", order.getOrderNo(),
                gap.reason(), gap.amount());
    }

    /**
     * 补绑推荐人后重算历史异常订单。
     *
     * @param memberId 被补绑的会员
     * @return 重算成功的订单数
     */
    @Transactional(rollbackFor = Exception.class)
    public int recomputeByMember(Long memberId) {
        HysOrder query = new HysOrder();
        query.setMemberId(memberId);
        List<HysOrder> orders = orderMapper.selectOrderList(query);
        int fixed = 0;
        for (HysOrder order : orders) {
            HysPayoutGap gap = gapMapper.selectByOrderId(order.getOrderId());
            if (gap == null || !"0".equals(gap.getHandled())) {
                continue;
            }
            settleVipUpgrade(order.getOrderId());
            if (!commissionMapper.selectByOrderId(order.getOrderId()).isEmpty()) {
                gap.setHandled("1");
                gap.setHandleRemark("补绑推荐人后自动重算成功");
                gapMapper.updateGap(gap);
                fixed++;
            }
        }
        return fixed;
    }

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    public HysVos.CommissionSummary summary(Long memberId) {
        long direct = commissionMapper.sumAmount(memberId, CommissionEngine.LEVEL_DIRECT,
                HysConstants.COMMISSION_SETTLED);
        long indirect = commissionMapper.sumAmount(memberId, CommissionEngine.LEVEL_INDIRECT,
                HysConstants.COMMISSION_SETTLED);
        int count = commissionMapper.countByMember(memberId, null);
        long pending = commissionMapper.sumAmount(memberId, null, 0);
        return new HysVos.CommissionSummary(direct + indirect, direct, indirect, pending, count,
                "仅两级佣金（直推 + 间推），每笔 398 订单合计拨出 200 元");
    }

    public List<HysCommission> list(Long memberId, HysCommission query) {
        query.setMemberId(memberId);
        return commissionMapper.selectCommissionList(query);
    }

    public List<HysCommission> adminList(HysCommission query) {
        return commissionMapper.selectCommissionList(query);
    }

    public List<HysPayoutGap> gapList(HysPayoutGap query) {
        return gapMapper.selectGapList(query);
    }

    /**
     * 人工核销异常对账（确认不再发放，例如确实属于绕过建档的测试数据）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeGap(Long gapId, String remark) {
        HysPayoutGap gap = gapMapper.selectGapById(gapId);
        if (gap == null) {
            throw new ServiceException("对账记录不存在");
        }
        if (!"0".equals(gap.getHandled())) {
            throw new ServiceException("该记录已处理，请刷新");
        }
        gap.setHandled("2");
        gap.setHandleRemark(remark == null || remark.isBlank() ? "人工核销，确认不再发放" : remark);
        gapMapper.updateGap(gap);
        log.warn("异常对账 {} 被人工核销：{}", gapId, gap.getHandleRemark());
    }

    /**
     * 今日佣金合计（看板用）。
     */
    public long todayTotal() {
        return commissionMapper.sumToday(LocalDate.now().atStartOfDay());
    }
}
