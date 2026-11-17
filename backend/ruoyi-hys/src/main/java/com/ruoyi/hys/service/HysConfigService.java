package com.ruoyi.hys.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.domain.HysConfig;
import com.ruoyi.hys.engine.CommissionEngine;
import com.ruoyi.hys.mapper.HysConfigMapper;

/**
 * 业务参数服务。
 *
 * <p>
 * AGENT.md 要求「待确认项用可配置参数实现，不硬编码」。这里把它们集中到 hys_config，
 * 并在 {@link #update} 做红线校验：<b>配置可以调，但调不出合规范围</b>——
 * 例如拨比总额不允许改成 190 元，充值上限不允许调到 5000 元以上，手续费率不允许超过 6%。
 * </p>
 */
@Service
public class HysConfigService {

    private static final Logger log = LoggerFactory.getLogger(HysConfigService.class);

    /** 配置缓存有效期（毫秒），避免每笔订单重复查库；后台改配置后立即失效 */
    private static final long CACHE_TTL = 10_000L;

    private final HysConfigMapper configMapper;

    private volatile Map<String, String> cache = Map.of();

    private volatile long cacheAt = 0L;

    public HysConfigService(HysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    public List<HysConfig> selectConfigList(HysConfig query) {
        return configMapper.selectConfigList(query);
    }

    /**
     * 修改参数（仅管理员，带红线校验）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(String configKey, String configValue) {
        validate(configKey, configValue);
        int rows = configMapper.updateConfigValue(configKey, configValue);
        if (rows == 0) {
            throw new ServiceException("参数不存在：" + configKey);
        }
        invalidate();
        log.info("业务参数变更 {} -> {}", configKey, configValue);
    }

    /**
     * 参数红线校验。
     */
    public void validate(String key, String value) {
        if (key == null || value == null) {
            throw new ServiceException("参数键与值不能为空");
        }
        switch (key) {
            case HysConstants.CFG_PAYOUT_TOTAL -> requireLong(key, value, CommissionEngine.PAYOUT_TOTAL);
            case HysConstants.CFG_PAYOUT_DIRECT_AFTER -> requireLong(key, value, CommissionEngine.PAYOUT_TOTAL);
            case "payout.max_depth" -> throw new ServiceException("分销层级为合规红线（仅两级），不允许通过配置修改");
            case HysConstants.CFG_WITHDRAW_FEE_RATE -> {
                BigDecimal rate = decimal(key, value, new BigDecimal("0.06"));
                if (rate.compareTo(new BigDecimal("0.06")) > 0 || rate.signum() < 0) {
                    throw new ServiceException("提现手续费率不得超过 6%（已确认口径）");
                }
            }
            case HysConstants.CFG_RECHARGE_LIMIT -> {
                long limit = longValue(key, value, HysConstants.DEFAULT_RECHARGE_LIMIT);
                if (limit <= 0 || limit > 500_000L) {
                    throw new ServiceException("单笔充值上限不得超过 5000 元（合规口径）");
                }
            }
            case HysConstants.CFG_PAYOUT_DIRECT_FIRST_TWO, HysConstants.CFG_PAYOUT_INDIRECT_FIRST_TWO -> {
                long v = longValue(key, value, 10_000L);
                long other = key.equals(HysConstants.CFG_PAYOUT_DIRECT_FIRST_TWO)
                        ? getLong(HysConstants.CFG_PAYOUT_INDIRECT_FIRST_TWO, 10_000L)
                        : getLong(HysConstants.CFG_PAYOUT_DIRECT_FIRST_TWO, 10_000L);
                if (v < 0 || v + other != CommissionEngine.PAYOUT_TOTAL) {
                    throw new ServiceException("前两单的直推奖 + 间推奖必须等于 200 元");
                }
            }
            case HysConstants.CFG_RENEW_COMMISSION_RATE -> {
                BigDecimal rate = decimal(key, value, BigDecimal.ZERO);
                if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                    throw new ServiceException("续费计佣比例需在 0 ~ 1 之间");
                }
            }
            case HysConstants.CFG_VIP_PRICE, HysConstants.CFG_VIP_RENEW_PRICE, HysConstants.CFG_TRANSFER_DAILY_LIMIT,
                 HysConstants.CFG_TRANSFER_DAILY_COUNT, HysConstants.CFG_VIP_RENEW_YEARS ->
                    longValue(key, value, 0L);
            case HysConstants.CFG_VIP_EXPIRE_AFFECTS_COMMISSION -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new ServiceException("请填写 true 或 false");
                }
            }
            default -> {
                // 文案类参数（如合规提示）不做数值校验
                if (value.length() > 200) {
                    throw new ServiceException("参数值长度不能超过 200 字");
                }
            }
        }
    }

    private void requireLong(String key, String value, long expected) {
        long v = longValue(key, value, expected);
        if (v != expected) {
            throw new ServiceException("该参数为合规红线，必须等于 " + expected);
        }
    }

    private long longValue(String key, String value, long defaultValue) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("参数 " + key + " 必须是整数");
        }
    }

    private BigDecimal decimal(String key, String value, BigDecimal defaultValue) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("参数 " + key + " 必须是数字");
        }
    }

    // ------------------------------------------------------------------
    // 读取
    // ------------------------------------------------------------------

    public String getString(String key, String defaultValue) {
        String v = load().get(key);
        return v == null || v.isBlank() ? defaultValue : v;
    }

    public long getLong(String key, long defaultValue) {
        String v = load().get(key);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            log.warn("参数 {} 的值 {} 不是整数，回退默认值 {}", key, v, defaultValue);
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {
        return (int) getLong(key, defaultValue);
    }

    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        String v = load().get(key);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            log.warn("参数 {} 的值 {} 不是数字，回退默认值 {}", key, v, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String v = load().get(key);
        return v == null || v.isBlank() ? defaultValue : Boolean.parseBoolean(v.trim());
    }

    /**
     * 当前拨比规则（受引擎校验，配置被改坏时启动即报错，不会悄悄少发钱）。
     */
    public CommissionEngine.PayoutRule payoutRule() {
        return new CommissionEngine.PayoutRule(
                getLong(HysConstants.CFG_PAYOUT_TOTAL, CommissionEngine.PAYOUT_TOTAL),
                getLong(HysConstants.CFG_PAYOUT_DIRECT_FIRST_TWO, 10_000L),
                getLong(HysConstants.CFG_PAYOUT_INDIRECT_FIRST_TWO, 10_000L),
                getLong(HysConstants.CFG_PAYOUT_DIRECT_AFTER, CommissionEngine.PAYOUT_TOTAL));
    }

    public BigDecimal withdrawFeeRate() {
        return getDecimal(HysConstants.CFG_WITHDRAW_FEE_RATE, new BigDecimal("0.06"));
    }

    public long rechargeLimit() {
        return getLong(HysConstants.CFG_RECHARGE_LIMIT, HysConstants.DEFAULT_RECHARGE_LIMIT);
    }

    public String appNotice() {
        return getString(HysConstants.CFG_APP_NOTICE, "分销仅两级，合法合规；提现收取 6% 手续费");
    }

    private Map<String, String> load() {
        long now = System.currentTimeMillis();
        if (now - cacheAt < CACHE_TTL && !cache.isEmpty()) {
            return cache;
        }
        List<HysConfig> list = configMapper.selectConfigList(new HysConfig());
        Map<String, String> map = new HashMap<>(list.size() * 2);
        for (HysConfig item : list) {
            map.put(item.getConfigKey(), item.getConfigValue());
        }
        cache = Map.copyOf(map);
        cacheAt = now;
        return cache;
    }

    public void invalidate() {
        cache = Map.of();
        cacheAt = 0L;
    }
}
