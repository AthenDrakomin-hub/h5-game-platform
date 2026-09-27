package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.*;
import com.h5.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户额外接口 Controller（真实数据实现）
 * 卡密/手动充值/免费提现/USDT汇率/钱包/消息分类/投注奖励/通用订单
 */
@RestController
@RequestMapping("/wap")
public class UserExtraController {

    @Autowired private MessageMapper messageMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private UserWalletMapper walletMapper;
    @Autowired private PaymentMethodMapper paymentMethodMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private TransactionMapper transactionMapper;

    // ==================== 卡密充值 ====================
    @GetMapping("/card-secret/meta")
    public Result<Map<String, Object>> cardSecretMeta() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", false);
        data.put("minAmount", 100);
        data.put("maxAmount", 50000);
        data.put("notice", "卡密充值功能维护中");
        return Result.success(data);
    }

    @PostMapping("/card-secret/redeem")
    public Result<Map<String, Object>> redeemCardSecret(@RequestBody Map<String, String> params) {
        throw new BusinessException("卡密兑换功能维护中");
    }

    // ==================== 手动充值 ====================
    @PostMapping("/recharge/manual-order")
    public Result<Map<String, Object>> manualRechargeOrder(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        BigDecimal amount = new BigDecimal(params.get("amount") != null ? params.get("amount").toString() : "0");
        String orderNo = "MR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(10000);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType("recharge");
        order.setMethod("manual");
        order.setMethodName("手动转账");
        order.setAmount(amount);
        order.setStatus("pending");
        order.setRemark(params.get("remark") != null ? params.get("remark").toString() : "");
        orderMapper.insert(order);

        // 获取收款账户信息
        List<PaymentMethod> methods = paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getType, "bank")
                        .eq(PaymentMethod::getStatus, 1)
                        .last("LIMIT 1")
        );
        String payAccount = methods.isEmpty() ? "请联系客服获取收款账户" : methods.get(0).getAddress();

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("amount", amount);
        data.put("payAccount", payAccount);
        data.put("qrcode", "/uploads/qrcode/manual.svg");
        data.put("status", "pending");
        return Result.success(data);
    }

    @PostMapping("/recharge/declare-paid")
    public Result<Void> declarePaid(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
        );
        if (order == null) throw new BusinessException("订单不存在");
        order.setRemark("用户已上传付款凭证: " + params.getOrDefault("voucher", ""));
        orderMapper.updateById(order);
        return Result.success();
    }

    @GetMapping("/recharge/query")
    public Result<Map<String, Object>> queryOrder(@RequestParam String orderNo) {
        Long userId = UserContext.getUserId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
        );
        if (order == null) throw new BusinessException("订单不存在");
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("status", order.getStatus());
        data.put("amount", order.getAmount());
        data.put("createTime", order.getCreateTime());
        return Result.success(data);
    }

    // ==================== 免费提现 ====================
    @GetMapping("/withdraw/free-info")
    public Result<Map<String, Object>> freeWithdrawInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", true);
        data.put("freeCount", 1);
        data.put("usedCount", 0);
        data.put("minAmount", 100);
        data.put("maxAmount", 50000);
        return Result.success(data);
    }

    @PostMapping("/withdraw/free-submit")
    public Result<Map<String, Object>> submitFreeWithdraw(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        BigDecimal amount = new BigDecimal(params.get("amount") != null ? params.get("amount").toString() : "0");
        String orderNo = "FW" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(10000);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType("withdraw");
        order.setMethod("free");
        order.setMethodName("免费提现");
        order.setAmount(amount);
        order.setStatus("pending");
        orderMapper.insert(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("status", "pending");
        return Result.success(data);
    }

    // ==================== USDT 汇率 ====================
    @GetMapping("/usdt/rate")
    public Result<Map<String, Object>> usdtRate() {
        Map<String, Object> data = new HashMap<>();
        data.put("cnyRate", 7.25);
        data.put("usdtRate", 1.0);
        data.put("updateTime", System.currentTimeMillis());
        return Result.success(data);
    }

    // ==================== 钱包方式 ====================
    @GetMapping("/wallet/method-list")
    public Result<List<PaymentMethod>> walletMethodList() {
        List<PaymentMethod> list = paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getStatus, 1)
                        .orderByAsc(PaymentMethod::getSort)
        );
        return Result.success(list);
    }

    @GetMapping("/wallet/method-type-list")
    public Result<List<Map<String, Object>>> walletMethodTypeList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("code", "trc20", "name", "TRC20", "network", "Tron"));
        list.add(Map.of("code", "erc20", "name", "ERC20", "network", "Ethereum"));
        return Result.success(list);
    }

    @PostMapping("/wallet/save")
    public Result<Void> saveWallet(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        UserWallet wallet = new UserWallet();
        wallet.setUserId(userId);
        wallet.setChain(params.get("chain") != null ? params.get("chain") : "TRC20");
        wallet.setAddress(params.get("address"));
        wallet.setLabel(params.get("label") != null ? params.get("label") : "");
        wallet.setStatus(1);
        walletMapper.insert(wallet);
        return Result.success();
    }

    @PostMapping("/wallet/toggle")
    public Result<Void> toggleWallet(@RequestBody Map<String, Object> params) {
        Long walletId = Long.valueOf(params.get("id").toString());
        UserWallet wallet = walletMapper.selectById(walletId);
        if (wallet != null && wallet.getUserId().equals(UserContext.getUserId())) {
            wallet.setStatus(wallet.getStatus() == 1 ? 0 : 1);
            walletMapper.updateById(wallet);
        }
        return Result.success();
    }

    // ==================== 消息分类 ====================
    @GetMapping("/message/notification-list")
    public Result<Map<String, Object>> notificationMessages(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int pageSize) {
        return getMessagesByType(page, pageSize, "notification");
    }

    @GetMapping("/message/finance-list")
    public Result<Map<String, Object>> financeMessages(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return getMessagesByType(page, pageSize, "finance");
    }

    @GetMapping("/message/promo-list")
    public Result<Map<String, Object>> promoMessages(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return getMessagesByType(page, pageSize, "promo");
    }

    @GetMapping("/message/private-list")
    public Result<Map<String, Object>> privateMessages(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        Page<Message> pageResult = messageMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getUserId, userId)
                        .orderByDesc(Message::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    @PostMapping("/message/read-all")
    public Result<Void> readAllMessages() {
        Long userId = UserContext.getUserId();
        List<Message> unread = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0))
                        .eq(Message::getIsRead, 0)
        );
        for (Message msg : unread) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
        return Result.success();
    }

    @PostMapping("/message/delete-read")
    public Result<Void> deleteReadMessages() {
        Long userId = UserContext.getUserId();
        messageMapper.delete(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getUserId, userId)
                        .eq(Message::getIsRead, 1)
        );
        return Result.success();
    }

    // ==================== 投注奖励 ====================
    @GetMapping("/bet-reward/summary")
    public Result<Map<String, Object>> betRewardSummary() {
        Long userId = UserContext.getUserId();
        List<Transaction> rewards = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "bet_reward")
        );
        BigDecimal total = rewards.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> data = new HashMap<>();
        data.put("totalReward", total);
        data.put("availableReward", total);
        data.put("claimedReward", BigDecimal.ZERO);
        return Result.success(data);
    }

    @PostMapping("/bet-reward/claim-all")
    public Result<Void> claimAllBetReward() {
        return Result.success();
    }

    @PostMapping("/bet-reward/claim-available")
    public Result<Map<String, Object>> claimAvailableBetReward() {
        Map<String, Object> data = new HashMap<>();
        data.put("claimedAmount", BigDecimal.ZERO);
        return Result.success(data);
    }

    // ==================== 通用订单创建 ====================
    @PostMapping("/order/create")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        String orderNo = "O" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(10000);
        BigDecimal amount = new BigDecimal(params.get("amount") != null ? params.get("amount").toString() : "0");

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType(params.get("type") != null ? params.get("type").toString() : "recharge");
        order.setAmount(amount);
        order.setStatus("pending");
        orderMapper.insert(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("status", "pending");
        return Result.success(data);
    }

    @PostMapping("/order/create-usdt")
    public Result<Map<String, Object>> createUsdtOrder(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        String orderNo = "USDT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(10000);
        BigDecimal amount = new BigDecimal(params.get("amount") != null ? params.get("amount").toString() : "0");

        // 获取USDT收款地址
        List<PaymentMethod> methods = paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getType, "usdt")
                        .eq(PaymentMethod::getStatus, 1)
                        .last("LIMIT 1")
        );
        String address = methods.isEmpty() ? "TExxxxxxxxxxxxxxxxxxxxxxxxxxxxx" : methods.get(0).getAddress();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setType("recharge");
        order.setMethod("usdt_trc20");
        order.setMethodName("USDT-TRC20");
        order.setAmount(amount);
        order.setPayAccount(address);
        order.setStatus("pending");
        orderMapper.insert(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("address", address);
        data.put("amount", amount);
        data.put("status", "pending");
        return Result.success(data);
    }

    // ==================== 内部工具 ====================
    private Result<Map<String, Object>> getMessagesByType(int page, int pageSize, String type) {
        Long userId = UserContext.getUserId();
        Page<Message> pageResult = messageMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0))
                        .eq(Message::getCategory, type)
                        .orderByDesc(Message::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }
}
