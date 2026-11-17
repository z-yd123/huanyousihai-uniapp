package com.ruoyi.hys.config;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysMember;

/**
 * 小程序会员上下文（ThreadLocal）。
 *
 * <p>
 * 数据边界的核心：所有 /app 接口的会员身份<b>只从令牌解析结果取</b>，绝不接受前端传入的 memberId。
 * 控制器通过 {@link #requireMemberId()} 取当前会员，服务层再显式以 memberId 作为查询条件，
 * 从根上避免“改个参数就能看别人订单/余额”的越权问题。
 * </p>
 * <p>
 * 拦截器在 {@code afterCompletion} 中必须清理，否则线程池复用会导致串号。
 * </p>
 */
public final class HysMemberContext {

    private static final ThreadLocal<HysMember> HOLDER = new ThreadLocal<>();

    private HysMemberContext() {
    }

    public static void set(HysMember member) {
        HOLDER.set(member);
    }

    /** 当前会员，未登录返回 null（仅拦截器与内部逻辑使用） */
    public static HysMember get() {
        return HOLDER.get();
    }

    /**
     * 当前会员，未登录直接抛 401。
     */
    public static HysMember require() {
        HysMember member = HOLDER.get();
        if (member == null) {
            throw new ServiceException("登录已失效，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        return member;
    }

    /**
     * 当前会员ID，未登录直接抛 401。
     */
    public static Long requireMemberId() {
        return require().getMemberId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
