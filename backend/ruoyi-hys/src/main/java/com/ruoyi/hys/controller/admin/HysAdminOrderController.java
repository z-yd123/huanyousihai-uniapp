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
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.service.HysOrderService;

/**
 * 管理端 · 订单中心（商品与线路统一处理）。
 */
@RestController
@RequestMapping("/hys/order")
public class HysAdminOrderController extends BaseController {

    private final HysOrderService orderService;

    public HysAdminOrderController(HysOrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("@ss.hasPermi('hys:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysOrder query) {
        startPage();
        List<HysOrder> list = orderService.adminList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:order:list')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId) {
        return success(orderService.adminGet(orderId));
    }

    /**
     * 发货 / 确认成团。
     */
    @PreAuthorize("@ss.hasPermi('hys:order:ship')")
    @Log(title = "环游四海订单", businessType = BusinessType.UPDATE)
    @PostMapping("/{orderId}/ship")
    public AjaxResult ship(@PathVariable Long orderId) {
        orderService.ship(orderId);
        return success("已标记发货 / 成团");
    }

    /**
     * 退款：退余额 + 回冲佣金 + 回滚库存，三步在同一事务内完成。
     */
    @PreAuthorize("@ss.hasPermi('hys:order:refund')")
    @Log(title = "环游四海订单", businessType = BusinessType.UPDATE)
    @PostMapping("/{orderId}/refund")
    public AjaxResult refund(@PathVariable Long orderId, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? "" : body.getOrDefault("reason", "运营退款");
        orderService.refund(orderId, reason);
        return success("退款完成，佣金已同步回冲");
    }

    /**
     * 驳回售后。
     */
    @PreAuthorize("@ss.hasPermi('hys:order:refund')")
    @Log(title = "环游四海订单", businessType = BusinessType.UPDATE)
    @PostMapping("/{orderId}/reject")
    public AjaxResult reject(@PathVariable Long orderId) {
        orderService.rejectAfterSale(orderId);
        return success("已驳回售后申请");
    }
}
