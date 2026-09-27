package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap/vip")
public class VipController {

    @Autowired
    private UserMapper userMapper;

    /**
     * VIP 信息
     * GET /api/wap/vip/info
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);

        int currentLevel = user != null ? user.getVipLevel() : 1;
        BigDecimal balance = user != null ? user.getBalance() : BigDecimal.ZERO;

        // VIP 等级配置
        List<Map<String, Object>> levels = new ArrayList<>();
        int[][] levelConfig = {
                {1, 0, 10000, 0, 50000},
                {2, 10000, 50000, 1, 100000},
                {3, 50000, 200000, 2, 200000},
                {4, 200000, 500000, 3, 500000},
                {5, 500000, 1000000, 5, 1000000},
                {6, 1000000, 3000000, 8, 3000000},
                {7, 3000000, 5000000, 10, 5000000},
                {8, 5000000, 10000000, 12, 10000000},
                {9, 10000000, 30000000, 15, 30000000},
                {10, 30000000, 50000000, 18, 50000000},
                {11, 50000000, Integer.MAX_VALUE, 20, Integer.MAX_VALUE}
        };

        for (int[] cfg : levelConfig) {
            Map<String, Object> level = new HashMap<>();
            level.put("level", cfg[0]);
            level.put("minRecharge", cfg[1]);
            level.put("maxRecharge", cfg[2]);
            level.put("rebateRate", cfg[3] / 100.0);
            level.put("dailyWithdrawLimit", cfg[4]);
            levels.add(level);
        }

        // 当前等级信息
        Map<String, Object> currentLevelInfo = levels.get(currentLevel - 1);
        Map<String, Object> nextLevelInfo = currentLevel < levels.size() ? levels.get(currentLevel) : null;

        // 升级进度（模拟：用余额作为充值累计）
        int progress = 0;
        if (nextLevelInfo != null) {
            int minRecharge = (int) nextLevelInfo.get("minRecharge");
            int currentRecharge = balance.intValue();
            progress = Math.min(100, (int) (currentRecharge * 100.0 / minRecharge));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("currentLevel", currentLevel);
        data.put("currentLevelInfo", currentLevelInfo);
        data.put("nextLevel", nextLevelInfo != null ? nextLevelInfo.get("level") : null);
        data.put("nextLevelInfo", nextLevelInfo);
        data.put("progress", progress);
        data.put("levels", levels);
        data.put("balance", balance);
        return Result.success(data);
    }

    /**
     * VIP 等级列表
     * GET /api/wap/vip/levels
     */
    @GetMapping("/levels")
    public Result<List<Map<String, Object>>> levels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        int[][] levelConfig = {
                {1, 0, 0, 50000}, {2, 10000, 1, 100000}, {3, 50000, 2, 200000},
                {4, 200000, 3, 500000}, {5, 500000, 5, 1000000}, {6, 1000000, 8, 3000000},
                {7, 3000000, 10, 5000000}, {8, 5000000, 12, 10000000}, {9, 10000000, 15, 30000000},
                {10, 30000000, 18, 50000000}, {11, 50000000, 20, Integer.MAX_VALUE}
        };
        for (int[] cfg : levelConfig) {
            Map<String, Object> level = new HashMap<>();
            level.put("level", cfg[0]);
            level.put("minRecharge", cfg[1]);
            level.put("rebateRate", cfg[2] / 100.0);
            level.put("dailyWithdrawLimit", cfg[3]);
            levels.add(level);
        }
        return Result.success(levels);
    }
}
