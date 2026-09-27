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
@RequestMapping("/wap/yuebao")
public class YuebaoController {

    @Autowired private UserMapper userMapper;

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        Map<String, Object> data = new HashMap<>();
        data.put("annualRate", 0.08);
        data.put("dailyRate", 0.00022);
        data.put("minAmount", new BigDecimal("100"));
        data.put("maxAmount", new BigDecimal("1000000"));
        data.put("status", 1);
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("balance", new BigDecimal("0.00"));
        data.put("totalProfit", new BigDecimal("0.00"));
        data.put("todayProfit", new BigDecimal("0.00"));
        data.put("yesterdayProfit", new BigDecimal("0.00"));
        data.put("availableBalance", user != null ? user.getBalance() : new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> records(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @PostMapping("/transfer-in")
    public Result<Map<String, Object>> transferIn(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        if (amount.compareTo(user.getBalance()) > 0) return Result.error("余额不足");
        user.setBalance(user.getBalance().subtract(amount));
        userMapper.updateById(user);
        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("balance", user.getBalance());
        return Result.success(data);
    }

    @PostMapping("/transfer-out")
    public Result<Map<String, Object>> transferOut(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        user.setBalance(user.getBalance().add(amount));
        userMapper.updateById(user);
        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("balance", user.getBalance());
        return Result.success(data);
    }

    @PostMapping("/claim-all")
    public Result<Void> claimAll() {
        return Result.success();
    }
}
