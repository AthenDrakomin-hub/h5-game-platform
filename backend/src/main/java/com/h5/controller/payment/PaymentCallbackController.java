package com.h5.controller.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.h5.entity.Order;
import com.h5.mapper.OrderMapper;
import com.h5.service.BalanceService;
import com.h5.service.payment.EasyPayGateway;
import com.h5.service.payment.PaymentGateway;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调 Controller
 * 接收第三方支付平台的异步回调通知，验签后确认到账
 *
 * 回调接口：
 * - POST /api/payment/callback/easypay  易支付异步通知
 * - GET  /api/payment/callback/easypay  易支付同步跳转（返回页面）
 */
@RestController
@RequestMapping("/payment/callback")
public class PaymentCallbackController {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackController.class);

    @Autowired(required = false)
    private EasyPayGateway easyPayGateway;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BalanceService balanceService;

    /**
     * 易支付异步回调（POST）
     * 验签通过后自动确认充值到账
     * 返回 "success" 表示处理成功，否则返回错误信息
     */
    @PostMapping("/easypay")
    public String easyPayCallback(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        log.info("收到易支付回调: {}", params);

        if (easyPayGateway == null) {
            log.error("易支付网关未启用（payment.easypay.enabled=false）");
            return "fail: gateway not enabled";
        }

        PaymentGateway.CallbackResult result = easyPayGateway.handleCallback(params);
        if (!result.isSuccess()) {
            log.warn("易支付回调验签失败: {}", result.getMessage());
            return "fail: " + result.getMessage();
        }

        // 确认订单到账
        String orderNo = result.getOrderNo();
        return confirmOrder(orderNo, "easypay");
    }

    /**
     * 易支付同步跳转（GET）
     * 支付完成后用户浏览器跳转，返回简单HTML提示
     */
    @GetMapping("/easypay")
    public String easyPayReturn(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        String orderNo = params.get("out_trade_no");
        String status = params.get("trade_status");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<title>支付结果</title>");
        html.append("<style>body{font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;background:#f5f5f5;}");
        html.append(".card{background:white;padding:40px;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,0.1);text-align:center;}");
        html.append(".success{color:#67c23a;font-size:24px;margin-bottom:16px;}");
        html.append(".pending{color:#e6a23c;font-size:24px;margin-bottom:16px;}");
        html.append("</style></head><body><div class='card'>");

        if ("TRADE_SUCCESS".equals(status)) {
            html.append("<div class='success'>支付成功</div>");
        } else {
            html.append("<div class='pending'>支付处理中</div>");
        }
        html.append("<p>订单号: ").append(orderNo != null ? orderNo : "").append("</p>");
        html.append("<p>请在App中查看余额到账情况</p>");
        html.append("</div></body></html>");
        return html.toString();
    }

    /**
     * 确认充值订单到账
     * 并发安全：使用状态机乐观锁 UPDATE ... WHERE status='pending'
     * 高并发下只有一个请求能更新成功，其余请求影响行数=0直接跳过
     */
    private String confirmOrder(String orderNo, String method) {
        if (orderNo == null || orderNo.isEmpty()) {
            return "fail: missing orderNo";
        }

        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
        );
        if (order == null) {
            log.warn("回调订单不存在: {}", orderNo);
            return "fail: order not found";
        }

        // 已处理的订单直接返回成功（幂等）
        if ("success".equals(order.getStatus())) {
            log.info("订单已处理，跳过: {}", orderNo);
            return "success";
        }

        if (!"pending".equals(order.getStatus())) {
            log.warn("订单状态不允许回调确认: orderNo={}, status={}", orderNo, order.getStatus());
            return "fail: invalid order status";
        }

        // 状态机乐观锁：只有status=pending时才能更新为success
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_no", orderNo)
                .eq("status", "pending")
                .set("status", "success")
                .set("audit_time", LocalDateTime.now())
                .set("remark", "三方支付自动到账(" + method + ")");
        int rows = orderMapper.update(null, updateWrapper);

        // 影响行数=0说明已被其他并发请求处理，幂等跳过
        if (rows == 0) {
            log.info("订单已被并发处理，跳过: {}", orderNo);
            return "success";
        }

        // 加余额 + 写流水
        balanceService.addBalance(
                order.getUserId(),
                order.getAmount(),
                "recharge",
                order.getId(),
                order.getOrderNo(),
                "三方支付(" + method + ")自动到账"
        );

        log.info("三方支付回调确认到账: orderNo={}, amount={}, method={}",
                orderNo, order.getAmount(), method);
        return "success";
    }

    /**
     * 从HttpServletRequest提取所有参数
     */
    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }
}
