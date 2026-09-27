package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wap/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户信息
     * GET /api/wap/user/info
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("balance", user.getBalance());
        data.put("frozenBalance", user.getFrozenBalance());
        data.put("vipLevel", user.getVipLevel());
        data.put("isTrial", user.getIsTrial());
        data.put("inviteCode", user.getInviteCode());
        data.put("createTime", user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        return Result.success(data);
    }

    /**
     * 更新用户信息
     * POST /api/wap/user/update
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        if (params.containsKey("nickname")) {
            user.setNickname(params.get("nickname"));
        }
        if (params.containsKey("avatar")) {
            user.setAvatar(params.get("avatar"));
        }
        if (params.containsKey("phone")) {
            user.setPhone(params.get("phone"));
        }
        userMapper.updateById(user);
        return Result.success();
    }

    /**
     * 修改密码
     * POST /api/wap/user/change-password
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("密码不能为空");
        }
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success();
    }
}
