package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.entity.Order;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private TransactionMapper transactionMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime);
        if (type != null && !type.isEmpty()) wrapper.eq(Order::getType, type);
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like(Order::getOrderNo, orderNo);

        Page<Order> pageResult = orderMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/detail/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        return Result.success(order);
    }

    @PostMapping("/recharge/approve")
    @Transactional
    public Result<Void> approveRecharge(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"pending".equals(order.getStatus())) throw new BusinessException("订单状态不允许审核");

        order.setStatus("success");
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 加余额
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            user.setBalance(user.getBalance().add(order.getAmount()));
            userMapper.updateById(user);

            Transaction tx = new Transaction();
            tx.setUserId(user.getId());
            tx.setType("recharge");
            tx.setAmount(order.getAmount());
            tx.setBalanceBefore(user.getBalance().subtract(order.getAmount()));
            tx.setBalanceAfter(user.getBalance());
            tx.setRefId(order.getId());
            tx.setRefNo(order.getOrderNo());
            tx.setDescription("充值到账");
            transactionMapper.insert(tx);
        }
        return Result.success();
    }

    @PostMapping("/recharge/reject")
    public Result<Void> rejectRecharge(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        order.setStatus("failed");
        order.setAdminRemark(reason);
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success();
    }

    @PostMapping("/withdraw/approve")
    @Transactional
    public Result<Void> approveWithdraw(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"pending".equals(order.getStatus())) throw new BusinessException("订单状态不允许审核");

        order.setStatus("success");
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 解冻余额（提现时已冻结，审核通过扣冻结）
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            user.setFrozenBalance(user.getFrozenBalance().subtract(order.getAmount()));
            userMapper.updateById(user);
        }
        return Result.success();
    }

    @PostMapping("/withdraw/reject")
    @Transactional
    public Result<Void> rejectWithdraw(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        order.setStatus("failed");
        order.setAdminRemark(reason);
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 退回余额
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            user.setBalance(user.getBalance().add(order.getAmount()));
            user.setFrozenBalance(user.getFrozenBalance().subtract(order.getAmount()));
            userMapper.updateById(user);
        }
        return Result.success();
    }
}
