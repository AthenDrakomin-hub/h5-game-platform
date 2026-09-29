package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.BotTelegramUser;
import com.h5.mapper.BotTelegramUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 后端 → Telegram Bot 通知服务。
 * 资金事件（充值到账、提现审核、派彩等）通过本服务推送给用户 Telegram。
 *
 * 失败策略：所有调用 try/catch 静默，不影响主业务事务；
 * 未配置 BOT_INTERNAL_SECRET 或 bot 未启动时直接跳过。
 */
@Service
public class BotNotifyService {

    private static final Logger log = LoggerFactory.getLogger(BotNotifyService.class);

    @Autowired
    private BotTelegramUserMapper botTelegramUserMapper;

    @Value("${bot.notify.url:http://127.0.0.1:3001/api/bot/notify/send}")
    private String notifyUrl;

    @Value("${bot.notify.secret:}")
    private String notifySecret;

    private final RestTemplate restTemplate = new RestTemplate();

    static {
        // 短超时，避免拖慢主请求
    }

    /**
     * 按用户 ID 发送通知。内部自动查 telegram_id；未绑定则跳过。
     *
     * @param userId 平台用户 ID
     * @param type   通知类型（recharge/withdraw_success/win/system 等，与 bot 模板对应）
     * @param data   模板数据
     */
    public void sendToUser(Long userId, String type, Map<String, Object> data) {
        if (notifySecret == null || notifySecret.isEmpty()) {
            return; // 未配置，跳过
        }
        try {
            BotTelegramUser binding = botTelegramUserMapper.selectOne(
                    new LambdaQueryWrapper<BotTelegramUser>()
                            .eq(BotTelegramUser::getUserId, userId)
            );
            if (binding == null || binding.getTelegramId() == null) {
                return; // 用户未绑定 Telegram
            }

            Map<String, Object> body = new HashMap<>();
            body.put("telegramId", binding.getTelegramId());
            body.put("type", type);
            body.put("data", data != null ? data : new HashMap<>());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Bot-Secret", notifySecret);

            restTemplate.postForEntity(notifyUrl, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.warn("[BotNotify] 发送失败 userId={} type={}: {}", userId, type, e.getMessage());
        }
    }

    /** 便捷方法：构造 data map */
    public static Map<String, Object> data(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }
}
