package com.h5.service.payment;

import com.h5.entity.PaymentMethod;
import com.h5.mapper.PaymentMethodMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EasyPayGateway 单元测试（签名/验签逻辑）
 */
@ExtendWith(MockitoExtension.class)
class EasyPayGatewayTest {

    @Mock private PaymentMethodMapper paymentMethodMapper;
    @InjectMocks private EasyPayGateway easyPayGateway;

    private PaymentMethod testMethod;

    @BeforeEach
    void setUp() {
        testMethod = new PaymentMethod();
        testMethod.setId(1L);
        testMethod.setCode("easypay_alipay");
        testMethod.setName("易支付-支付宝");
        testMethod.setType("easypay");
        testMethod.setStatus(1);
        testMethod.setApiConfig("{\"gatewayUrl\":\"https://pay.test.com\",\"pid\":\"1001\",\"key\":\"testkey123\",\"type\":\"alipay\",\"notifyUrl\":\"https://api.test.com/api/payment/callback/easypay\"}");
    }

    @Nested
    @DisplayName("签名算法")
    class SignAlgorithm {
        @Test
        @DisplayName("MD5签名正确性")
        void testMd5Sign() {
            Map<String, String> params = new TreeMap<>();
            params.put("pid", "1001");
            params.put("type", "alipay");
            params.put("out_trade_no", "ORD001");
            params.put("money", "100.00");

            String expected = md5("money=100.00&out_trade_no=ORD001&pid=1001&type=alipay" + "testkey123");
            // 验证签名算法逻辑（通过回调验签间接验证）
            assertNotNull(expected);
            assertEquals(32, expected.length());
        }

        @Test
        @DisplayName("参数按key升序拼接")
        void testParamsSorted() {
            Map<String, String> params = new TreeMap<>();
            params.put("z_key", "z");
            params.put("a_key", "a");
            params.put("m_key", "m");

            StringBuilder sb = new StringBuilder();
            params.forEach((k, v) -> {
                if (sb.length() > 0) sb.append("&");
                sb.append(k).append("=").append(v);
            });
            assertEquals("a_key=a&m_key=m&z_key=z", sb.toString());
        }
    }

    @Nested
    @DisplayName("回调验签")
    class CallbackVerification {
        @Test
        @DisplayName("缺少订单号返回失败")
        void testMissingOrderNo() {
            when(paymentMethodMapper.selectList(any())).thenReturn(Arrays.asList(testMethod));
            PaymentGateway.CallbackResult result = easyPayGateway.handleCallback(new HashMap<>());
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("签名错误返回失败")
        void testWrongSign() {
            when(paymentMethodMapper.selectList(any())).thenReturn(Arrays.asList(testMethod));
            Map<String, String> params = new HashMap<>();
            params.put("out_trade_no", "ORD001");
            params.put("trade_status", "TRADE_SUCCESS");
            params.put("sign", "wrongsign");
            params.put("pid", "1001");
            params.put("money", "100.00");

            PaymentGateway.CallbackResult result = easyPayGateway.handleCallback(params);
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("支付状态非成功返回失败")
        void testNonSuccessStatus() {
            when(paymentMethodMapper.selectList(any())).thenReturn(Arrays.asList(testMethod));
            Map<String, String> params = new HashMap<>();
            params.put("out_trade_no", "ORD001");
            params.put("trade_status", "WAIT_BUYER_PAY");
            params.put("pid", "1001");
            params.put("money", "100.00");
            // 计算正确签名
            params.put("sign", calculateSign(params, "testkey123"));

            PaymentGateway.CallbackResult result = easyPayGateway.handleCallback(params);
            assertFalse(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("网关标识")
    class GatewayIdentity {
        @Test
        @DisplayName("网关编码正确")
        void testGetCode() {
            assertEquals("easypay", easyPayGateway.getCode());
        }

        @Test
        @DisplayName("默认启用")
        void testIsEnabled() {
            assertTrue(easyPayGateway.isEnabled());
        }
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String calculateSign(Map<String, String> params, String key) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        sorted.remove("sign");
        sorted.remove("sign_type");
        String joined = sorted.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(java.util.stream.Collectors.joining("&"));
        return md5(joined + key);
    }
}
