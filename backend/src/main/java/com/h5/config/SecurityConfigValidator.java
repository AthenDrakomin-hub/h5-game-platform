package com.h5.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 安全配置启动校验
 * 在应用启动时检查关键安全配置，生产环境使用默认值时输出严重警告
 */
@Component
public class SecurityConfigValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfigValidator.class);

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${admin.init.password:yefeng}")
    private String adminPassword;

    @Resource
    private Environment environment;

    private static final String DEFAULT_JWT_SECRET = "h5-game-secret-key-2024-please-change-in-production-0123456789";
    private static final String DEFAULT_ADMIN_PASSWORD = "yefeng";

    @Override
    public void run(ApplicationArguments args) {
        boolean isProd = isProfileActive("prod");

        // JWT Secret 校验
        if (DEFAULT_JWT_SECRET.equals(jwtSecret) || jwtSecret == null || jwtSecret.isEmpty()) {
            if (isProd) {
                log.error("============================================================");
                log.error("  [安全警告] JWT Secret 使用默认值！生产环境必须通过 JWT_SECRET 环境变量覆盖");
                log.error("  当前值: {}...", jwtSecret != null ? jwtSecret.substring(0, Math.min(12, jwtSecret.length())) : "null");
                log.error("============================================================");
            } else {
                log.warn("[安全提示] JWT Secret 使用默认值，生产环境请设置 JWT_SECRET 环境变量");
            }
        } else {
            log.info("[安全] JWT Secret 已自定义配置");
        }

        // 管理员默认密码校验
        if (DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
            if (isProd) {
                log.warn("[安全提示] 管理员密码使用默认值(yefeng)，建议首次登录后立即修改");
            }
        }

        // 数据库密码校验（非root）
        String dbPassword = environment.getProperty("spring.datasource.password", "");
        if ("root".equals(dbPassword) && isProd) {
            log.warn("[安全提示] 数据库密码为root，生产环境建议使用非root账号");
        }

        log.info("安全配置校验完成 (prod={})", isProd);
    }

    private boolean isProfileActive(String profile) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles == null || profiles.length == 0) return false;
        for (String p : profiles) {
            if (profile.equalsIgnoreCase(p)) return true;
        }
        return false;
    }
}
