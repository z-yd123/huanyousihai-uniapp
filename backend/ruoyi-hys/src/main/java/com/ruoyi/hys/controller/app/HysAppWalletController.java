package com.ruoyi.hys.controller.app;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.hys.config.HysMemberContext;
import com.ruoyi.hys.domain.HysFundApply;
import com.ruoyi.hys.domain.HysWalletTxn;
import com.ruoyi.hys.domain.dto.HysAppDtos;
import com.ruoyi.hys.service.HysFundService;
import com.ruoyi.hys.service.HysWalletService;

/**
 * 小程序 · 钱包（余额、充值、互转、提现、流水）。
 */
@Anonymous
@RestController
@RequestMapping("/app/wallet")
public class HysAppWalletController extends BaseController {

    private final HysWalletService walletService;

    private final HysFundService fundService;

    public HysAppWalletController(HysWalletService walletService, HysFundService fundService) {
        this.walletService = walletService;
        this.fundService = fundService;
    }

    /**
     * 钱包概览：余额、冻结、累计佣金、累计充值提现。
     */
    @GetMapping("/summary")
    public AjaxResult summary() {
        return success(walletService.summary(HysMemberContext.requireMemberId()));
    }

    /**
     * 余额流水。
     */
    @GetMapping("/transactions")
    public TableDataInfo transactions(HysWalletTxn query) {
        query.setMemberId(HysMemberContext.requireMemberId());
        startPage();
        List<HysWalletTxn> list = walletService.listTxn(query.getMemberId(), query);
        return getDataTable(list);
    }

    /**
     * 会员互转：一对一转账，双方余额在同一事务内更新。
     *
     * <p>
     * 幂等：请求体必须携带 {@code requestId}，重复提交（例如网络重试）直接返回上次结果，不会二次扣款。
     * </p>
     */
    @PostMapping("/transfer")
    public AjaxResult transfer(@Validated @RequestBody HysAppDtos.Transfer body) {
        walletService.transfer(HysMemberContext.requireMemberId(), body);
        return AjaxResult.success("转账成功");
    }

    /**
     * 提现试算（手续费 6%，按分向下取整）。
     */
    @GetMapping("/withdraw/quote")
    public AjaxResult withdrawQuote(@RequestParam Long amount) {
        return success(walletService.withdrawQuote(amount));
    }

    /**
     * 提现申请（人工审核）。
     */
    @PostMapping("/withdraw")
    public AjaxResult withdraw(@Validated @RequestBody HysAppDtos.Withdraw body) {
        HysFundApply apply = walletService.applyWithdraw(HysMemberContext.requireMemberId(), body);
        return AjaxResult.success("提现申请已提交，等待人工审核", apply);
    }

    /**
     * 充值登记（单笔 ≤ 5000 元）。
     */
    @PostMapping("/recharge")
    public AjaxResult recharge(@Validated @RequestBody HysAppDtos.Recharge body) {
        HysFundApply apply = walletService.applyRecharge(HysMemberContext.requireMemberId(), body);
        return AjaxResult.success("充值登记已提交，运营核对到账后入余额", apply);
    }

    /**
     * 我的充值 / 提现申请记录。
     */
    @GetMapping("/applies")
    public TableDataInfo applies(HysFundApply query) {
        startPage();
        List<HysFundApply> list = fundService.list(HysMemberContext.requireMemberId(), query);
        return getDataTable(list);
    }
}
