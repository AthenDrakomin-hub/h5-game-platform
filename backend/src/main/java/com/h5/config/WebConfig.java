package com.h5.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：CORS + 鉴权拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-User-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 公开接口（不需要登录）
                        "/wap/auth/login",
                        "/wap/auth/register",
                        "/wap/auth/trial-login",
                        "/wap/home/config",
                        "/wap/home/banners",
                        "/wap/home/announcements",
                        "/wap/home/categories",
                        "/wap/home/games",
                        "/wap/draw/info/batch",
                        "/wap/game/casino-providers",
                        "/wap/game/casino-games",
                        "/wap/promotion/list",
                        "/wap/promotion/categories",
                        "/wap/promotion/detail/**",
                        // 静态资源
                        "/uploads/**",
                        "/error"
                );
    }
}
