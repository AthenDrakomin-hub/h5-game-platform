package com.h5.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RateLimitInterceptor 单元测试（内存降级模式）
 */
@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @InjectMocks private RateLimitInterceptor rateLimitInterceptor;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/wap/user/info");
    }

    @Test
    @DisplayName("正常请求通过")
    void testNormalRequestPass() throws Exception {
        assertTrue(rateLimitInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("全局限流：超过100次/分钟被拒绝")
    void testGlobalRateLimit() throws Exception {
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        // 前100次应该通过
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimitInterceptor.preHandle(request, response, new Object()));
        }
        // 第101次应该被拒绝
        assertFalse(rateLimitInterceptor.preHandle(request, response, new Object()));
        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    @DisplayName("登录接口限流：超过5次/分钟被拒绝")
    void testLoginRateLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/wap/auth/login");
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitInterceptor.preHandle(request, response, new Object()));
        }
        assertFalse(rateLimitInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("不同IP限流独立计数")
    void testDifferentIpIndependent() throws Exception {
        // IP1 请求100次
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimitInterceptor.preHandle(request, response, new Object()));
        }
        // IP2 应该仍然可以请求
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");
        assertTrue(rateLimitInterceptor.preHandle(request, response, new Object()));
    }
}
