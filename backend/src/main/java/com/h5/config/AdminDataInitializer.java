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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 系统初始化器
 * 应用启动时自动创建超级管理员账号（如不存在）
 *
 * 默认账号：admin / yefeng
 * 可通过环境变量覆盖：
 *   admin.init.username=admin
 *   admin.init.password=yefeng
 */
@Component
public class AdminDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    @Autowired
    private UserMapper userMapper;

    @Value("${admin.init.username:admin}")
    private String adminUsername;

    @Value("${admin.init.password:yefeng}")
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
            // 已存在，确保角色为 superadmin
            if (!"superadmin".equals(existing.getRole())) {
                existing.setRole("superadmin");
                userMapper.updateById(existing);
                log.info("已将用户 {} 升级为 superadmin", adminUsername);
            } else {
                log.info("超级管理员账号 {} 已存在，跳过初始化", adminUsername);
            }
            return;
        }

        // 创建超级管理员
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
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
        log.info("  密码:   {}", adminPassword);
        log.info("  角色:   superadmin");
        log.info("  ⚠️  请尽快登录并修改默认密码");
        log.info("========================================");
    }
}
