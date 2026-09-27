package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Order;
import com.h5.entity.Transaction;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wap/transaction")
public class TransactionController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private TransactionMapper transactionMapper;

    /**
     * 交易记录列表
     * GET /api/wap/transaction/list?page=1&pageSize=10&type=recharge
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getUserId, userId)
                .orderByDesc(Transaction::getCreateTime);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Transaction::getType, type);
        }

        Page<Transaction> pageResult = transactionMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /**
     * 充值订单列表
     * GET /api/wap/transaction/recharge-list
     */
    @GetMapping("/recharge-list")
    public Result<Map<String, Object>> rechargeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getType, "recharge")
                .orderByDesc(Order::getCreateTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> pageResult = orderMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    /**
     * 提现订单列表
     * GET /api/wap/transaction/withdraw-list
     */
    @GetMapping("/withdraw-list")
    public Result<Map<String, Object>> withdrawList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getType, "withdraw")
                .orderByDesc(Order::getCreateTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> pageResult = orderMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    /**
     * 订单详情
     * GET /api/wap/transaction/detail/{orderNo}
     */
    @GetMapping("/detail/{orderNo}")
    public Result<Order> detail(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
        );
        return Result.success(order);
    }
}
