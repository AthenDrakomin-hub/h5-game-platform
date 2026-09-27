package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/wap/payment")
public class PaymentController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private UserMapper userMapper;

    /**
     * 充值方式列表
     * GET /api/wap/payment-methods/recharge-methods
     */
    @GetMapping("/recharge-methods")
    public Result<Map<String, Object>> rechargeMethods() {
        List<Map<String, Object>> cnyMethods = new ArrayList<>();
        cnyMethods.add(Map.of("id", 1, "name", "支付宝", "code", "alipay", "icon", "/uploads/icons/alipay.png", "minAmount", 100, "maxAmount", 50000, "status", 1));
        cnyMethods.add(Map.of("id", 2, "name", "微信支付", "code", "wechat", "icon", "/uploads/icons/wechat.png", "minAmount", 100, "maxAmount", 50000, "status", 1));
        cnyMethods.add(Map.of("id", 3, "name", "银行卡转账", "code", "bank", "icon", "/uploads/icons/bank.png", "minAmount", 100, "maxAmount", 50000, "status", 1));

        List<Map<String, Object>> cryptoMethods = new ArrayList<>();
        cryptoMethods.add(Map.of("id", 11, "name", "USDT-TRC20", "code", "usdt_trc20", "icon", "/uploads/icons/usdt.png", "minAmount", 100, "maxAmount", 500000, "status", 1, "address", "TExxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
        cryptoMethods.add(Map.of("id", 12, "name", "USDT-ERC20", "code", "usdt_erc20", "icon", "/uploads/icons/usdt.png", "minAmount", 100, "maxAmount", 500000, "status", 1, "address", "0xExxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));

        Map<String, Object> data = new HashMap<>();
        data.put("cnyMethods", cnyMethods);
        data.put("cryptoMethods", cryptoMethods);
        return Result.success(data);
    }

    /**
     * 提现方式列表
     * GET /api/wap/payment-methods/withdraw-methods
     */
    @GetMapping("/withdraw-methods")
    public Result<Map<String, Object>> withdrawMethods() {
        List<Map<String, Object>> cnyMethods = new ArrayList<>();
        cnyMethods.add(Map.of("id", 1, "name", "银行卡提现", "code", "bank", "icon", "/uploads/icons/bank.png", "minAmount", 100, "maxAmount", 50000, "feeRate", 0.01, "status", 1));

        List<Map<String, Object>> cryptoMethods = new ArrayList<>();
        cryptoMethods.add(Map.of("id", 11, "name", "USDT-TRC20", "code", "usdt_trc20", "icon", "/uploads/icons/usdt.png", "minAmount", 100, "maxAmount", 500000, "feeRate", 0.005, "status", 1));

        Map<String, Object> data = new HashMap<>();
        data.put("cnyMethods", cnyMethods);
        data.put("cryptoMethods", cryptoMethods);
        data.put("withdrawCnyEnabled", 1);
        data.put("withdrawCryptoEnabled", 1);
        return Result.success(data);
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
        if (amount.compareTo(new BigDecimal("100")) < 0) throw new BusinessException("最低充值金额100元");

        String orderNo = "R" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(1000);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType("recharge");
        order.setAmount(amount);
        order.setFee(BigDecimal.ZERO);
        order.setActualAmount(amount);
        order.setMethod(method);
        order.setMethodName(getMethodName(method));
        order.setStatus("pending");
        order.setPayAccount("收款账户_" + method);
        order.setPayQrcode("/uploads/qrcode/" + method + ".png");
        orderMapper.insert(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("amount", amount);
        data.put("method", method);
        data.put("payAccount", order.getPayAccount());
        data.put("payQrcode", order.getPayQrcode());
        data.put("status", "pending");
        return Result.success(data);
    }

    /**
     * 创建提现申请
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

        if (amount.compareTo(new BigDecimal("100")) < 0) throw new BusinessException("最低提现金额100元");
        if (amount.compareTo(user.getBalance()) > 0) throw new BusinessException("余额不足");

        BigDecimal fee = amount.multiply(new BigDecimal("0.01"));
        BigDecimal actualAmount = amount.subtract(fee);

        String orderNo = "W" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(1000);

        // 冻结余额
        user.setBalance(user.getBalance().subtract(amount));
        user.setFrozenBalance(user.getFrozenBalance().add(amount));
        userMapper.updateById(user);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType("withdraw");
        order.setAmount(amount);
        order.setFee(fee);
        order.setActualAmount(actualAmount);
        order.setMethod(method);
        order.setMethodName(getMethodName(method));
        order.setUserAccount(account);
        order.setStatus("pending");
        orderMapper.insert(order);

        // 交易流水
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setType("withdraw");
        tx.setAmount(amount.negate());
        tx.setBalanceBefore(user.getBalance().add(amount));
        tx.setBalanceAfter(user.getBalance());
        tx.setRefId(order.getId());
        tx.setRefNo(orderNo);
        tx.setDescription("提现申请冻结");
        transactionMapper.insert(tx);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("amount", amount);
        data.put("fee", fee);
        data.put("actualAmount", actualAmount);
        data.put("status", "pending");
        return Result.success(data);
    }

    private String getMethodName(String method) {
        Map<String, String> map = Map.of("alipay", "支付宝", "wechat", "微信支付", "bank", "银行卡", "usdt_trc20", "USDT-TRC20", "usdt_erc20", "USDT-ERC20");
        return map.getOrDefault(method, method);
    }
}
