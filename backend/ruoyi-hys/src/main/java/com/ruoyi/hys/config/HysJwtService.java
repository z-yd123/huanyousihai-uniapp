package com.ruoyi.hys.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 小程序会员令牌服务（与若依后台 sys_user 的令牌体系完全隔离）。
 *
 * <p>
 * 设计要点：
 * </p>
 * <ul>
 * <li>无状态 JWT，不依赖 Redis，方便单机演示与后续水平扩容；</li>
 * <li>令牌内携带 {@code ver}（会员表 token_version），改密 / 停用 / 补绑等敏感操作递增版本，
 * 旧令牌立即失效，解决“无状态令牌无法主动下线”的常见缺陷；</li>
 * <li>与若依后台共用 {@code token.secret}，但不复用其 claims 结构，避免两套身份体系互相串用。</li>
 * </ul>
 */
@Component
public class HysJwtService {

    private static final Logger log = LoggerFactory.getLogger(HysJwtService.class);

    private static final String CLAIM_VERSION = "ver";

    private final SecretKey secretKey;

    /** 会员令牌有效期（分钟），默认 7 天 */
    private final long expireMillis;

    public HysJwtService(@Value("${token.secret}") String secret,
            @Value("${hys.token-expire-minutes:10080}") long expireMinutes) {
        // HS512 要求密钥不少于 64 字节，若依默认密钥已满足；此处再兜底一次，避免配置被改短后启动即崩
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 64) {
            throw new IllegalStateException("token.secret 长度不足 64 字节，无法用于 HS512 会员令牌签名");
        }
        this.secretKey = Keys.hmacShaKeyFor(bytes);
        this.expireMillis = Math.max(1, expireMinutes) * 60_000L;
    }

    /**
     * 令牌载荷。
     *
     * @param memberId 会员ID
     * @param version  令牌版本
     */
    public record Payload(long memberId, int version) {
    }

    /**
     * 签发会员令牌。
     *
     * @param memberId 会员ID
     * @param version  当前令牌版本
     * @return JWT 字符串
     */
    public String createToken(long memberId, int version) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_VERSION, version)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 解析令牌。
     *
     * @param token JWT
     * @return 载荷；令牌非法 / 过期 / 结构不符时返回 null（由调用方统一转 401，不泄露具体原因）
     */
    public Payload parseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();
            long memberId = Long.parseLong(claims.getSubject());
            Integer version = claims.get(CLAIM_VERSION, Integer.class);
            return new Payload(memberId, version == null ? 0 : version);
        } catch (JwtException | IllegalArgumentException e) {
            // 令牌被篡改 / 过期 / 用后台令牌访问小程序接口，都走同一出口，避免探测
            log.debug("会员令牌解析失败: {}", e.getMessage());
            return null;
        }
    }
}
