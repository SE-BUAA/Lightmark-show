package top.ortus.timemark.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.exception.ApiException;

/**
 * 管理员认证拦截器，用于保护需要管理员权限的接口
 */
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;

    /**
     * 构造函数
     * @param jwtTokenService JWT 令牌服务
     */
    public AdminAuthInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 请求处理前拦截，验证用户是否具有管理员权限
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器对象
     * @return true 表示通过验证，false 表示验证失败
     * @throws ApiException 如果未授权或权限不足
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(401, "unauthorized");
        }
        String token = header.substring("Bearer ".length());
        UserIdentity identity = jwtTokenService.resolveIdentity(token);
        if (identity != UserIdentity.ADMIN) {
            throw new ApiException(403, "forbidden");
        }
        return true;
    }
}