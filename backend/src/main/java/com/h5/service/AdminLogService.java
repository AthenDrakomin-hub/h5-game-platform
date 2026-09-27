package com.h5.service;

import com.h5.entity.AdminOperationLog;
import com.h5.mapper.AdminOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 管理员操作日志服务
 */
@Service
public class AdminLogService {

    @Autowired
    private AdminOperationLogMapper logMapper;

    /**
     * 记录操作日志
     */
    public void log(Long adminId, String adminName, String action,
                    String targetType, Long targetId,
                    String beforeData, String afterData,
                    HttpServletRequest request) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName != null ? adminName : "");
        log.setAction(action);
        log.setTargetType(targetType != null ? targetType : "");
        log.setTargetId(targetId);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setIp(getClientIp(request));
        log.setUserAgent(request != null ? request.getHeader("User-Agent") : "");
        logMapper.insert(log);
    }

    /**
     * 简化版记录（无 request）
     */
    public void log(Long adminId, String adminName, String action,
                    String targetType, Long targetId,
                    String beforeData, String afterData) {
        log(adminId, adminName, action, targetType, targetId, beforeData, afterData, null);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "";
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
}
