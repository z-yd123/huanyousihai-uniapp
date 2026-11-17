package com.ruoyi.hys.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.mapper.HysMemberMapper;

/**
 * 小程序接口鉴权拦截器。
 *
 * <p>
 * 为什么不复用若依的 Spring Security 链：后台是 sys_user + Redis 会话，小程序是会员 + 无状态 JWT，
 * 两套身份体系混在一条链上极易串号。这里让 /app/** 统一走本拦截器，
 * 后台 /hys/** 仍由若依原生链 + {@code @PreAuthorize} 保护，职责清晰、互不干扰。
 * </p>
 *
 * <p>
 * 校验顺序：令牌存在 → 签名/有效期 → 会员存在 → 状态正常 → 令牌版本一致（改密后旧令牌失效）。
 * 任何一步失败都返回 401，且不区分具体原因，避免被用来探测账号是否存在。
 * </p>
 */
@Component
public class HysAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HysAuthInterceptor.class);

    private static final String HEADER = "Authorization";

    private static final String BEARER = "Bearer ";

    private final HysJwtService jwtService;

    private final HysMemberMapper memberMapper;

    public HysAuthInterceptor(HysJwtService jwtService, HysMemberMapper memberMapper) {
        this.jwtService = jwtService;
        this.memberMapper = memberMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求直接放行（CORS 由若依的 CorsFilter 处理）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String token = resolveToken(request);
        HysJwtService.Payload payload = jwtService.parseToken(token);
        if (payload == null) {
            throw new ServiceException("登录已失效，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        HysMember member = memberMapper.selectMemberById(payload.memberId());
        if (member == null) {
            throw new ServiceException("账号不存在或已注销", HttpStatus.UNAUTHORIZED);
        }
        if (HysConstants.MEMBER_DISABLED.equals(member.getStatus())) {
            throw new ServiceException("账号已停用，请联系客服", HttpStatus.FORBIDDEN);
        }
        int currentVersion = member.getTokenVersion() == null ? 0 : member.getTokenVersion();
        if (currentVersion != payload.version()) {
            // 改密 / 后台重置密码 / 强制下线后，旧令牌立即作废
            throw new ServiceException("登录状态已变更，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        HysMemberContext.set(member);
        log.debug("会员 {} 访问 {}", member.getMemberId(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理，避免线程复用造成身份串号
        HysMemberContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(BEARER)) {
            return header.substring(BEARER.length()).trim();
        }
        // 兼容小程序里 <image src> 等无法设置请求头的场景：允许 query 传 token
        return request.getParameter("token");
    }
}
