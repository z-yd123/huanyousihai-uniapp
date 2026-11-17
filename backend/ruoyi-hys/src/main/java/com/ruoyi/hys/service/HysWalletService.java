package com.ruoyi.hys.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.domain.HysFundApply;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysWalletTxn;
import com.ruoyi.hys.domain.dto.HysAppDtos;
import com.ruoyi.hys.domain.vo.HysVos;
import com.ruoyi.hys.engine.CommissionEngine;
import com.ruoyi.hys.mapper.HysCommissionMapper;
import com.ruoyi.hys.mapper.HysFundApplyMapper;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.mapper.HysWalletTxnMapper;

/**
 * 钱包服务：余额、充值登记、互转、提现、流水。
 *
 * <p>
 * 一致性设计（AGENT.md §7）：
 * </p>
 * <ul>
 * <li>所有余额变动都用<b>条件更新</b>（{@code where balance >= amount}），不做「先查后改」，
 * 并发下不会出现余额覆盖或透支；</li>
 * <li>互转是「先扣后加」两条更新 + 两条流水，同一个事务内完成，任一步失败整体回滚，不会单边账；</li>
 * <li>每条流水都有唯一 {@code biz_key}，重试/重复点击只会命中已有记录，不会重复入账。</li>
 * </ul>
 */
@Service
public class HysWalletService {

    private static final Logger log = LoggerFactory.getLogger(HysWalletService.class);

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final HysMemberMapper memberMapper;

    private final HysWalletTxnMapper txnMapper;

    private final HysFundApplyMapper fundApplyMapper;

    private final HysCommissionMapper commissionMapper;

    private final HysConfigService configService;

    public HysWalletService(HysMemberMapper memberMapper, HysWalletTxnMapper txnMapper,
            HysFundApplyMapper fundApplyMapper, HysCommissionMapper commissionMapper,
            HysConfigService configService) {
        this.memberMapper = memberMapper;
        this.txnMapper = txnMapper;
        this.fundApplyMapper = fundApplyMapper;
        this.commissionMapper = commissionMapper;
        this.configService = configService;
    }

    /**
     * 钱包概览。
     */
    public HysVos.WalletSummary summary(Long memberId) {
        HysMember member = requireMember(memberId);
        long commissionTotal = commissionMapper.sumAmount(memberId, null, HysConstants.COMMISSION_SETTLED);
        long rechargeTotal = fundApplyMapper.sumPassedByMember(memberId, HysConstants.APPLY_RECHARGE);
        long withdrawTotal = fundApplyMapper.sumPassedByMember(memberId, HysConstants.APPLY_WITHDRAW);
        return new HysVos.WalletSummary(member.getBalance(), member.getFrozen(), commissionTotal, rechargeTotal,
                withdrawTotal, member.getGreenPoints(), configService.appNotice());
    }

    public List<HysWalletTxn> listTxn(Long memberId, HysWalletTxn query) {
        query.setMemberId(memberId);
        List<HysWalletTxn> list = txnMapper.selectTxnList(query);
        list.forEach(txn -> {
            if (txn.getCounterpartyName() != null) {
                txn.setRemark(txn.getRemark());
            }
        });
        return list;
    }

    /**
     * 提现试算：手续费按分向下取整（AGENT.md Q-01：6%）。
     */
    public HysVos.WithdrawQuote withdrawQuote(long amount) {
        if (amount <= 0) {
            throw new ServiceException("请填写提现金额");
        }
        long fee = CommissionEngine.withdrawFee(amount, configService.withdrawFeeRate());
        return new HysVos.WithdrawQuote(amount, fee, amount - fee, "6%（按分向下取整）");
    }

    /**
     * 充值登记：单笔不得超过 5000 元（AGENT.md §0.3 第 3 条），超出必须拆分。
     */
    @Transactional(rollbackFor = Exception.class)
    public HysFundApply applyRecharge(Long memberId, HysAppDtos.Recharge body) {
        long limit = configService.rechargeLimit();
        if (body.amount() > limit) {
            long times = (body.amount() + limit - 1) / limit;
            throw new ServiceException("单笔充值不超过 " + (limit / 100) + " 元，请拆分为 " + times + " 次充值");
        }
        requireMember(memberId);
        HysFundApply apply = new HysFundApply();
        apply.setApplyNo("FA" + LocalDateTime.now().format(NO_FMT) + memberId);
        apply.setMemberId(memberId);
        apply.setApplyType(HysConstants.APPLY_RECHARGE);
        apply.setAmount(body.amount());
        apply.setFee(0L);
        apply.setActualAmount(body.amount());
        apply.setRemark(body.remark());
        fundApplyMapper.insertApply(apply);
        log.info("会员 {} 提交充值登记 {} 分", memberId, body.amount());
        return apply;
    }

    /**
     * 提现申请：先冻结余额，运营审核通过后解冻并出账；驳回则解冻回可用余额。
     */
    @Transactional(rollbackFor = Exception.class)
    public HysFundApply applyWithdraw(Long memberId, HysAppDtos.Withdraw body) {
        if (body.amount() < HysConstants.MIN_WITHDRAW) {
            throw new ServiceException("单笔提现不低于 1 元");
        }
        long fee = CommissionEngine.withdrawFee(body.amount(), configService.withdrawFeeRate());
        HysMember member = requireMember(memberId);
        if (member.getBalance() < body.amount()) {
            throw new ServiceException("可用余额不足，当前余额 " + formatYuan(member.getBalance()) + " 元");
        }
        if (memberMapper.freezeBalance(memberId, body.amount()) == 0) {
            throw new ServiceException("可用余额不足或正在处理其他提现，请刷新后重试");
        }
        HysFundApply apply = new HysFundApply();
        apply.setApplyNo("FD" + LocalDateTime.now().format(NO_FMT) + memberId);
        apply.setMemberId(memberId);
        apply.setApplyType(HysConstants.APPLY_WITHDRAW);
        apply.setAmount(body.amount());
        apply.setFee(fee);
        apply.setActualAmount(body.amount() - fee);
        apply.setAccountName(body.accountName());
        apply.setAccountNo(body.accountNo());
        apply.setBankName(body.bankName());
        apply.setRemark("提现 " + formatYuan(body.amount()) + " 元，手续费 " + formatYuan(fee) + " 元");
        fundApplyMapper.insertApply(apply);
        log.info("会员 {} 提交提现 {} 分，手续费 {} 分", memberId, body.amount(), fee);
        return apply;
    }

    /**
     * 余额互转。
     *
     * <p>
     * 风控：单日次数与金额上限来自配置；同一 requestId 重试直接返回成功（幂等）。
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long memberId, HysAppDtos.Transfer body) {
        if (body.amount() <= 0) {
            throw new ServiceException("转账金额必须大于 0");
        }
        String outBizKey = "TRANSFER:" + body.requestId() + ":OUT";
        if (txnMapper.selectByBizKey(outBizKey) != null) {
            // 幂等重放：上一次已经成功，直接返回成功，不重复扣款
            return;
        }
        HysMember from = requireMember(memberId);
        HysMember to = memberMapper.selectMemberByPhone(body.toPhone());
        if (to == null) {
            throw new ServiceException("对方账号不存在，请核对手机号");
        }
        if (to.getMemberId().equals(memberId)) {
            throw new ServiceException("不能给自己转账");
        }
        if (HysConstants.MEMBER_DISABLED.equals(to.getStatus())) {
            throw new ServiceException("对方账号已停用，无法转入");
        }
        // 风控：单日次数与累计金额
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int dailyCount = txnMapper.countTransferToday(memberId, todayStart);
        int maxCount = configService.getInt(HysConstants.CFG_TRANSFER_DAILY_COUNT,
                HysConstants.DEFAULT_TRANSFER_DAILY_COUNT);
        if (dailyCount >= maxCount) {
            throw new ServiceException("今日互转次数已达上限（" + maxCount + " 次），请明日再试");
        }
        long todayAmount = txnMapper.sumByTypes(memberId, List.of(HysConstants.TXN_TRANSFER_OUT), false);
        long dailyLimit = configService.getLong(HysConstants.CFG_TRANSFER_DAILY_LIMIT,
                HysConstants.DEFAULT_TRANSFER_DAILY_LIMIT);
        if (todayAmount + body.amount() > dailyLimit) {
            throw new ServiceException("今日互转累计金额不能超过 " + formatYuan(dailyLimit) + " 元");
        }
        if (from.getBalance() < body.amount()) {
            throw new ServiceException("可用余额不足，当前余额 " + formatYuan(from.getBalance()) + " 元");
        }

        if (memberMapper.changeBalance(memberId, -body.amount()) == 0) {
            throw new ServiceException("可用余额不足，请刷新后重试");
        }
        if (memberMapper.changeBalance(to.getMemberId(), body.amount()) == 0) {
            // 目标账户异常（被停用/删除）时整体回滚，绝不单边扣款
            throw new ServiceException("对方账户状态异常，转账已取消");
        }

        HysMember afterOut = requireMember(memberId);
        HysMember afterIn = requireMember(to.getMemberId());
        String remark = body.remark() == null || body.remark().isBlank() ? "余额互转" : body.remark();
        try {
            insertTxn(memberId, HysConstants.TXN_TRANSFER_OUT, -body.amount(), 0L, afterOut.getBalance(),
                    to.getMemberId(), outBizKey, "转给 " + to.getNickname() + "：" + remark);
            insertTxn(to.getMemberId(), HysConstants.TXN_TRANSFER_IN, body.amount(), 0L, afterIn.getBalance(),
                    memberId, "TRANSFER:" + body.requestId() + ":IN", "来自 " + from.getNickname() + "：" + remark);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("该笔转账已处理，请勿重复提交");
        }
        log.info("会员 {} 向会员 {} 互转 {} 分", memberId, to.getMemberId(), body.amount());
    }

    // ------------------------------------------------------------------
    // 供订单 / 佣金服务调用的原子记账能力
    // ------------------------------------------------------------------

    /**
     * 扣款（余额支付）。余额不足抛业务异常。
     */
    @Transactional(rollbackFor = Exception.class)
    public void debit(Long memberId, long amount, String txnType, String bizKey, String remark) {
        if (amount <= 0) {
            throw new ServiceException("扣款金额必须大于 0");
        }
        if (memberMapper.changeBalance(memberId, -amount) == 0) {
            throw new ServiceException("可用余额不足，请先充值或选择互转归集");
        }
        HysMember member = requireMember(memberId);
        insertTxn(memberId, txnType, -amount, 0L, member.getBalance(), null, bizKey, remark);
    }

    /**
     * 入账（充值通过 / 佣金 / 退款）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void credit(Long memberId, long amount, String txnType, long fee, Long counterpartyId, String bizKey,
            String remark) {
        if (amount <= 0) {
            throw new ServiceException("入账金额必须大于 0");
        }
        if (memberMapper.changeBalance(memberId, amount) == 0) {
            throw new ServiceException("入账失败：会员不存在");
        }
        HysMember member = requireMember(memberId);
        insertTxn(memberId, txnType, amount, fee, member.getBalance(), counterpartyId, bizKey, remark);
    }

    /**
     * 佣金回冲流水（余额已由调用方扣减，这里只记一笔负向流水）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void creditReverse(Long memberId, long amount, String bizKey, String remark, long balanceAfter) {
        insertTxn(memberId, HysConstants.TXN_COMMISSION_REVERSE, -amount, 0L, balanceAfter, null, bizKey, remark);
    }

    /**
     * 提现通过：冻结金额出账（余额在申请时已扣减，此处只清冻结）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleWithdraw(HysFundApply apply) {
        if (memberMapper.releaseFrozen(apply.getMemberId(), apply.getAmount()) == 0) {
            throw new ServiceException("冻结金额不足，可能已被处理，请刷新后重试");
        }
        HysMember member = requireMember(apply.getMemberId());
        insertTxn(apply.getMemberId(), HysConstants.TXN_WITHDRAW, -apply.getAmount(), apply.getFee(),
                member.getBalance(), null, "WITHDRAW:" + apply.getApplyId(),
                "提现到 " + apply.getBankName() + "，手续费 " + formatYuan(apply.getFee()) + " 元");
    }

    /**
     * 提现驳回：冻结金额退回可用余额。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelWithdraw(HysFundApply apply) {
        if (memberMapper.unfreezeBalance(apply.getMemberId(), apply.getAmount()) == 0) {
            throw new ServiceException("冻结金额不足，可能已被处理，请刷新后重试");
        }
        HysMember member = requireMember(apply.getMemberId());
        insertTxn(apply.getMemberId(), HysConstants.TXN_WITHDRAW_UNFREEZE, apply.getAmount(), 0L,
                member.getBalance(), null, "WITHDRAW_FAIL:" + apply.getApplyId(),
                "提现被驳回，金额退回可用余额");
    }

    public HysMember requireMember(Long memberId) {
        HysMember member = memberMapper.selectMemberById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在");
        }
        return member;
    }

    private void insertTxn(Long memberId, String type, long amount, long fee, long balanceAfter, Long counterpartyId,
            String bizKey, String remark) {
        HysWalletTxn txn = new HysWalletTxn();
        txn.setMemberId(memberId);
        txn.setTxnType(type);
        txn.setAmount(amount);
        txn.setFee(fee);
        txn.setBalanceAfter(balanceAfter);
        txn.setCounterpartyId(counterpartyId);
        txn.setBizKey(bizKey);
        txn.setRemark(remark);
        txnMapper.insertTxn(txn);
    }

    /**
     * 分转元展示（仅用于文案，计算一律用分）。
     */
    public static String formatYuan(long cents) {
        return String.format("%.2f", cents / 100.0);
    }
}
