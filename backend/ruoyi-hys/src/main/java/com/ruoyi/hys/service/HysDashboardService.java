package com.ruoyi.hys.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.hys.domain.vo.HysVos;
import com.ruoyi.hys.mapper.HysCommissionMapper;
import com.ruoyi.hys.mapper.HysFundApplyMapper;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.mapper.HysOrderMapper;
import com.ruoyi.hys.mapper.HysPayoutGapMapper;

/**
 * 运营看板：会员、订单、成交额、佣金与待办。
 */
@Service
public class HysDashboardService {

    private final HysMemberMapper memberMapper;

    private final HysOrderMapper orderMapper;

    private final HysCommissionMapper commissionMapper;

    private final HysFundApplyMapper fundApplyMapper;

    private final HysPayoutGapMapper gapMapper;

    public HysDashboardService(HysMemberMapper memberMapper, HysOrderMapper orderMapper,
            HysCommissionMapper commissionMapper, HysFundApplyMapper fundApplyMapper,
            HysPayoutGapMapper gapMapper) {
        this.memberMapper = memberMapper;
        this.orderMapper = orderMapper;
        this.commissionMapper = commissionMapper;
        this.fundApplyMapper = fundApplyMapper;
        this.gapMapper = gapMapper;
    }

    public HysVos.Dashboard overview() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> raw = orderMapper.selectDailyTrend(today.minusDays(6).atStartOfDay());
        Map<String, Map<String, Object>> byDate = new HashMap<>();
        for (Map<String, Object> row : raw) {
            byDate.put(String.valueOf(row.get("d")), row);
        }
        // 补齐 7 天，缺失日期显示 0，避免前端折线断档
        List<HysVos.TrendPoint> trend = new ArrayList<>(7);
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String key = String.format("%02d-%02d", day.getMonthValue(), day.getDayOfMonth());
            Map<String, Object> row = byDate.get(key);
            long orders = row == null ? 0L : ((Number) row.get("orders")).longValue();
            long amount = row == null ? 0L : ((Number) row.get("amount")).longValue();
            trend.add(new HysVos.TrendPoint(key, orders, amount));
        }
        return new HysVos.Dashboard(
                memberMapper.countAll(),
                memberMapper.countVip(java.time.LocalDateTime.now()),
                orderMapper.countAll(),
                orderMapper.countToday(today.atStartOfDay()),
                orderMapper.sumPaidAmount(),
                commissionMapper.sumTotal(),
                fundApplyMapper.countPending(),
                gapMapper.countUnhandled(),
                trend);
    }
}
