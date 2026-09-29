package com.h5.controller.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.dto.TelegramAuthDTO;
import com.h5.entity.BotTelegramUser;
import com.h5.entity.User;
import com.h5.mapper.BotTelegramUserMapper;
import com.h5.mapper.UserMapper;
import com.h5.service.BotAuthService;
import com.h5.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Telegram Mini App 登录接口
 * 前端 H5 在 Telegram 客户端内打开时，将 initData 发送到此接口
 * 后端验证 initData 签名（HMAC-SHA256，Telegram 官方算法），
 * 验证通过后自动注册/登录并返回 JWT token
 *
 * 文档: https://core.telegram.org/bots/webapps#validating-data-received-via-the-mini-app
 */
@RestController
@RequestMapping("/wap/auth")
public class TelegramAuthController {

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthController.class);

    @Autowired
    private BotAuthService botAuthService;

    @Autowired
    private BotTelegramUserMapper botTelegramUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${telegram.bot-token:}")
    private String botToken;

    /**
     * 已使用过的 initData hash（防重放）。
     * 单实例内存级实现；若多实例部署需改为 Redis SETNX。
     * 登录量低，24h 内重复 hash 直接拒绝。
     */
    private final Set<String> usedHashes = ConcurrentHashMap.newKeySet();
    private static final long AUTH_TTL_SECONDS = 86400; // 24h

    /**
     * Telegram Mini App 登录
     * POST /api/wap/auth/telegram
     *
     * @param params 包含 initData (Telegram WebAppInitData)
     */
    @PostMapping("/telegram")
    public Result<Map<String, Object>> telegramLogin(@RequestBody Map<String, Object> params,
                                                       HttpServletRequest request) {
        String initData = params.get("initData") != null ? params.get("initData").toString() : "";
        if (initData.isEmpty()) {
            throw new BusinessException("initData 不能为空");
        }

        // 1. 解析 initData（URL query string 格式）
        Map<String, String> data = parseInitData(initData);
        String hash = data.get("hash");
        if (hash == null || hash.isEmpty()) {
            throw new BusinessException("initData 缺少 hash");
        }

        // 2. 验证签名（如果配置了 bot token）
        if (botToken != null && !botToken.isEmpty()) {
            if (!verifyInitData(initData, hash, botToken)) {
                log.warn("Telegram initData 签名验证失败");
                throw new BusinessException("Telegram 登录验证失败");
            }
        } else {
            // 生产环境必须配置 bot token；未配置时直接拒绝，防止伪造
            log.error("未配置 telegram.bot-token，拒绝 initData 登录");
            throw new BusinessException("登录服务未配置");
        }

        // 3. auth_date 时效校验（Telegram 返回 unix 秒）
        String authDateStr = data.get("auth_date");
        if (authDateStr == null || authDateStr.isEmpty()) {
            throw new BusinessException("initData 缺少 auth_date");
        }
        long authDate;
        try {
            authDate = Long.parseLong(authDateStr);
        } catch (NumberFormatException e) {
            throw new BusinessException("auth_date 格式错误");
        }
        long nowSec = System.currentTimeMillis() / 1000;
        if (nowSec - authDate > AUTH_TTL_SECONDS) {
            throw new BusinessException("登录凭证已过期，请在 Telegram 内重新打开");
        }
        if (authDate - nowSec > 300) {
            // 客户端时钟偏差超过 5 分钟也拒绝，防重放窗口
            throw new BusinessException("客户端时间异常，请检查设备时间");
        }

        // 4. 防重放：同一 hash 只能用一次
        if (!usedHashes.add(hash)) {
            throw new BusinessException("登录凭证已被使用，请重新打开");
        }
        // 简单清理：set 过大时（>10000）不做主动过期，登录频率低，可接受

        // 3. 提取用户信息
        String userJson = data.get("user");
        if (userJson == null || userJson.isEmpty()) {
            throw new BusinessException("initData 缺少 user 信息");
        }
        TelegramUser tgUser = parseTelegramUser(userJson);
        if (tgUser.id == null) {
            throw new BusinessException("无法解析 Telegram 用户ID");
        }

        // 4. 检查是否已绑定
        BotTelegramUser binding = botTelegramUserMapper.selectOne(
                new LambdaQueryWrapper<BotTelegramUser>()
                        .eq(BotTelegramUser::getTelegramId, tgUser.id)
        );

        if (binding != null) {
            // 已绑定，直接返回登录态
            User user = userMapper.selectById(binding.getUserId());
            if (user == null) throw new BusinessException("绑定用户不存在");
            if (user.getStatus() == 0) throw new BusinessException("账号已被禁用");

            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            return Result.success(buildLoginResponse(user, token));
        }

        // 5. 未绑定，走 BotAuthService 自动注册逻辑
        TelegramAuthDTO dto = new TelegramAuthDTO();
        dto.setTelegramId(tgUser.id);
        dto.setUsername(tgUser.username);
        dto.setFirstName(tgUser.firstName);
        dto.setLastName(tgUser.lastName);
        dto.setLanguageCode(tgUser.languageCode);

        String ip = getClientIp(request);
        Map<String, Object> result = botAuthService.telegramAuth(dto, ip);
        return Result.success(result);
    }

    // ==================== 内部工具 ====================

    private Map<String, Object> buildLoginResponse(User user, String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("balance", user.getBalance());
        data.put("vipLevel", user.getVipLevel());
        data.put("isTrial", user.getIsTrial());
        return data;
    }

    /**
     * 解析 initData（URL query string 格式）
     */
    private Map<String, String> parseInitData(String initData) {
        Map<String, String> result = new HashMap<>();
        for (String pair : initData.split("&")) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                // URL decode
                try {
                    value = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
                } catch (Exception ignored) {}
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 验证 Telegram initData 签名
     * 算法: HMAC-SHA256(HMAC-SHA256(botToken, "WebAppData"), dataCheckString) == hash
     */
    private boolean verifyInitData(String initData, String hash, String botToken) {
        try {
            // 构建 dataCheckString（排除 hash，按 key 排序，用 \n 连接）
            Map<String, String> data = parseInitData(initData);
            data.remove("hash");

            StringBuilder dataCheckString = new StringBuilder();
            data.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        if (dataCheckString.length() > 0) dataCheckString.append("\n");
                        dataCheckString.append(e.getKey()).append("=").append(e.getValue());
                    });

            // Telegram 官方算法:
            //   secret_key = HMAC_SHA256(key="WebAppData", msg=bot_token)
            //   hash       = HMAC_SHA256(key=secret_key, msg=data_check_string)
            // 注意 hmacSha256(key, data) 的参数顺序，不能反
            SecretKeySpec keySpec = new SecretKeySpec(
                    hmacSha256("WebAppData".getBytes(StandardCharsets.UTF_8), botToken.getBytes(StandardCharsets.UTF_8)),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] result = mac.doFinal(dataCheckString.toString().getBytes(StandardCharsets.UTF_8));

            // 转为 hex
            StringBuilder hex = new StringBuilder();
            for (byte b : result) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString().equalsIgnoreCase(hash);
        } catch (Exception e) {
            log.error("Telegram 签名验证异常", e);
            return false;
        }
    }

    private byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    /**
     * 解析 Telegram user JSON
     */
    private TelegramUser parseTelegramUser(String userJson) {
        TelegramUser u = new TelegramUser();
        try {
            // 简单 JSON 解析
            u.id = extractLong(userJson, "id");
            u.username = extractString(userJson, "username");
            u.firstName = extractString(userJson, "first_name");
            u.lastName = extractString(userJson, "last_name");
            u.languageCode = extractString(userJson, "language_code");
        } catch (Exception e) {
            log.warn("解析 Telegram user 失败: {}", userJson);
        }
        return u;
    }

    private String extractString(String json, String field) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private Long extractLong(String json, String field) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)");
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? Long.parseLong(m.group(1)) : null;
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

    /** Telegram 用户信息 */
    private static class TelegramUser {
        Long id;
        String username;
        String firstName;
        String lastName;
        String languageCode;
    }
}
