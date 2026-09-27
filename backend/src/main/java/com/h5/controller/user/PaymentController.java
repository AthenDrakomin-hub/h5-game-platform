package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Order;
import com.h5.entity.PaymentMethod;
import com.h5.entity.User;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.UserMapper;
import com.h5.service.BalanceService;
import com.h5.service.payment.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/wap/payment-methods")
public class PaymentController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private BalanceService balanceService;
    @Autowired private PaymentMethodService paymentMethodService;

    /**
     * 充值方式列表（从数据库 payment_methods 表读取）
     * GET /api/wap/payment/recharge-methods
     */
    @GetMapping("/recharge-methods")
    public Result<Map<String, Object>> rechargeMethods() {
        return Result.success(paymentMethodService.getRechargeMethods());
    }

    /**
     * 提现方式列表（从数据库读取）
     * GET /api/wap/payment/withdraw-methods
     */
    @GetMapping("/withdraw-methods")
    public Result<Map<String, Object>> withdrawMethods() {
        return Result.success(paymentMethodService.getWithdrawMethods());
    }

    /**
     * 创建充值订单
     * POST /api/wap/payment/recharge/create
     */
    @PostMapping("/recharge/create")
    @Transactional
    public Result<Map<String, Object>> createRecharge(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");
        if (user.getIsTrial() == 1) throw new BusinessException("试玩账号不支持充值，请先注册正式账号");

        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String method = params.get("method") != null ? params.get("method").toString() : "alipay";

        // 从支付方式表获取配置
        PaymentMethod pm = paymentMethodService.getByCode(method);
        BigDecimal minAmount = pm != null && pm.getMinAmount() != null ? pm.getMinAmount() : new BigDecimal("100");
        BigDecimal maxAmount = pm != null && pm.getMaxAmount() != null ? pm.getMaxAmount() : new BigDecimal("50000");

        if (amount.compareTo(minAmount) < 0) throw new BusinessException("最低充值金额" + minAmount + "元");
        if (amount.compareTo(maxAmount) > 0) throw new BusinessException("最高充值金额" + maxAmount + "元");

        String orderNo = "R" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(1000);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType("recharge");
        order.setAmount(amount);
        order.setFee(BigDecimal.ZERO);
        order.setActualAmount(amount);
        order.setMethod(method);
        order.setMethodName(pm != null ? pm.getName() : method);
        order.setStatus("pending");
        order.setPayAccount(pm != null && pm.getAddress() != null ? pm.getAddress() : "收款账户_" + method);
        order.setPayQrcode(pm != null && pm.getQrcode() != null ? pm.getQrcode() : "");
        orderMapper.insert(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("amount", amount);
        data.put("method", method);
        data.put("payAccount", order.getPayAccount());
        data.put("payQrcode", order.getPayQrcode());
        data.put("status", "pending");
        // USDT 自动到账标识
        if (pm != null && pm.getAutoConfirm() != null && pm.getAutoConfirm() == 1) {
            data.put("autoConfirm", true);
            data.put("chain", pm.getChain());
        }
        return Result.success(data);
    }

    /**
     * 创建提现申请（使用 BalanceService 安全冻结）
     * POST /api/wap/payment/withdraw/create
     */
    @PostMapping("/withdraw/create")
    @Transactional
    public Result<Map<String, Object>> createWithdraw(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");
        if (user.getIsTrial() == 1) throw new BusinessException("试玩账号不支持提现");

        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String method = params.get("method") != null ? params.get("method").toString() : "bank";
        String account = params.get("account") != null ? params.get("account").toString() : "";

        // 从支付方式表获取配置
        PaymentMethod pm = paymentMethodService.getByCode(method);
        BigDecimal minAmount = pm != null && pm.getMinAmount() != null ? pm.getMinAmount() : new BigDecimal("100");
        BigDecimal feeRate = pm != null && pm.getFeeRate() != null ? pm.getFeeRate() : new BigDecimal("0.01");

        if (amount.compareTo(minAmount) < 0) throw new BusinessException("最低提现金额" + minAmount + "元");

        BigDecimal fee = amount.multiply(feeRate);
        BigDecimal actualAmount = amount.subtract(fee);

        String orderNo = "W" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(1000);

        // 使用 BalanceService 安全冻结余额（乐观锁 + 自动写流水）
        balanceService.freezeBalance(userId, amount, orderNo);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType("withdraw");
        order.setAmount(amount);
        order.setFee(fee);
        order.setActualAmount(actualAmount);
        order.setMethod(method);
        order.setMethodName(pm != null ? pm.getName() : method);
        order.setUserAccount(account);
        order.setStatus("pending");
        orderMapper.insert(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("amount", amount);
        data.put("fee", fee);
        data.put("actualAmount", actualAmount);
        data.put("status", "pending");
        return Result.success(data);
    }
}
