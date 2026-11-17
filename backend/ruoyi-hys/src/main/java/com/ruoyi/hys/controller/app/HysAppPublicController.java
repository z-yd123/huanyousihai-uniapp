package com.ruoyi.hys.controller.app;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.engine.CommissionEngine;
import com.ruoyi.hys.service.HysConfigService;

/**
 * 小程序 · 公开配置（登录页、注册页也要用的口径，无需令牌）。
 *
 * <p>
 * 只暴露「对外口径」类参数：398 价格、续费价、充值上限、合规文案。
 * 拨比明细、费率配置一律不出，避免被用来推算佣金结构。
 * </p>
 */
@Anonymous
@RestController
@RequestMapping("/app/public")
public class HysAppPublicController extends BaseController {

    private final HysConfigService configService;

    public HysAppPublicController(HysConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config")
    public AjaxResult config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("vipPrice", configService.getLong(HysConstants.CFG_VIP_PRICE, 39_800L));
        data.put("vipRenewPrice", configService.getLong(HysConstants.CFG_VIP_RENEW_PRICE, 9_900L));
        data.put("rechargeLimit", configService.rechargeLimit());
        data.put("minWithdraw", HysConstants.MIN_WITHDRAW);
        data.put("withdrawFeeRate", configService.withdrawFeeRate());
        data.put("commissionDepth", CommissionEngine.MAX_DEPTH);
        data.put("payoutTotal", CommissionEngine.PAYOUT_TOTAL);
        data.put("notice", configService.appNotice());
        data.put("vipValueAnchor", 398_000L);
        return success(data);
    }
}
