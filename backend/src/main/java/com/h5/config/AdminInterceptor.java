package com.h5.config;

import com.h5.common.UserContext;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import com.h5.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端鉴权拦截器
 * 在 AuthInterceptor 之后执行，校验用户角色是否为 admin/superadmin
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            writeForbidden(response, "未登录");
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            writeForbidden(response, "用户不存在");
            return false;
        }

        if (user.getStatus() == 0) {
            writeForbidden(response, "账号已被禁用");
            return false;
        }

        String role = user.getRole();
        if (!"admin".equals(role) && !"superadmin".equals(role)) {
            writeForbidden(response, "没有管理权限");
            return false;
        }

        return true;
    }

    private void writeForbidden(HttpServletResponse response, String msg) {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":403,\"msg\":\"" + msg + "\",\"time\":" + System.currentTimeMillis() + "}");
        } catch (Exception e) {
            // ignore
        }
    }
}
