package top.ortus.timemark.backend;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long expireMinutes;

    public JwtTokenService(
            @Value("${timemark.jwt.secret:timemark-secret-key-please-change}") String secret,
            @Value("${timemark.jwt.issuer:timemark}") String issuer,
            @Value("${timemark.jwt.expire-minutes:120}") long expireMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expireMinutes = expireMinutes;
    }

    public String createToken(Long userId, String nickname, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
                .claim("nickname", nickname)
                .claim("roles", roles)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long resolveUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.valueOf(claims.getSubject());
        } catch (Exception ex) {
            return null;
        }
    }

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
}
