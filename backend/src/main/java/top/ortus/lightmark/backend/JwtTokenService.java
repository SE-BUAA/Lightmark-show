package top.ortus.lightmark.backend;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.ortus.lightmark.backend.security.UserIdentity;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * JWT 令牌服务类，负责创建和解析 JWT 令牌
 */
@Service
public class JwtTokenService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long expireMinutes;

    /**
     * 构造函数，初始化 JWT 配置
     * @param secret JWT 密钥
     * @param issuer JWT 发行者
     * @param expireMinutes 令牌过期时间（分钟）
     */
    public JwtTokenService(
            @Value("${lightmark.jwt.secret:lightmark-secret-key-please-change}") String secret,
            @Value("${lightmark.jwt.issuer:lightmark}") String issuer,
            @Value("${lightmark.jwt.expire-minutes:120}") long expireMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expireMinutes = expireMinutes;
    }

    /**
     * 创建 JWT 令牌
     * @param userId 用户 ID
     * @param nickname 用户昵称
     * @param roles 用户角色列表
     * @return JWT 令牌字符串
     */
    public String createToken(Long userId, String nickname, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
                .claim("nickname", nickname)
                .claim("roles", roles)
                .claim("identity", resolveIdentityFromRoles(roles).name())
                .signWith(secretKey)
                .compact();
    }

    /**
     * 创建基于单一身份的 JWT。
     */
    public String createToken(Long userId, String nickname, UserIdentity identity) {
        UserIdentity safeIdentity = identity == null ? UserIdentity.USER : identity;
        return createToken(userId, nickname, List.of(safeIdentity.name()));
    }

    /**
     * 解析 JWT 令牌
     * @param token JWT 令牌字符串
     * @return 令牌声明对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中解析用户 ID
     * @param token JWT 令牌字符串
     * @return 用户 ID，解析失败返回 null
     */
    public Long resolveUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.valueOf(claims.getSubject());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 从令牌中解析用户角色
     * @param token JWT 令牌字符串
     * @return 用户角色列表，解析失败返回空列表
     */
    public List<String> resolveRoles(String token) {
        try {
            Claims claims = parseToken(token);
            Object roles = claims.get("roles");
            if (roles instanceof List<?> list) {
                return list.stream().map(String::valueOf).toList();
            }
            return List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }

    /**
     * 从令牌中解析单一身份，解析失败默认 USER。
     */
    public UserIdentity resolveIdentity(String token) {
        try {
            Claims claims = parseToken(token);
            Object identity = claims.get("identity");
            if (identity != null) {
                return UserIdentity.fromRoleName(String.valueOf(identity));
            }
            return resolveIdentityFromRoles(resolveRoles(token));
        } catch (Exception ex) {
            return UserIdentity.USER;
        }
    }

    private UserIdentity resolveIdentityFromRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return UserIdentity.USER;
        }
        for (String role : roles) {
            if (UserIdentity.fromRoleName(role) == UserIdentity.ADMIN) {
                return UserIdentity.ADMIN;
            }
        }
        return UserIdentity.fromRoleName(roles.get(0));
    }
}