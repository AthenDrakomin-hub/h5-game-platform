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
 * 安全配置启动校验。
 * 生产环境检测到弱默认值时输出 error 日志，提醒运维通过环境变量覆盖。
 */
@Component
public class SecurityConfigValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfigValidator.class);

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${admin.init.password:}")
    private String adminPassword;

    @Resource
    private Environment environment;

    private static final String DEV_JWT_PLACEHOLDER = "dev-only-insecure-secret-change-me-please-0123456789";

    @Override
    public void run(ApplicationArguments args) {
        boolean isProd = isProfileActive("prod");

        // JWT Secret 校验
        if (jwtSecret == null || jwtSecret.isEmpty() || DEV_JWT_PLACEHOLDER.equals(jwtSecret)) {
            if (isProd) {
                log.error("============================================================");
                log.error("  [安全警告] JWT_SECRET 未配置或仍为开发默认值！生产必须通过环境变量注入强随机串");
                log.error("============================================================");
            } else {
                log.warn("[安全提示] JWT_SECRET 使用开发默认值，生产必须设置");
            }
        }

        // 管理员密码：留空 = 首次启动随机生成（安全）；显式设了短密码才告警
        if (adminPassword != null && !adminPassword.isEmpty() && adminPassword.length() < 12) {
            if (isProd) {
                log.warn("[安全提示] ADMIN_PASSWORD 长度 <12，建议留空让系统随机生成 16 位密码");
            }
        }

        // 数据库密码校验
        String dbPassword = environment.getProperty("spring.datasource.password", "");
        if ((dbPassword.isEmpty() || "root".equals(dbPassword)) && isProd) {
            log.warn("[安全提示] 数据库密码为空或为 root，生产必须通过 DB_PASSWORD 注入强密码");
        }

        // Telegram bot token（C 端唯一登录方式，生产必须配）
        String botToken = environment.getProperty("telegram.bot-token", "");
        if (botToken.isEmpty() && isProd) {
            log.error("[安全警告] TELEGRAM_BOT_TOKEN 未配置，Telegram Mini App 登录将不可用");
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
