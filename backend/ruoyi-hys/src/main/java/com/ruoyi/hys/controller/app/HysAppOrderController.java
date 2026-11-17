package com.ruoyi.hys.controller.app;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.hys.config.HysMemberContext;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.domain.dto.HysAppDtos;
import com.ruoyi.hys.service.HysOrderService;

/**
 * 小程序 · 订单中心（待付款 / 待收货 / 售后 / 全部）。
 */
@Anonymous
@RestController
@RequestMapping("/app/orders")
public class HysAppOrderController extends BaseController {

    private final HysOrderService orderService;

    public HysAppOrderController(HysOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 下单报名。
     */
    @PostMapping
    public AjaxResult create(@Validated @RequestBody HysAppDtos.CreateOrder body) {
        HysMember member = HysMemberContext.require();
        HysOrder order = orderService.create(member.getMemberId(), member, body);
        return AjaxResult.success("下单成功，请在 24 小时内完成支付", order);
    }

    /**
     * 我的订单列表。
     *
     * @param status 0 待付款 / 1 待收货·待出行 / 3 售后中 / 4 已退款，不传为全部
     */
    @GetMapping
    public TableDataInfo list(HysOrder query) {
        query.setMemberId(HysMemberContext.requireMemberId());
        startPage();
        List<HysOrder> list = orderService.list(query.getMemberId(), query);
        return getDataTable(list);
    }

    /**
     * 订单详情。
     */
    @GetMapping("/{orderId}")
    public AjaxResult detail(@PathVariable Long orderId) {
        return success(orderService.detail(HysMemberContext.requireMemberId(), orderId));
    }

    /**
     * 余额支付。
     */
    @PostMapping("/{orderId}/pay")
    public AjaxResult pay(@PathVariable Long orderId) {
        HysOrder order = orderService.pay(HysMemberContext.requireMemberId(), orderId);
        return AjaxResult.success("支付成功", order);
    }

    /**
     * 取消订单。
     */
    @PostMapping("/{orderId}/cancel")
    public AjaxResult cancel(@PathVariable Long orderId) {
        orderService.cancel(HysMemberContext.requireMemberId(), orderId);
        return success("订单已取消");
    }

    /**
     * 确认收货 / 完成行程。
     */
    @PostMapping("/{orderId}/confirm")
    public AjaxResult confirm(@PathVariable Long orderId) {
        orderService.confirm(HysMemberContext.requireMemberId(), orderId);
        return success("已确认完成");
    }

    /**
     * 申请售后。
     */
    @PostMapping("/{orderId}/aftersale")
    public AjaxResult afterSale(@PathVariable Long orderId,
            @Validated @RequestBody HysAppDtos.AfterSale body) {
        orderService.applyAfterSale(HysMemberContext.requireMemberId(), orderId, body.reason());
        return success("售后申请已提交，运营将在 24 小时内处理");
    }
}
