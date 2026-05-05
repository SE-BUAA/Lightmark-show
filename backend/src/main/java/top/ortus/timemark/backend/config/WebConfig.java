package top.ortus.timemark.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.security.AdminAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtTokenService jwtTokenService;

    public WebConfig(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor(jwtTokenService))
                .addPathPatterns("/api/admin/**");
    }
}

