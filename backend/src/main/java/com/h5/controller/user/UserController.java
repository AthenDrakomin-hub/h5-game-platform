package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户中心 Controller（委托 UserService）
 */
@RestController
@RequestMapping("/wap/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户信息
     * GET /api/wap/user/info
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.success(userService.getUserInfo(UserContext.getUserId()));
    }

    /**
     * 更新用户信息
     * POST /api/wap/user/update
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Map<String, String> params) {
        userService.updateProfile(
                UserContext.getUserId(),
                params.get("nickname"),
                params.get("avatar"),
                params.get("phone"),
                params.get("email")
        );
        return Result.success();
    }

    /**
     * 修改登录密码
     * POST /api/wap/user/change-password
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        userService.changePassword(
                UserContext.getUserId(),
                params.get("oldPassword"),
                params.get("newPassword")
        );
        return Result.success();
    }

    /**
     * 设置资金密码
     * POST /api/wap/user/set-fund-password
     */
    @PostMapping("/set-fund-password")
    public Result<Void> setFundPassword(@RequestBody Map<String, String> params) {
        userService.setFundPassword(UserContext.getUserId(), params.get("password"));
        return Result.success();
    }

    /**
     * 修改资金密码
     * POST /api/wap/user/change-fund-password
     */
    @PostMapping("/change-fund-password")
    public Result<Void> changeFundPassword(@RequestBody Map<String, String> params) {
        userService.changeFundPassword(
                UserContext.getUserId(),
                params.get("oldPassword"),
                params.get("newPassword")
        );
        return Result.success();
    }
}
