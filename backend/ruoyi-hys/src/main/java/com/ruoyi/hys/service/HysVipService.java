package com.ruoyi.hys.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.engine.CommissionEngine;
import com.ruoyi.hys.mapper.HysMemberMapper;

/**
 * VIP 会员服务：开通与续费。
 *
 * <p>
 * 续费口径（AGENT.md §4.5、Q-03）：99 元/年，<b>续费同样走余额支付</b>；
 * 是否计佣由 {@code payout.renew_commission_rate} 决定，当前默认 0（会上未确认比例），
 * 接口会明确返回「待确认」，<b>不静默发钱</b>。
 * </p>
 */
@Service
public class HysVipService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HysMemberMapper memberMapper;

    private final HysWalletService walletService;

    private final HysConfigService configService;

    private final HysMemberService memberService;

    public HysVipService(HysMemberMapper memberMapper, HysWalletService walletService,
            HysConfigService configService, HysMemberService memberService) {
        this.memberMapper = memberMapper;
        this.walletService = walletService;
        this.configService = configService;
        this.memberService = memberService;
    }

    /**
     * 续费 VIP（余额支付）。
     *
     * @return 续费结果（金额、原到期、新到期、是否计佣说明）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> renew(Long memberId) {
        HysMember member = memberService.requireMember(memberId);
        if (!HysConstants.LEVEL_VIP.equals(member.getLevel())) {
            throw new ServiceException("您还不是 VIP 会员，请先购买 398 权益开通");
        }
        long price = configService.getLong(HysConstants.CFG_VIP_RENEW_PRICE, 9_900L);
        long before = member.getBalance() == null ? 0L : member.getBalance();
        if (before < price) {
            throw new ServiceException("余额不足，续费需 " + HysWalletService.formatYuan(price) + " 元");
        }
        walletService.debit(memberId, price, HysConstants.TXN_VIP_RENEW, "VIP_RENEW:" + memberId + ":"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                "VIP 会员续费 " + HysWalletService.formatYuan(price) + " 元");
        LocalDateTime expireAt = memberService.activateVip(memberId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("amount", price);
        result.put("vipExpireAt", expireAt.format(FMT));
        long renewRate = configService.getLong(HysConstants.CFG_RENEW_COMMISSION_RATE, 0L);
        if (renewRate > 0) {
            // 预留：Q-03 确认比例后在此按直推/间推规则发放，当前不会进入该分支
            result.put("commission", CommissionEngine.renewCommission(price, java.math.BigDecimal.valueOf(renewRate)));
            result.put("note", "续费佣金已按配置比例发放");
        } else {
            result.put("commission", 0);
            result.put("note", "Q-03 待确认：续费暂不计佣，比例确认后改配置即可生效");
        }
        return result;
    }
}
