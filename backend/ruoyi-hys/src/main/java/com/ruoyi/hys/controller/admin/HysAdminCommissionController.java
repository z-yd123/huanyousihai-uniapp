package com.ruoyi.hys.controller.admin;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.hys.domain.HysCommission;
import com.ruoyi.hys.domain.HysPayoutGap;
import com.ruoyi.hys.service.HysCommissionService;

/**
 * 管理端 · 佣金明细与异常对账。
 */
@RestController
@RequestMapping("/hys/commission")
public class HysAdminCommissionController extends BaseController {

    private final HysCommissionService commissionService;

    public HysAdminCommissionController(HysCommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @PreAuthorize("@ss.hasPermi('hys:commission:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysCommission query) {
        startPage();
        List<HysCommission> list = commissionService.adminList(query);
        return getDataTable(list);
    }

    /**
     * 佣金汇总（用于页面顶部统计卡）。
     */
    @PreAuthorize("@ss.hasPermi('hys:commission:list')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        return success(Map.of("today", commissionService.todayTotal()));
    }

    /**
     * 异常对账列表：正常情况下应为空。
     */
    @PreAuthorize("@ss.hasPermi('hys:gap:list')")
    @GetMapping("/gap/list")
    public TableDataInfo gapList(HysPayoutGap query) {
        startPage();
        List<HysPayoutGap> list = commissionService.gapList(query);
        return getDataTable(list);
    }

    /**
     * 人工核销异常记录（确认不再发放）。
     */
    @PreAuthorize("@ss.hasPermi('hys:gap:handle')")
    @Log(title = "佣金异常对账", businessType = BusinessType.UPDATE)
    @PostMapping("/gap/{gapId}/close")
    public AjaxResult closeGap(@PathVariable Long gapId,
            @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        commissionService.closeGap(gapId, remark);
        return success("已核销");
    }
}
