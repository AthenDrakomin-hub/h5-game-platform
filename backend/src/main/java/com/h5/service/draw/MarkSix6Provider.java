package com.h5.service.draw;

import com.h5.entity.DrawResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MarkSix6 第三方开奖 API 提供者
 * API 文档: https://api1.marksix6.com/
 * 接口: https://api3.marksix6.net/lottery_api.php?type={type}
 *
 * 支持彩种映射（可在配置中覆盖）：
 *   jspk10  → pk10
 *   jsssc   → ssc
 *   jsdd    → pc28 (幸运28)
 *   happy8lhc → lhc (六合彩)
 *   jsft    → feit (飞艇)
 *
 * 启用条件: draw.provider.marksix6.enabled=true
 */
@Component
@ConditionalOnProperty(name = "draw.provider.marksix6.enabled", havingValue = "true")
public class MarkSix6Provider implements DrawDataProvider {

    private static final Logger log = LoggerFactory.getLogger(MarkSix6Provider.class);
    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @Value("${draw.provider.marksix6.api-url:https://api3.marksix6.net/lottery_api.php}")
    private String apiUrl;

    @Value("${draw.provider.marksix6.timeout:5000}")
    private int timeout;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getName() {
        return "marksix6";
    }

    @Override
    public boolean isEnabled() {
        return true; // @ConditionalOnProperty 已保证
    }

    @Override
    public DrawResult getLatestDraw(String lotteryCode) {
        String type = mapLotteryType(lotteryCode);
        if (type == null) {
            log.warn("MarkSix6 不支持彩种: {}", lotteryCode);
            return null;
        }

        try {
            String url = apiUrl + "?type=" + type;
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isEmpty()) {
                log.warn("MarkSix6 返回空响应, type={}", type);
                return null;
            }
            return parseResponse(lotteryCode, response);
        } catch (Exception e) {
            log.error("MarkSix6 获取开奖失败, lotteryCode={}, error={}", lotteryCode, e.getMessage());
            return null;
        }
    }

    @Override
    public List<DrawResult> getLatestDraws(List<String> lotteryCodes) {
        List<DrawResult> results = new ArrayList<>();
        for (String code : lotteryCodes) {
            DrawResult r = getLatestDraw(code);
            if (r != null) {
                results.add(r);
            }
        }
        return results;
    }

    @Override
    public List<DrawResult> getHistoryDraws(String lotteryCode, int limit) {
        // MarkSix6 免费接口只返回最新一期，历史记录需付费 API
        // 此处返回单条，历史记录从本地 draw_results 表读取
        DrawResult latest = getLatestDraw(lotteryCode);
        List<DrawResult> list = new ArrayList<>();
        if (latest != null) {
            list.add(latest);
        }
        return list;
    }

    /**
     * 解析 API 响应
     * MarkSix6 返回格式示例（JSON）：
     * { "code": 0, "data": { "expect": "20240101001", "opencode": "1,2,3,4,5,6,7,8,9,10", "opentime": "2024-01-01 12:00:00" } }
     * 部分彩种可能返回不同字段名，做兼容处理
     */
    @SuppressWarnings("unchecked")
    private DrawResult parseResponse(String lotteryCode, String response) {
        try {
            // 简单 JSON 解析（不依赖 Jackson ObjectMapper，用字符串处理）
            // 实际项目中建议用 ObjectMapper
            response = response.trim();
            if (!response.startsWith("{")) {
                log.warn("MarkSix6 响应非JSON: {}", response.substring(0, Math.min(100, response.length())));
                return null;
            }

            // 提取 expect/period
            String period = extractJsonField(response, "expect");
            if (period == null) period = extractJsonField(response, "period");
            if (period == null) period = extractJsonField(response, "issue");

            // 提取 opencode/numbers
            String numbers = extractJsonField(response, "opencode");
            if (numbers == null) numbers = extractJsonField(response, "numbers");
            if (numbers == null) numbers = extractJsonField(response, "openCode");

            // 提取 opentime
            String openTimeStr = extractJsonField(response, "opentime");
            if (openTimeStr == null) openTimeStr = extractJsonField(response, "openTime");

            if (numbers == null || numbers.isEmpty()) {
                log.warn("MarkSix6 无法解析开奖号码, response={}", response.substring(0, Math.min(200, response.length())));
                return null;
            }

            DrawResult result = new DrawResult();
            result.setLotteryCode(lotteryCode);
            result.setPeriod(period != null ? period : LocalDateTime.now().format(PERIOD_FORMAT));
            result.setNumbers(numbers);
            result.setDrawTime(openTimeStr != null ? parseDateTime(openTimeStr) : LocalDateTime.now());
            result.setStatus(1);
            return result;
        } catch (Exception e) {
            log.error("MarkSix6 解析响应失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 JSON 字符串中提取字段值（简单实现，处理嵌套 data 对象）
     */
    private String extractJsonField(String json, String field) {
        // 先尝试直接字段
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        // 尝试数字字段
        pattern = "\"" + field + "\"\\s*:\\s*([0-9.]+)";
        p = java.util.regex.Pattern.compile(pattern);
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private LocalDateTime parseDateTime(String str) {
        try {
            // 尝试多种格式
            if (str.contains("T")) {
                return LocalDateTime.parse(str.replace("Z", ""));
            }
            if (str.contains("-")) {
                return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            return LocalDateTime.now();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /**
     * 彩种代码映射
     */
    private String mapLotteryType(String lotteryCode) {
        return switch (lotteryCode) {
            case "jspk10" -> "pk10";
            case "jsssc" -> "ssc";
            case "jsdd" -> "pc28";
            case "happy8lhc" -> "lhc";
            case "jsft" -> "feit";
            case "jsydh" -> "sports";
            default -> null;
        };
    }
}
