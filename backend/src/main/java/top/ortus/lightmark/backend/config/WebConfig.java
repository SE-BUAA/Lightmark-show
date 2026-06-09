package top.ortus.lightmark.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.ortus.lightmark.backend.JwtTokenService;
import top.ortus.lightmark.backend.security.AdminAuthInterceptor;

/**
 * Web MVC 配置类，配置拦截器等 Web 相关功能
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtTokenService jwtTokenService;

    /**
     * 构造函数
     * @param jwtTokenService JWT 令牌服务
     */
    public WebConfig(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 添加拦截器配置
     * 为 /api/admin/** 路径添加管理员认证拦截器
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor(jwtTokenService))
                .addPathPatterns("/api/admin/**");
    }
}