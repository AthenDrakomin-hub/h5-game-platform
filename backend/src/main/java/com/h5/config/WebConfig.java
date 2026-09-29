package com.h5.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：CORS + 鉴权拦截器 + 静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    /** 外部上传文件目录（生产环境建议挂载到持久化卷） */
    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * CORS 白名单，逗号分隔。
     * 生产只允许 Mini App 域名（https://h5.goodspage.cn）；
     * 本地开发通过 CORS_ALLOWED_ORIGINS=http://localhost:5173 注入。
     */
    @Value("${app.cors.allowed-origins:https://h5.goodspage.cn}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
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
                        // Bot 自动注册（Bot 内部调用，Controller 内有 X-Bot-Secret 校验）
                        "/bot/auth/register",
                        // 第三方支付异步回调（Controller 内有签名校验，无需 JWT）
                        "/payment/callback/**",
                        "/wap/payment-methods/recharge-methods",
                        "/wap/payment-methods/withdraw-methods",
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

    /**
     * 静态资源映射：/uploads/** 同时映射到 classpath 和外部目录
     * 优先级：外部目录 > classpath（外部上传的文件覆盖内置资源）
     * 注意：由于 context-path=/api，实际访问路径为 /api/uploads/**
     *       前端独立部署时由 Nginx 直接服务 /uploads，无需经过后端
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:" + uploadDir + "/",
                        "classpath:/static/uploads/",
                        "classpath:/uploads/"
                )
                .setCachePeriod(3600); // 缓存1小时
    }
}
