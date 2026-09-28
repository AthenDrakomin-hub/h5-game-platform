package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import com.h5.service.AdminLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired private UserMapper userMapper;
    @Autowired private AdminLogService adminLogService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer vipLevel) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        if (status != null) wrapper.eq(User::getStatus, status);
        if (vipLevel != null) wrapper.eq(User::getVipLevel, vipLevel);

        Page<User> pageResult = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/detail/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        user.setFundPassword(null);
        return Result.success(user);
    }

    @PostMapping("/update-status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        String before = "status=" + user.getStatus();
        user.setStatus(status);
        userMapper.updateById(user);
        writeAdminLog("update_user_status", "user", id, before, "status=" + status);
        return Result.success();
    }

    @PostMapping("/adjust-balance")
    public Result<Void> adjustBalance(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String type = params.get("type") != null ? params.get("type").toString() : "add";
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        BigDecimal before = user.getBalance();
        if ("add".equals(type)) {
            user.setBalance(user.getBalance().add(amount));
        } else {
            if (amount.compareTo(user.getBalance()) > 0) throw new BusinessException("余额不足");
            user.setBalance(user.getBalance().subtract(amount));
        }
        userMapper.updateById(user);
        writeAdminLog("adjust_balance", "user", id,
                "balance=" + before + ",type=" + type + ",amount=" + amount,
                "balance=" + user.getBalance());
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        String newPassword = params.get("password");
        if (newPassword == null || newPassword.length() < 6) throw new BusinessException("密码至少6位");
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
        writeAdminLog("reset_password", "user", id, "password=***", "password=***");
        return Result.success();
    }

    @PostMapping("/set-vip")
    public Result<Void> setVip(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer vipLevel = Integer.valueOf(params.get("vipLevel").toString());
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        Integer before = user.getVipLevel();
        user.setVipLevel(vipLevel);
        userMapper.updateById(user);
        writeAdminLog("set_vip", "user", id, "vipLevel=" + before, "vipLevel=" + vipLevel);
        return Result.success();
    }

    /** 记录管理员操作日志 */
    private void writeAdminLog(String action, String targetType, Long targetId,
                               String beforeData, String afterData) {
        try {
            Long adminId = UserContext.getUserId();
            User admin = adminId != null ? userMapper.selectById(adminId) : null;
            String adminName = admin != null ? admin.getUsername() : "admin";
            adminLogService.log(adminId, adminName, action, targetType, targetId, beforeData, afterData);
        } catch (Exception ignored) { /* 日志记录失败不影响主流程 */ }
    }
}
