package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Order;
import com.h5.entity.User;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.UserMapper;
import com.h5.service.AdminLogService;
import com.h5.service.BalanceService;
import com.h5.service.BotNotifyService;
import com.h5.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private BalanceService balanceService;
    @Autowired private AdminLogService adminLogService;
    @Autowired private BotNotifyService botNotifyService;

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

    /**
     * 充值审核通过（使用 BalanceService 安全加余额）
     */
    @PostMapping("/recharge/approve")
    @Transactional
    public Result<Void> approveRecharge(@RequestBody Map<String, Object> params,
                                         HttpServletRequest request) {
        Long id = Long.valueOf(params.get("id").toString());
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"pending".equals(order.getStatus())) throw new BusinessException("订单状态不允许审核");

        String beforeStatus = order.getStatus();
        order.setStatus("success");
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 使用 BalanceService 安全加余额（乐观锁 + 自动写流水）
        User updated = balanceService.addBalance(
                order.getUserId(),
                order.getAmount(),
                "recharge",
                order.getId(),
                order.getOrderNo(),
                "充值到账(" + order.getMethodName() + ")"
        );

        // 通知用户 Telegram
        botNotifyService.sendToUser(order.getUserId(), "recharge", BotNotifyService.data(
                "amount", order.getAmount(),
                "payType", order.getMethodName(),
                "balance", updated.getBalance()
        ));

        // 记录操作日志
        adminLogService.log(UserContext.getUserId(), UserContext.getUsername(),
                "approve_recharge", "order", id,
                "{\"status\":\"" + beforeStatus + "\"}",
                "{\"status\":\"success\",\"amount\":" + order.getAmount() + "}",
                request);

        return Result.success();
    }

    @PostMapping("/recharge/reject")
    public Result<Void> rejectRecharge(@RequestBody Map<String, Object> params,
                                        HttpServletRequest request) {
        Long id = Long.valueOf(params.get("id").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        String beforeStatus = order.getStatus();
        order.setStatus("failed");
        order.setAdminRemark(reason);
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        adminLogService.log(UserContext.getUserId(), UserContext.getUsername(),
                "reject_recharge", "order", id,
                "{\"status\":\"" + beforeStatus + "\"}",
                "{\"status\":\"failed\",\"reason\":\"" + reason + "\"}",
                request);
        return Result.success();
    }

    /**
     * 提现审核通过（使用 BalanceService 确认冻结扣减）
     */
    @PostMapping("/withdraw/approve")
    @Transactional
    public Result<Void> approveWithdraw(@RequestBody Map<String, Object> params,
                                         HttpServletRequest request) {
        Long id = Long.valueOf(params.get("id").toString());
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"pending".equals(order.getStatus())) throw new BusinessException("订单状态不允许审核");

        String beforeStatus = order.getStatus();
        order.setStatus("success");
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 使用 BalanceService 确认冻结扣减（只扣 frozen_balance，乐观锁）
        balanceService.confirmFreeze(order.getUserId(), order.getAmount());

        botNotifyService.sendToUser(order.getUserId(), "withdraw_success", BotNotifyService.data(
                "amount", order.getAmount(),
                "account", order.getAccount() != null ? order.getAccount() : "",
                "balance", 0
        ));

        adminLogService.log(UserContext.getUserId(), UserContext.getUsername(),
                "approve_withdraw", "order", id,
                "{\"status\":\"" + beforeStatus + "\"}",
                "{\"status\":\"success\",\"amount\":" + order.getAmount() + "}",
                request);
        return Result.success();
    }

    /**
     * 提现审核拒绝（使用 BalanceService 解冻退回）
     */
    @PostMapping("/withdraw/reject")
    @Transactional
    public Result<Void> rejectWithdraw(@RequestBody Map<String, Object> params,
                                        HttpServletRequest request) {
        Long id = Long.valueOf(params.get("id").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        String beforeStatus = order.getStatus();
        order.setStatus("failed");
        order.setAdminRemark(reason);
        order.setAuditTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 使用 BalanceService 解冻退回（frozen → balance，乐观锁 + 自动写流水）
        balanceService.unfreezeAndRefund(order.getUserId(), order.getAmount(), order.getOrderNo());

        botNotifyService.sendToUser(order.getUserId(), "system", BotNotifyService.data(
                "content", "您的提现申请 " + order.getOrderNo() + " 已被拒绝"
                        + (reason.isEmpty() ? "" : "，原因：" + reason) + "，金额已退回余额。"
        ));

        adminLogService.log(UserContext.getUserId(), UserContext.getUsername(),
                "reject_withdraw", "order", id,
                "{\"status\":\"" + beforeStatus + "\"}",
                "{\"status\":\"failed\",\"reason\":\"" + reason + "\"}",
                request);
        return Result.success();
    }
}
