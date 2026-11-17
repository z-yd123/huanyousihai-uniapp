package com.ruoyi.hys.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hys.domain.HysOrder;

/**
 * 订单数据访问。
 */
public interface HysOrderMapper {

    List<HysOrder> selectOrderList(HysOrder query);

    HysOrder selectOrderById(Long orderId);

    HysOrder selectOrderByNo(String orderNo);

    int insertOrder(HysOrder order);

    int updateOrder(HysOrder order);

    /**
     * 状态机流转：仅当当前状态等于 {@code from} 时才更新，返回 0 表示状态已被并发修改 / 不合法。
     */
    int transitStatus(@Param("orderId") Long orderId, @Param("from") int from, @Param("to") int to);

    int markPaid(@Param("orderId") Long orderId, @Param("payType") String payType, @Param("payAmount") long payAmount);

    int markAfterSale(@Param("orderId") Long orderId, @Param("reason") String reason);

    int markRefunded(@Param("orderId") Long orderId);

    int rejectAfterSale(@Param("orderId") Long orderId);

    long countAll();

    long countToday(@Param("start") LocalDateTime start);

    long countByStatus(@Param("status") int status);

    long countByProductId(Long productId);

    long sumPaidAmount();

    /** 近 N 日按天统计（下单量 / 实付额），用于运营看板趋势 */
    List<java.util.Map<String, Object>> selectDailyTrend(@Param("start") LocalDateTime start);
}
