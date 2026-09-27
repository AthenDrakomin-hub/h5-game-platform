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

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

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
        // 1. 全局鉴权拦截器（JWT 校验）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 用户端公开接口
                        "/wap/auth/login",
                        "/wap/auth/register",
                        "/wap/auth/trial-login",
                        "/wap/auth/telegram",
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
                        // 管理端登录（登录时无 token）
                        "/admin/auth/login",
                        // Bot 自动注册（Bot 内部调用）
                        "/bot/auth/register",
                        // 静态资源
                        "/uploads/**",
                        "/error"
                );

        // 2. 管理端角色拦截器（校验 admin/superadmin 角色）
        //    在 AuthInterceptor 之后执行，已通过 JWT 校验
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/auth/login"
                );

        // 3. 限流拦截器（最先执行，在鉴权之前）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/uploads/**")
                .order(0); // 最高优先级
    }
}
