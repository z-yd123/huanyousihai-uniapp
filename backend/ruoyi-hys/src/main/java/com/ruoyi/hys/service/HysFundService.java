package com.ruoyi.hys.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.domain.HysFundApply;
import com.ruoyi.hys.domain.dto.HysAdminDtos;
import com.ruoyi.hys.mapper.HysFundApplyMapper;
import com.ruoyi.hys.util.HysMasks;

/**
 * 充值 / 提现审核服务。
 *
 * <p>
 * 审核是资金链路上唯一的「人工闸门」，因此做了三重保护：
 * </p>
 * <ol>
 * <li>状态机：只有待审核（status=0）可审核，条件更新保证并发重复点击只生效一次；</li>
 * <li>先流转后出账：状态流转与余额变动在同一事务，任何一步失败全部回滚；</li>
 * <li>幂等键：入账 / 出账流水都以 applyId 为 biz_key，重复审核不会重复打钱。</li>
 * </ol>
 */
@Service
public class HysFundService {

    private static final Logger log = LoggerFactory.getLogger(HysFundService.class);

    private final HysFundApplyMapper fundApplyMapper;

    private final HysWalletService walletService;

    public HysFundService(HysFundApplyMapper fundApplyMapper, HysWalletService walletService) {
        this.fundApplyMapper = fundApplyMapper;
        this.walletService = walletService;
    }

    public List<HysFundApply> list(Long memberId, HysFundApply query) {
        if (memberId != null) {
            query.setMemberId(memberId);
        }
        List<HysFundApply> list = fundApplyMapper.selectApplyList(query);
        // 银行卡号脱敏后再出接口，后台列表也不需要完整卡号
        list.forEach(item -> item.setAccountNo(HysMasks.account(item.getAccountNo())));
        return list;
    }

    public HysFundApply get(Long applyId) {
        HysFundApply apply = fundApplyMapper.selectApplyById(applyId);
        if (apply == null) {
            throw new ServiceException("申请不存在");
        }
        apply.setAccountNo(HysMasks.account(apply.getAccountNo()));
        return apply;
    }

    /**
     * 审核。
     *
     * @param applyId 申请ID
     * @param body    审核结果与意见
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long applyId, HysAdminDtos.Audit body) {
        HysFundApply apply = fundApplyMapper.selectApplyById(applyId);
        if (apply == null) {
            throw new ServiceException("申请不存在");
        }
        if (apply.getStatus() != HysConstants.APPLY_PENDING) {
            throw new ServiceException("该申请已处理，请刷新列表");
        }
        String auditor = SecurityUtils.getUsername();
        String remark = body.remark() == null ? "" : body.remark();

        if (!body.pass()) {
            if (fundApplyMapper.auditApply(applyId, HysConstants.APPLY_REJECTED, auditor, remark, null) == 0) {
                throw new ServiceException("该申请已被其他人处理，请刷新");
            }
            if (HysConstants.APPLY_WITHDRAW.equals(apply.getApplyType())) {
                // 驳回提现必须把冻结金额退回可用余额，否则用户的钱就被“卡住”了
                walletService.cancelWithdraw(apply);
            }
            log.info("{} 审核驳回 {} 单 {}", auditor, apply.getApplyType(), apply.getApplyNo());
            return;
        }

        if (fundApplyMapper.auditApply(applyId, HysConstants.APPLY_PASSED, auditor, remark, apply.getActualAmount()) == 0) {
            throw new ServiceException("该申请已被其他人处理，请刷新");
        }
        if (HysConstants.APPLY_RECHARGE.equals(apply.getApplyType())) {
            walletService.credit(apply.getMemberId(), apply.getAmount(), HysConstants.TXN_RECHARGE, 0L, null,
                    "RECHARGE:" + applyId, "充值入账 · 审核通过（" + auditor + "）");
        } else {
            walletService.settleWithdraw(apply);
        }
        log.info("{} 审核通过 {} 单 {}，金额 {} 分", auditor, apply.getApplyType(), apply.getApplyNo(), apply.getAmount());
    }

    public long countPending() {
        return fundApplyMapper.countPending();
    }
}
