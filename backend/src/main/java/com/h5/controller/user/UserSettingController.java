package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/wap/user")
public class UserSettingController {

    @Autowired private UserMapper userMapper;

    @GetMapping("/avatars")
    public Result<List<Map<String, Object>>> avatars() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(Map.of("id", i, "url", "/uploads/avatars/avatar" + i + ".png"));
        }
        return Result.success(list);
    }

    @GetMapping("/device-info")
    public Result<Map<String, Object>> deviceInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", "device_" + UserContext.getUserId());
        data.put("deviceName", "iPhone 15 Pro");
        data.put("os", "iOS 17.5");
        data.put("lastLoginTime", "2024-01-01 12:00:00");
        return Result.success(data);
    }

    @GetMapping("/bind-status")
    public Result<Map<String, Object>> bindStatus() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("phoneBound", user.getPhone() != null && !user.getPhone().isEmpty());
        data.put("phone", user.getPhone() != null ? maskPhone(user.getPhone()) : "");
        data.put("emailBound", user.getEmail() != null && !user.getEmail().isEmpty());
        data.put("email", user.getEmail() != null ? user.getEmail() : "");
        data.put("fundPasswordSet", user.getFundPassword() != null && !user.getFundPassword().isEmpty());
        data.put("birthdaySet", false);
        return Result.success(data);
    }

    @PostMapping("/set-withdraw-password")
    public Result<Void> setWithdrawPassword(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        String password = params.get("password");
        if (password == null || password.length() < 6) return Result.error("资金密码至少6位");
        user.setFundPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password));
        userMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/bind-phone")
    public Result<Void> bindPhone(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        user.setPhone(params.get("phone"));
        userMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/bind-email")
    public Result<Void> bindEmail(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        user.setEmail(params.get("email"));
        userMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/set-birthday")
    public Result<Void> setBirthday(@RequestBody Map<String, String> params) {
        return Result.success();
    }

    @PostMapping("/update-avatar")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        user.setAvatar(params.get("avatar"));
        userMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadImage() {
        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/temp/upload_" + System.currentTimeMillis() + ".png");
        return Result.success(data);
    }

    @PostMapping("/feedback/submit")
    public Result<Void> submitFeedback(@RequestBody Map<String, String> params) {
        return Result.success();
    }

    @GetMapping("/feedback/my-list")
    public Result<Map<String, Object>> myFeedback(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/feedback/replies/{id}")
    public Result<List<Map<String, Object>>> feedbackReplies(@PathVariable Long id) {
        return Result.success(new ArrayList<>());
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
