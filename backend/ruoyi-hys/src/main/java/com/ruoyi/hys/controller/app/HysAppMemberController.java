package com.ruoyi.hys.controller.app;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.hys.config.HysMemberContext;
import com.ruoyi.hys.domain.HysCommission;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.vo.HysVos;
import com.ruoyi.hys.service.HysCommissionService;
import com.ruoyi.hys.service.HysMemberService;
import com.ruoyi.hys.service.HysQrCodeService;
import com.ruoyi.hys.service.HysVipService;

/**
 * 小程序 · 个人中心（资料、佣金、我的分享、海报、VIP 续费）。
 */
@Anonymous
@RestController
@RequestMapping("/app/member")
public class HysAppMemberController extends BaseController {

    private final HysMemberService memberService;

    private final HysCommissionService commissionService;

    private final HysQrCodeService qrCodeService;

    private final HysVipService vipService;

    public HysAppMemberController(HysMemberService memberService, HysCommissionService commissionService,
            HysQrCodeService qrCodeService, HysVipService vipService) {
        this.memberService = memberService;
        this.commissionService = commissionService;
        this.qrCodeService = qrCodeService;
        this.vipService = vipService;
    }

    /**
     * 当前会员资料（会员卡 / 个人中心）。
     */
    @GetMapping("/profile")
    public AjaxResult profile() {
        return success(memberService.profile(HysMemberContext.requireMemberId()));
    }

    /**
     * 佣金概览。
     */
    @GetMapping("/commission/summary")
    public AjaxResult commissionSummary() {
        return success(commissionService.summary(HysMemberContext.requireMemberId()));
    }

    /**
     * 佣金明细（只有直推 / 间推两类）。
     */
    @GetMapping("/commission/list")
    public TableDataInfo commissionList(HysCommission query) {
        query.setMemberId(HysMemberContext.requireMemberId());
        startPage();
        List<HysCommission> list = commissionService.list(query.getMemberId(), query);
        return getDataTable(list);
    }

    /**
     * 我的分享 · 概览。
     */
    @GetMapping("/share")
    public AjaxResult share() {
        return success(memberService.shareOverview(HysMemberContext.requireMemberId()));
    }

    /**
     * 我的分享 · 直推名单。
     */
    @GetMapping("/share/directs")
    public TableDataInfo directs() {
        startPage();
        List<HysVos.DirectItem> list = memberService.directList(HysMemberContext.requireMemberId());
        return getDataTable(list);
    }

    /**
     * 邀请二维码（海报底图由前端 canvas 合成，四套模板）。
     */
    @GetMapping("/invite-qr")
    public AjaxResult inviteQr(@RequestParam(required = false) Integer size) {
        HysMember member = HysMemberContext.require();
        return success(qrCodeService.inviteQr(member.getPhone(), size));
    }

    /**
     * VIP 续费（99 元/年，余额支付）。
     */
    @PostMapping("/vip/renew")
    public AjaxResult renewVip() {
        return AjaxResult.success("续费成功", vipService.renew(HysMemberContext.requireMemberId()));
    }
}
