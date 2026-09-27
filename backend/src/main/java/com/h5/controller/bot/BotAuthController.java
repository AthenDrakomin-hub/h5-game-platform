package com.h5.controller.bot;

import com.h5.common.Result;
import com.h5.dto.TelegramAuthDTO;
import com.h5.service.BotAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bot/auth")
public class BotAuthController {

    @Autowired
    private BotAuthService botAuthService;

    /**
     * Telegram 用户自动注册/登录
     * POST /api/bot/auth/register
     *
     * Bot 服务调用此接口，传入 Telegram 用户信息，
     * 后端自动创建平台账号（如未绑定）并返回登录态 token。
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> telegramRegister(
            @RequestBody TelegramAuthDTO dto,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        Map<String, Object> data = botAuthService.telegramAuth(dto, ip);
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
        return ip;
    }
}
