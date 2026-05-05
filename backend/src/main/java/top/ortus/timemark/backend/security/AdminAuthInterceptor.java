package top.ortus.timemark.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.exception.ApiException;

import java.util.List;

public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;

    public AdminAuthInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(401, "unauthorized");
        }
        String token = header.substring("Bearer ".length());
        List<String> roles = jwtTokenService.resolveRoles(token);
        if (roles == null || roles.stream().noneMatch(role -> role.equalsIgnoreCase("ADMIN"))) {
            throw new ApiException(403, "forbidden");
        }
        return true;
    }
}

