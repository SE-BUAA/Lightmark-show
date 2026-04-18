package top.ortus.timemark.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import top.ortus.timemark.backend.config.JwtProperties;
import top.ortus.timemark.backend.exception.BusinessException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenService {
    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long userId, String nickname, List<String> roles) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("nickname", nickname)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public AuthUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            Long userId = Long.parseLong(claims.getSubject());
            String nickname = claims.get("nickname", String.class);
            List<String> roles = claims.get("roles", List.class);
            return new AuthUser(userId, nickname, roles);
        } catch (Exception ex) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
    }
}

