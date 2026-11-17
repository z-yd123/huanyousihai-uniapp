package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hys.domain.HysFundApply;
import com.ruoyi.hys.domain.dto.HysAdminDtos;
import com.ruoyi.hys.service.HysFundService;

/**
 * 管理端 · 资金审核（充值入账 / 提现出款）。
 */
@RestController
@RequestMapping("/hys/fund")
public class HysAdminFundController extends BaseController {

    private final HysFundService fundService;

    public HysAdminFundController(HysFundService fundService) {
        this.fundService = fundService;
    }

    @PreAuthorize("@ss.hasPermi('hys:fund:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysFundApply query) {
        startPage();
        List<HysFundApply> list = fundService.list(null, query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:fund:list')")
    @GetMapping("/{applyId}")
    public AjaxResult getInfo(@PathVariable Long applyId) {
        return success(fundService.get(applyId));
    }

    /**
     * 审核通过 / 驳回。
     */
    @PreAuthorize("@ss.hasPermi('hys:fund:audit')")
    @Log(title = "环游四海资金审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{applyId}/audit")
    public AjaxResult audit(@PathVariable Long applyId, @Validated @RequestBody HysAdminDtos.Audit body) {
        fundService.audit(applyId, body);
        return success(body.pass() ? "已通过并完成入账 / 出款" : "已驳回");
    }
}
