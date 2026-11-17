package com.ruoyi.hys.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.domain.HysProduct;
import com.ruoyi.hys.domain.dto.HysAppDtos;
import com.ruoyi.hys.mapper.HysOrderMapper;
import com.ruoyi.hys.mapper.HysProductMapper;

/**
 * 订单服务：下单、支付、取消、确认、售后，以及后台发货 / 退款。
 *
 * <p>
 * 状态机集中在此，所有流转都用「带前置状态的条件更新」实现：
 * {@code update ... where order_id = ? and status = ?}，影响行数为 0 即视为并发冲突或非法跳变，
 * 直接抛错让用户刷新——不会出现「待付款订单被直接改成已完成」这类脏数据。
 * </p>
 *
 * <p>
 * 取价：一律由服务端按会员当前 VIP 状态重新计算（{@link HysCatalogService#priceFor}），
 * 前端传入的价格只用于展示，永不参与计算，杜绝改价下单。
 * </p>
 */
@Service
public class HysOrderService {

    private static final Logger log = LoggerFactory.getLogger(HysOrderService.class);

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final HysOrderMapper orderMapper;

    private final HysProductMapper productMapper;

    private final HysCatalogService catalogService;

    private final HysWalletService walletService;

    private final HysCommissionService commissionService;

    private final HysMemberService memberService;

    public HysOrderService(HysOrderMapper orderMapper, HysProductMapper productMapper,
            HysCatalogService catalogService, HysWalletService walletService,
            HysCommissionService commissionService, HysMemberService memberService) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.catalogService = catalogService;
        this.walletService = walletService;
        this.commissionService = commissionService;
        this.memberService = memberService;
    }

    /**
     * 下单报名（生成待付款订单，扣减库存）。
     */
    @Transactional(rollbackFor = Exception.class)
    public HysOrder create(Long memberId, HysMember member, HysAppDtos.CreateOrder body) {
        HysProduct product = productMapper.selectProductById(body.productId());
        if (product == null || !HysConstants.ON_SALE.equals(product.getStatus())) {
            throw new ServiceException("线路或商品已下架");
        }
        boolean isRoute = HysConstants.PRODUCT_ROUTE.equals(product.getProductType());
        if (isRoute && body.travelDate() == null) {
            throw new ServiceException("请选择出行日期");
        }
        if (body.travelDate() != null && body.travelDate().isBefore(LocalDate.now())) {
            throw new ServiceException("出行日期不能早于今天");
        }
        if (product.getStock() == null || product.getStock() < body.quantity()) {
            throw new ServiceException("库存不足，当前仅剩 " + (product.getStock() == null ? 0 : product.getStock()) + " 份");
        }
        long unitPrice = catalogService.priceFor(product, HysCatalogService.isVipActive(member));
        long amount = unitPrice * body.quantity();

        if (productMapper.increaseSales(product.getProductId(), body.quantity()) == 0) {
            throw new ServiceException("库存不足，请稍后重试");
        }

        HysOrder order = new HysOrder();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(memberId);
        order.setProductId(product.getProductId());
        order.setProductType(product.getProductType());
        order.setProductTitle(product.getTitle());
        order.setProductCover(product.getCover());
        order.setUnitPrice(unitPrice);
        order.setQuantity(body.quantity());
        order.setAmount(amount);
        order.setStatus(HysConstants.ORDER_UNPAID);
        order.setTravelDate(body.travelDate());
        order.setContactName(body.contactName());
        order.setContactPhone(body.contactPhone());
        order.setRemark(body.remark());
        orderMapper.insertOrder(order);
        log.info("会员 {} 下单 {} 金额 {} 分", memberId, order.getOrderNo(), amount);
        return order;
    }

    /**
     * 余额支付。支付成功后：
     * <ul>
     * <li>398 权益商品 → 立即升级 VIP，并触发两级佣金结算；</li>
     * <li>其他商品 → 进入待收货 / 待出行，不计佣（本期仅升级订单计佣）。</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public HysOrder pay(Long memberId, Long orderId) {
        HysOrder order = requireOwnedOrder(memberId, orderId);
        if (order.getStatus() != HysConstants.ORDER_UNPAID) {
            throw new ServiceException("该订单当前状态不可支付，请刷新后重试");
        }
        walletService.debit(memberId, order.getAmount(), HysConstants.TXN_ORDER_PAY, "ORDER:" + orderId,
                "支付订单 " + order.getOrderNo());
        if (orderMapper.markPaid(orderId, "BALANCE", order.getAmount()) == 0) {
            throw new ServiceException("订单状态已变更，支付已取消，请刷新");
        }
        if (HysConstants.PRODUCT_VIP_RIGHTS.equals(order.getProductType())) {
            memberService.activateVip(memberId);
            // COMPLIANCE：升级订单必须计佣；异常时落对账并告警，但不影响会员已获得的权益
            commissionService.settleVipUpgrade(orderId);
        }
        HysOrder paid = orderMapper.selectOrderById(orderId);
        log.info("订单 {} 支付成功，会员 {} 余额 {}", order.getOrderNo(), memberId,
                HysWalletService.formatYuan(walletService.requireMember(memberId).getBalance()));
        return paid;
    }

    /**
     * 取消订单（仅待付款可取消，回滚库存）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long memberId, Long orderId) {
        HysOrder order = requireOwnedOrder(memberId, orderId);
        if (orderMapper.transitStatus(orderId, HysConstants.ORDER_UNPAID, HysConstants.ORDER_CANCELLED) == 0) {
            throw new ServiceException("仅待付款订单可取消");
        }
        productMapper.rollbackSales(order.getProductId(), order.getQuantity());
    }

    /**
     * 确认收货 / 完成行程。
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long memberId, Long orderId) {
        requireOwnedOrder(memberId, orderId);
        if (orderMapper.transitStatus(orderId, HysConstants.ORDER_PAID, HysConstants.ORDER_FINISHED) == 0) {
            throw new ServiceException("当前状态不可确认完成");
        }
    }

    /**
     * 申请售后。
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyAfterSale(Long memberId, Long orderId, String reason) {
        requireOwnedOrder(memberId, orderId);
        if (orderMapper.markAfterSale(orderId, reason) == 0) {
            throw new ServiceException("当前订单状态不支持售后申请");
        }
    }

    public HysOrder detail(Long memberId, Long orderId) {
        return requireOwnedOrder(memberId, orderId);
    }

    public List<HysOrder> list(Long memberId, HysOrder query) {
        query.setMemberId(memberId);
        return orderMapper.selectOrderList(query);
    }

    // ------------------------------------------------------------------
    // 后台
    // ------------------------------------------------------------------

    public List<HysOrder> adminList(HysOrder query) {
        return orderMapper.selectOrderList(query);
    }

    public HysOrder adminGet(Long orderId) {
        HysOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        return order;
    }

    /**
     * 发货 / 确认成团。
     */
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long orderId) {
        HysOrder order = adminGet(orderId);
        HysOrder patch = new HysOrder();
        patch.setOrderId(orderId);
        patch.setShipTime(LocalDateTime.now());
        orderMapper.updateOrder(patch);
        log.info("订单 {} 已发货 / 成团", order.getOrderNo());
    }

    /**
     * 退款：退回余额、回冲佣金、回滚库存，状态置为已退款。
     */
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long orderId, String reason) {
        HysOrder order = adminGet(orderId);
        if (order.getStatus() == HysConstants.ORDER_UNPAID) {
            throw new ServiceException("订单未支付，无需退款，可直接取消");
        }
        if (order.getStatus() == HysConstants.ORDER_REFUNDED) {
            throw new ServiceException("订单已退款，请勿重复操作");
        }
        if (orderMapper.markRefunded(orderId) == 0) {
            throw new ServiceException("当前状态不可退款，请刷新");
        }
        long payAmount = order.getPayAmount() == null ? order.getAmount() : order.getPayAmount();
        walletService.credit(order.getMemberId(), payAmount, HysConstants.TXN_REFUND, 0L, null,
                "REFUND:" + orderId, "订单 " + order.getOrderNo() + " 退款到余额");
        commissionService.reverseByOrder(orderId, reason);
        productMapper.rollbackSales(order.getProductId(), order.getQuantity());
        log.warn("订单 {} 已退款 {} 分，原因：{}", order.getOrderNo(), payAmount, reason);
    }

    /**
     * 驳回售后。
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectAfterSale(Long orderId) {
        if (orderMapper.rejectAfterSale(orderId) == 0) {
            throw new ServiceException("当前订单不在售后中");
        }
    }

    // ------------------------------------------------------------------

    /**
     * 数据边界：越权访问他人订单统一返回「订单不存在」，不泄露订单是否存在。
     */
    private HysOrder requireOwnedOrder(Long memberId, Long orderId) {
        HysOrder order = orderMapper.selectOrderById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            throw new ServiceException("订单不存在");
        }
        return order;
    }

    private String generateOrderNo() {
        return "HYS" + LocalDateTime.now().format(NO_FMT)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
