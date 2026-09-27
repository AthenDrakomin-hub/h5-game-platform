package com.h5.config;

import com.h5.entity.User;
import com.h5.mapper.UserMapper;
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
 * AdminInterceptor 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminInterceptorTest {

    @Mock private UserMapper userMapper;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @InjectMocks private AdminInterceptor adminInterceptor;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("superadmin");
        adminUser.setStatus(1);

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUsername("user");
        normalUser.setRole("user");
        normalUser.setStatus(1);
    }

    @Test
    @DisplayName("OPTIONS预检请求放行")
    void testOptionsPreflight() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");
        assertTrue(adminInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("管理员角色通过")
    void testAdminRolePass() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        assertTrue(adminInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("普通用户角色被拒绝")
    void testNormalUserRejected() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(userMapper.selectById(2L)).thenReturn(normalUser);
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));
        boolean result = adminInterceptor.preHandle(request, response, new Object());
        assertFalse(result);
        verify(response).setStatus(403);
    }

    @Test
    @DisplayName("禁用账号被拒绝")
    void testDisabledUserRejected() throws Exception {
        adminUser.setStatus(0);
        when(request.getMethod()).thenReturn("POST");
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));
        assertFalse(adminInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("用户不存在被拒绝")
    void testUserNotFoundRejected() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(userMapper.selectById(999L)).thenReturn(null);
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));
        assertFalse(adminInterceptor.preHandle(request, response, new Object()));
    }
}
