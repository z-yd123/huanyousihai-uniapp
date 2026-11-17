package com.ruoyi.hys.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.service.HysDashboardService;

/**
 * 管理端 · 运营看板。
 */
@RestController
@RequestMapping("/hys/dashboard")
public class HysAdminDashboardController extends BaseController {

    private final HysDashboardService dashboardService;

    public HysAdminDashboardController(HysDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("@ss.hasPermi('hys:dashboard:view')")
    @GetMapping("/overview")
    public AjaxResult overview() {
        return success(dashboardService.overview());
    }
}
