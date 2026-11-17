package com.ruoyi.hys.util;

/**
 * 脱敏工具。AGENT.md 明确要求：代码、日志、接口、测试数据中不得出现真实手机号与完整银行卡号。
 */
public final class HysMasks {

    private HysMasks() {
    }

    /**
     * 手机号脱敏：13800000004 → 138****0004。
     */
    public static String phone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 银行卡 / 账号脱敏：6217000011118899 → 6217****8899。
     */
    public static String account(String account) {
        if (account == null || account.length() < 8) {
            return account;
        }
        return account.substring(0, 4) + "****" + account.substring(account.length() - 4);
    }

    /**
     * 昵称脱敏（仅用于日志排查，接口不做处理）。
     */
    public static String name(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.charAt(0) + "**";
    }
}
