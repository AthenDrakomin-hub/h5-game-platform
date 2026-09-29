package com.h5.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 系统初始化器
 * 应用启动时自动创建超级管理员账号（如不存在）。
 *
 * 密码策略：
 * - 通过 admin.init.password 显式配置时使用该密码；
 * - 未配置（默认空）时随机生成 16 位强密码，只在首次创建时打印一次到日志；
 * - 账号已存在时不再打印密码，避免反复泄露。
 */
@Component
public class AdminDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);
    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private UserMapper userMapper;

    @Value("${admin.init.username:admin}")
    private String adminUsername;

    @Value("${admin.init.password:}")
    private String adminPassword;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        try {
            initSuperAdmin();
        } catch (Exception e) {
            log.error("管理员账号初始化失败", e);
        }
    }

    private void initSuperAdmin() {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, adminUsername)
        );

        if (existing != null) {
            if (!"superadmin".equals(existing.getRole())) {
                existing.setRole("superadmin");
                userMapper.updateById(existing);
                log.info("已将用户 {} 升级为 superadmin", adminUsername);
            } else {
                log.info("超级管理员账号 {} 已存在，跳过初始化", adminUsername);
            }
            return;
        }

        // 未显式配置密码时，随机生成强密码
        String plainPassword = (adminPassword == null || adminPassword.isEmpty())
                ? generateRandomPassword(16)
                : adminPassword;

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(plainPassword));
        admin.setNickname("超级管理员");
        admin.setBalance(BigDecimal.ZERO);
        admin.setFrozenBalance(BigDecimal.ZERO);
        admin.setVipLevel(1);
        admin.setIsTrial(0);
        admin.setStatus(1);
        admin.setRole("superadmin");
        admin.setInviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp("127.0.0.1");
        userMapper.insert(admin);

        log.info("========================================");
        log.info("  超级管理员账号已创建");
        log.info("  用户名: {}", adminUsername);
        if (adminPassword == null || adminPassword.isEmpty()) {
            log.info("  密码:   {} （自动生成，仅此一次打印，请立即保存并登录后修改）", plainPassword);
        } else {
            log.info("  密码:   （来自环境变量 ADMIN_PASSWORD）");
        }
        log.info("  角色:   superadmin");
        log.info("========================================");
    }

    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
