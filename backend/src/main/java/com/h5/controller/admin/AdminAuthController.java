package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import com.h5.service.AdminLogService;
import com.h5.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端认证 Controller
 * 独立于用户端 /wap/auth，管理员专用登录入口
 */
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminLogService adminLogService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 管理员登录
     * POST /api/admin/auth/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params,
                                               HttpServletRequest request) {
        String username = params.get("username") != null ? params.get("username").toString() : "";
        String password = params.get("password") != null ? params.get("password").toString() : "";

        if (username.isEmpty() || password.isEmpty()) {
            throw new BusinessException("用户名和密码不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        String role = user.getRole();
        if (!"admin".equals(role) && !"superadmin".equals(role)) {
            throw new BusinessException("没有管理权限");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp(request));
        userMapper.updateById(user);

        // 记录登录日志
        adminLogService.log(user.getId(), username, "login", "admin", null,
                null, null, request);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 返回兼容 vue-pure-admin 前端的格式
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", token);
        data.put("refreshToken", token);
        data.put("expires", LocalDateTime.now().plusDays(1).toString());
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("roles", java.util.List.of(user.getRole() != null ? user.getRole() : "admin"));
        data.put("permissions", java.util.List.of("*"));
        data.put("role", user.getRole());
        data.put("avatar", user.getAvatar());
        return Result.success(data);
    }

    /**
     * 获取当前管理员信息
     * GET /api/admin/auth/info
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        data.put("avatar", user.getAvatar());
        data.put("balance", user.getBalance());
        return Result.success(data);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "";
    }
}
