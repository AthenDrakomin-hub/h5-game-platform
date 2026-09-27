package com.h5.config;

import com.h5.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局限流拦截器（Redis 分布式 + 内存降级）
 *
 * 限流规则：
 * - 登录接口：5次/分钟/IP
 * - 投注接口：10次/10秒/用户
 * - 全局：100次/分钟/IP
 *
 * Redis 实现：INCR + EXPIRE 固定窗口算法
 * 内存降级：ConcurrentHashMap + 定时清理
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 内存降级存储：key -> [count, windowStart]
    private final ConcurrentHashMap<String, int[]> memoryStore = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.currentTimeMillis();

    // 限流配置
    private static final int LOGIN_LIMIT = 5;
    private static final int LOGIN_WINDOW = 60; // 秒
    private static final int BET_LIMIT = 10;
    private static final int BET_WINDOW = 10; // 秒
    private static final int GLOBAL_LIMIT = 100;
    private static final int GLOBAL_WINDOW = 60; // 秒

    private static final String REDIS_PREFIX = "ratelimit:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIp(request);
        String path = request.getRequestURI();
        String userId = request.getHeader("X-User-Id");

        // 1. 登录接口限流（按IP）
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            if (!checkLimit("login:" + ip, LOGIN_LIMIT, LOGIN_WINDOW)) {
                writeLimitResponse(response, "登录请求过于频繁，请稍后再试");
                return false;
            }
        }

        // 2. 投注接口限流（按用户）
        if (path.contains("/bet/place") && userId != null && !userId.isEmpty()) {
            if (!checkLimit("bet:" + userId, BET_LIMIT, BET_WINDOW)) {
                writeLimitResponse(response, "投注过于频繁，请稍后再试");
                return false;
            }
        }

        // 3. 全局限流（按IP）
        if (!checkLimit("global:" + ip, GLOBAL_LIMIT, GLOBAL_WINDOW)) {
            writeLimitResponse(response, "请求过于频繁，请稍后再试");
            return false;
        }

        return true;
    }

    /**
     * 限流检查（优先 Redis，失败降级内存）
     */
    private boolean checkLimit(String key, int limit, int windowSeconds) {
        // 优先使用 Redis
        if (redisTemplate != null) {
            try {
                return checkRedisLimit(key, limit, windowSeconds);
            } catch (Exception e) {
                log.warn("Redis限流失败，降级内存: {}", e.getMessage());
            }
        }
        // 内存降级
        return checkMemoryLimit(key, limit, windowSeconds);
    }

    /**
     * Redis 固定窗口限流
     */
    private boolean checkRedisLimit(String key, int limit, int windowSeconds) {
        String redisKey = REDIS_PREFIX + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            // 第一次访问，设置过期时间
            redisTemplate.expire(redisKey, windowSeconds, java.util.concurrent.TimeUnit.SECONDS);
        }
        return count != null && count <= limit;
    }

    /**
     * 内存固定窗口限流（降级方案）
     */
    private boolean checkMemoryLimit(String key, int limit, int windowSeconds) {
        cleanupMemoryStore();

        long now = System.currentTimeMillis();
        int[] entry = memoryStore.computeIfAbsent(key, k -> new int[]{0, (int) (now / 1000)});

        synchronized (entry) {
            int windowStart = entry[1];
            if (now / 1000 - windowStart >= windowSeconds) {
                // 窗口过期，重置
                entry[0] = 1;
                entry[1] = (int) (now / 1000);
                return true;
            }
            if (entry[0] >= limit) {
                return false;
            }
            entry[0]++;
            return true;
        }
    }

    /**
     * 清理过期的内存限流记录（每60秒执行一次）
     */
    private void cleanupMemoryStore() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < 60000) return;
        lastCleanup = now;

        memoryStore.entrySet().removeIf(entry -> {
            int[] val = entry.getValue();
            return now / 1000 - val[1] > 300; // 超过5分钟的记录删除
        });
    }

    private void writeLimitResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(429, message)
            ));
        } catch (Exception e) {
            log.error("写入限流响应失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
