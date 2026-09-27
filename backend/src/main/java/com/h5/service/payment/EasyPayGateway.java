package com.h5.service.payment;

import com.h5.entity.Order;
import com.h5.entity.PaymentMethod;
import com.h5.mapper.PaymentMethodMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 易支付网关实现（兼容彩虹易支付/码支付等通用易支付接口）
 *
 * 接口规范：
 * - 提交支付：POST {gateway}/submit.php 或 mapi.php
 * - 签名算法：MD5(参数按key升序拼接 key=value& + key)
 * - 异步回调：POST notify_url，验签后确认到账
 *
 * 启用条件：payment.easypay.enabled=true
 * 配置存储在 payment_methods 表的 api_config 字段（JSON）：
 * {
 *   "gatewayUrl": "https://pay.example.com",
 *   "pid": "商户ID",
 *   "key": "商户密钥",
 *   "type": "alipay"  (默认支付方式，可被订单覆盖)
 * }
 */
@Component
@ConditionalOnProperty(name = "payment.easypay.enabled", havingValue = "true")
public class EasyPayGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(EasyPayGateway.class);

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getCode() {
        return "easypay";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 创建支付订单（返回支付链接/表单参数）
     */
    @Override
    public Map<String, Object> createPayment(Order order) {
        EasyPayConfig config = loadConfig(order.getMethod());
        if (config == null) {
            throw new RuntimeException("易支付配置未找到，method=" + order.getMethod());
        }

        // 构建支付参数
        Map<String, String> params = new TreeMap<>();
        params.put("pid", config.pid);
        params.put("type", config.type != null ? config.type : "alipay");
        params.put("out_trade_no", order.getOrderNo());
        params.put("notify_url", config.notifyUrl != null ? config.notifyUrl : "");
        params.put("return_url", config.returnUrl != null ? config.returnUrl : "");
        params.put("name", "账户充值-" + order.getAmount() + "元");
        params.put("money", order.getAmount().toString());
        params.put("sign_type", "MD5");

        // 生成签名
        String sign = generateSign(params, config.key);
        params.put("sign", sign);

        // 构建支付URL（GET方式跳转）
        String payUrl = config.gatewayUrl + "/submit.php?" + params.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        Map<String, Object> result = new HashMap<>();
        result.put("payUrl", payUrl);
        result.put("params", params);
        result.put("orderNo", order.getOrderNo());
        result.put("amount", order.getAmount());
        return result;
    }

    /**
     * 处理支付异步回调
     */
    @Override
    public CallbackResult handleCallback(Map<String, String> params) {
        try {
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            String money = params.get("money");
            String sign = params.get("sign");

            if (orderNo == null || orderNo.isEmpty()) {
                return CallbackResult.fail("缺少订单号");
            }

            // 加载配置验签
            EasyPayConfig config = loadConfigByOrderNo(orderNo);
            if (config == null) {
                return CallbackResult.fail("支付配置未找到");
            }

            // 验签
            Map<String, String> signParams = new TreeMap<>(params);
            signParams.remove("sign");
            signParams.remove("sign_type");
            String expectedSign = generateSign(signParams, config.key);
            if (!expectedSign.equalsIgnoreCase(sign)) {
                log.warn("易支付回调验签失败, orderNo={}, expected={}, actual={}",
                        orderNo, expectedSign, sign);
                return CallbackResult.fail("签名验证失败");
            }

            // 检查支付状态
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"1".equals(tradeStatus)) {
                return CallbackResult.fail("支付未成功: " + tradeStatus);
            }

            log.info("易支付回调成功, orderNo={}, tradeNo={}, money={}", orderNo, tradeNo, money);
            return CallbackResult.ok(orderNo);

        } catch (Exception e) {
            log.error("易支付回调处理异常", e);
            return CallbackResult.fail("处理异常: " + e.getMessage());
        }
    }

    /**
     * 主动查询支付状态
     */
    @Override
    public String queryPayment(String orderNo) {
        try {
            EasyPayConfig config = loadConfigByOrderNo(orderNo);
            if (config == null) return "unknown";

            Map<String, String> params = new TreeMap<>();
            params.put("pid", config.pid);
            params.put("out_trade_no", orderNo);
            params.put("sign", generateSign(params, config.key));

            String url = config.gatewayUrl + "/api.php?act=order&" + params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                    .collect(Collectors.joining("&"));

            String response = restTemplate.getForObject(url, String.class);
            if (response != null && response.contains("TRADE_SUCCESS")) {
                return "success";
            }
            return "pending";
        } catch (Exception e) {
            log.error("易支付查询失败, orderNo={}, error={}", orderNo, e.getMessage());
            return "unknown";
        }
    }

    // ==================== 内部工具 ====================

    /**
     * 从 payment_methods 表加载易支付配置
     */
    private EasyPayConfig loadConfig(String methodCode) {
        PaymentMethod pm = paymentMethodMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getCode, methodCode)
        );
        if (pm == null || pm.getApiConfig() == null) return null;
        return parseConfig(pm.getApiConfig());
    }

    /**
     * 通过订单号反查配置（先查订单获取 method，再查配置）
     * 简化实现：遍历所有易支付类型的支付方式
     */
    private EasyPayConfig loadConfigByOrderNo(String orderNo) {
        List<PaymentMethod> methods = paymentMethodMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getStatus, 1)
        );
        for (PaymentMethod pm : methods) {
            if (pm.getApiConfig() != null && pm.getApiConfig().contains("easypay")) {
                EasyPayConfig config = parseConfig(pm.getApiConfig());
                if (config != null) return config;
            }
        }
        // fallback: 找第一个有 api_config 的
        for (PaymentMethod pm : methods) {
            if (pm.getApiConfig() != null && !pm.getApiConfig().isEmpty()) {
                EasyPayConfig config = parseConfig(pm.getApiConfig());
                if (config != null && config.pid != null) return config;
            }
        }
        return null;
    }

    private EasyPayConfig parseConfig(String json) {
        try {
            EasyPayConfig config = new EasyPayConfig();
            config.gatewayUrl = extractJsonString(json, "gatewayUrl");
            config.pid = extractJsonString(json, "pid");
            config.key = extractJsonString(json, "key");
            config.type = extractJsonString(json, "type");
            config.notifyUrl = extractJsonString(json, "notifyUrl");
            config.returnUrl = extractJsonString(json, "returnUrl");
            if (config.gatewayUrl == null || config.pid == null || config.key == null) {
                return null;
            }
            return config;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonString(String json, String field) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 易支付签名：MD5(参数按key升序 key=value& + key)
     */
    private String generateSign(Map<String, String> params, String key) {
        String sorted = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return md5(sorted + key);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 error", e);
        }
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    /** 易支付配置 */
    private static class EasyPayConfig {
        String gatewayUrl;
        String pid;
        String key;
        String type;
        String notifyUrl;
        String returnUrl;
    }
}
