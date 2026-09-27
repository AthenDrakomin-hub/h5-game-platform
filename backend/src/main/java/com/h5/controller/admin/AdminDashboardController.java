package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.Result;
import com.h5.entity.*;
import com.h5.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired private UserMapper userMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private BetMapper betMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();

        // 用户统计
        long totalUsers = userMapper.selectCount(null);
        long todayUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, LocalDate.now().atStartOfDay())
        );

        // 充值统计
        List<Order> rechargeOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getType, "recharge").eq(Order::getStatus, "success")
        );
        BigDecimal totalRecharge = rechargeOrders.stream().map(Order::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 提现统计
        List<Order> withdrawOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getType, "withdraw").eq(Order::getStatus, "success")
        );
        BigDecimal totalWithdraw = withdrawOrders.stream().map(Order::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 投注统计
        long totalBets = betMapper.selectCount(null);
        List<Bet> bets = betMapper.selectList(null);
        BigDecimal totalBetAmount = bets.stream().map(Bet::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWinAmount = bets.stream().map(Bet::getWinAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 待审核
        long pendingRecharge = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getType, "recharge").eq(Order::getStatus, "pending")
        );
        long pendingWithdraw = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getType, "withdraw").eq(Order::getStatus, "pending")
        );

        data.put("totalUsers", totalUsers);
        data.put("todayUsers", todayUsers);
        data.put("totalRecharge", totalRecharge);
        data.put("totalWithdraw", totalWithdraw);
        data.put("netProfit", totalRecharge.subtract(totalWithdraw));
        data.put("totalBets", totalBets);
        data.put("totalBetAmount", totalBetAmount);
        data.put("totalWinAmount", totalWinAmount);
        data.put("pendingRecharge", pendingRecharge);
        data.put("pendingWithdraw", pendingWithdraw);
        return Result.success(data);
    }

    @GetMapping("/recent-orders")
    public Result<List<Order>> recentOrders(@RequestParam(defaultValue = "10") int limit) {
        List<Order> list = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime).last("LIMIT " + limit)
        );
        return Result.success(list);
    }

    @GetMapping("/recent-users")
    public Result<List<User>> recentUsers(@RequestParam(defaultValue = "10") int limit) {
        List<User> list = userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime).last("LIMIT " + limit)
        );
        return Result.success(list);
    }
}
