package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap")
public class UserExtraController {

    // ===== 卡密充值 =====
    @GetMapping("/card-secret/meta")
    public Result<Map<String, Object>> cardSecretMeta() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", true);
        data.put("minAmount", 100);
        data.put("maxAmount", 50000);
        return Result.success(data);
    }

    @PostMapping("/card-secret/redeem")
    public Result<Map<String, Object>> redeemCardSecret(@RequestBody Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", new BigDecimal("100.00"));
        data.put("message", "卡密兑换成功");
        return Result.success(data);
    }

    // ===== 手动充值 =====
    @PostMapping("/recharge/manual-order")
    public Result<Map<String, Object>> manualRechargeOrder(@RequestBody Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", "MR" + System.currentTimeMillis());
        data.put("amount", params.get("amount"));
        data.put("payAccount", "收款账户信息");
        data.put("qrcode", "/uploads/qrcode/manual.png");
        data.put("status", "pending");
        return Result.success(data);
    }

    @PostMapping("/recharge/declare-paid")
    public Result<Void> declarePaid(@RequestBody Map<String, String> params) {
        return Result.success();
    }

    @GetMapping("/recharge/query")
    public Result<Map<String, Object>> queryOrder(@RequestParam String orderNo) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("status", "pending");
        return Result.success(data);
    }

    // ===== 免费提现 =====
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
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", "FW" + System.currentTimeMillis());
        data.put("status", "pending");
        return Result.success(data);
    }

    // ===== USDT 汇率 =====
    @GetMapping("/usdt/rate")
    public Result<Map<String, Object>> usdtRate() {
        Map<String, Object> data = new HashMap<>();
        data.put("cnyRate", 7.25);
        data.put("usdtRate", 1.0);
        data.put("updateTime", System.currentTimeMillis());
        return Result.success(data);
    }

    // ===== 钱包方式 =====
    @GetMapping("/wallet/method-list")
    public Result<List<Map<String, Object>>> walletMethodList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "name", "USDT-TRC20", "code", "usdt_trc20", "status", 1));
        list.add(Map.of("id", 2, "name", "USDT-ERC20", "code", "usdt_erc20", "status", 1));
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
        return Result.success();
    }

    @PostMapping("/wallet/toggle")
    public Result<Void> toggleWallet(@RequestBody Map<String, Object> params) {
        return Result.success();
    }

    // ===== 消息分类 =====
    @GetMapping("/message/notification-list")
    public Result<Map<String, Object>> notificationMessages(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/message/finance-list")
    public Result<Map<String, Object>> financeMessages(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/message/promo-list")
    public Result<Map<String, Object>> promoMessages(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/message/private-list")
    public Result<Map<String, Object>> privateMessages(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @PostMapping("/message/read-all")
    public Result<Void> readAllMessages() {
        return Result.success();
    }

    @PostMapping("/message/delete-read")
    public Result<Void> deleteReadMessages() {
        return Result.success();
    }

    // ===== 投注奖励 =====
    @GetMapping("/bet-reward/summary")
    public Result<Map<String, Object>> betRewardSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalReward", new BigDecimal("0.00"));
        data.put("availableReward", new BigDecimal("0.00"));
        data.put("claimedReward", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @PostMapping("/bet-reward/claim-all")
    public Result<Void> claimAllBetReward() {
        return Result.success();
    }

    @PostMapping("/bet-reward/claim-available")
    public Result<Map<String, Object>> claimAvailableBetReward() {
        Map<String, Object> data = new HashMap<>();
        data.put("claimedAmount", new BigDecimal("0.00"));
        return Result.success(data);
    }

    // ===== 通用订单创建（兼容前端多种调用） =====
    @PostMapping("/order/create")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", "O" + System.currentTimeMillis());
        data.put("status", "pending");
        return Result.success(data);
    }

    @PostMapping("/order/create-usdt")
    public Result<Map<String, Object>> createUsdtOrder(@RequestBody Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", "USDT" + System.currentTimeMillis());
        data.put("address", "TExxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        data.put("amount", params.get("amount"));
        data.put("status", "pending");
        return Result.success(data);
    }
}
