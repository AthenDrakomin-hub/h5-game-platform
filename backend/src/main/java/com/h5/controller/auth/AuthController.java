package com.h5.controller.auth;

import com.h5.common.Result;
import com.h5.dto.LoginDTO;
import com.h5.dto.RegisterDTO;
import com.h5.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wap/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     * POST /api/wap/auth/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        Map<String, Object> data = authService.login(dto, ip);
        return Result.success(data);
    }

    /**
     * 注册
     * POST /api/wap/auth/register
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        Map<String, Object> data = authService.register(dto, ip);
        return Result.success(data);
    }

    /**
     * 试玩登录
     * POST /api/wap/auth/trial-login
     */
    @PostMapping("/trial-login")
    public Result<Map<String, Object>> trialLogin(HttpServletRequest request) {
        String ip = getClientIp(request);
        Map<String, Object> data = authService.trialLogin(ip);
        return Result.success(data);
    }

    /**
     * 登出
     * POST /api/wap/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，前端清除 token 即可
        return Result.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
