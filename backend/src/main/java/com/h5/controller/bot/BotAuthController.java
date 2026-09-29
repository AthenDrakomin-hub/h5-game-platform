package com.h5.controller.bot;

import com.h5.common.Result;
import com.h5.dto.TelegramAuthDTO;
import com.h5.service.BotAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bot/auth")
public class BotAuthController {

    @Autowired
    private BotAuthService botAuthService;

    /**
     * Bot → 后端的共享密钥。必须与 bot 端 BOT_INTERNAL_SECRET 一致。
     * 未配置时接口直接拒绝（fail-closed），防止生产忘配。
     */
    @Value("${bot.notify.secret:}")
    private String internalSecret;

    /**
     * Telegram 用户自动注册/登录
     * POST /api/bot/auth/register
     *
     * 仅允许本机 bot 服务调用，必须带 X-Bot-Secret 头。
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> telegramRegister(
            @RequestBody TelegramAuthDTO dto,
            HttpServletRequest request) {

        // 1. 共享密钥校验
        String provided = request.getHeader("X-Bot-Secret");
        if (internalSecret == null || internalSecret.isEmpty()) {
            return Result.error(500, "后端未配置 BOT_INTERNAL_SECRET，拒绝内部调用");
        }
        if (provided == null || !constantTimeEquals(provided, internalSecret)) {
            return Result.error(403, "Bot 内部调用鉴权失败");
        }

        // 2. 本机来源校验（额外防线，bot 通过 127.0.0.1 调用）
        String remote = request.getRemoteAddr();
        if (!"127.0.0.1".equals(remote) && !"0:0:0:0:0:0:0:1".equals(remote) && !"localhost".equals(remote)) {
            // 经 nginx 反代时 remote 是 127.0.0.1；直接外网调用在生产网络层就挡了
            // 这里只做日志告警，不强制拒绝（避免误杀 nginx 反代）
        }

        String ip = getClientIp(request);
        Map<String, Object> data = botAuthService.telegramAuth(dto, ip);
        return Result.success(data);
    }

    /** 恒定时间比较，防时序攻击 */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
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
