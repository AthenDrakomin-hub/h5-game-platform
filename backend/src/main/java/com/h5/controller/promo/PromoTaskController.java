package com.h5.controller.promo;

import com.h5.common.Result;
import com.h5.common.UserContext;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap/promo")
public class PromoTaskController {

    @GetMapping("/float-activities")
    public Result<List<Map<String, Object>>> floatActivities() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "name", "新人专享", "icon", "/uploads/icons/float1.png", "link", "/promo/detail/1"));
        return Result.success(list);
    }

    @GetMapping("/float-event")
    public Result<Map<String, Object>> floatEvent() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("title", "限时活动");
        data.put("image", "/uploads/promo/float_event.png");
        data.put("link", "/promo/detail/1");
        data.put("status", 1);
        return Result.success(data);
    }

    @GetMapping("/reward/list")
    public Result<Map<String, Object>> rewardList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/reward/summary")
    public Result<Map<String, Object>> rewardSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalReward", new BigDecimal("0.00"));
        data.put("availableReward", new BigDecimal("0.00"));
        data.put("claimedReward", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @PostMapping("/recharge-reward/preview")
    public Result<Map<String, Object>> rechargeRewardPreview(@RequestBody Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", params.get("amount"));
        data.put("reward", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/recharge-reward/summary")
    public Result<Map<String, Object>> rechargeRewardSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRecharge", new BigDecimal("0.00"));
        data.put("totalReward", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/register-bonus/summary")
    public Result<Map<String, Object>> registerBonusSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("claimed", false);
        data.put("amount", new BigDecimal("88.00"));
        return Result.success(data);
    }

    @GetMapping("/task/list")
    public Result<List<Map<String, Object>>> taskList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "name", "每日签到", "type", "daily", "reward", "10", "progress", 0, "target", 1, "completed", false));
        list.add(Map.of("id", 2, "name", "首次充值", "type", "recharge", "reward", "50", "progress", 0, "target", 1, "completed", false));
        list.add(Map.of("id", 3, "name", "邀请好友", "type", "invite", "reward", "20", "progress", 0, "target", 1, "completed", false));
        return Result.success(list);
    }

    @GetMapping("/signin/info")
    public Result<Map<String, Object>> signinInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("signedToday", false);
        data.put("continuousDays", 0);
        data.put("totalDays", 0);
        List<Map<String, Object>> days = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            days.add(Map.of("day", i, "reward", i * 5, "signed", false));
        }
        data.put("days", days);
        return Result.success(data);
    }

    @PostMapping("/signin")
    public Result<Map<String, Object>> signin() {
        Map<String, Object> data = new HashMap<>();
        data.put("reward", new BigDecimal("10.00"));
        data.put("continuousDays", 1);
        return Result.success(data);
    }

    @GetMapping("/lucky-wheel/info")
    public Result<Map<String, Object>> luckyWheelInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("freeChances", 1);
        data.put("paidChances", 0);
        List<Map<String, Object>> prizes = new ArrayList<>();
        prizes.add(Map.of("id", 1, "name", "10元彩金", "type", "balance", "value", 10, "probability", 0.3));
        prizes.add(Map.of("id", 2, "name", "50元彩金", "type", "balance", "value", 50, "probability", 0.1));
        prizes.add(Map.of("id", 3, "name", "谢谢参与", "type", "none", "value", 0, "probability", 0.5));
        data.put("prizes", prizes);
        return Result.success(data);
    }

    @PostMapping("/lucky-wheel/spin")
    public Result<Map<String, Object>> luckyWheelSpin() {
        Map<String, Object> data = new HashMap<>();
        data.put("prizeId", 1);
        data.put("prizeName", "10元彩金");
        data.put("prizeValue", new BigDecimal("10.00"));
        return Result.success(data);
    }

    @GetMapping("/loss-rescue/summary")
    public Result<Map<String, Object>> lossRescueSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("weekLoss", new BigDecimal("0.00"));
        data.put("availableRescue", new BigDecimal("0.00"));
        data.put("claimedRescue", new BigDecimal("0.00"));
        data.put("rescueRate", 0.05);
        return Result.success(data);
    }

    @PostMapping("/loss-rescue/claim")
    public Result<Void> lossRescueClaim() {
        return Result.success();
    }

    @GetMapping("/invite/info")
    public Result<Map<String, Object>> inviteInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", "INV" + UserContext.getUserId());
        data.put("inviteLink", "https://h5.lexiangim.com/register?invite=INV" + UserContext.getUserId());
        data.put("qrCode", "/uploads/qrcode/invite_" + UserContext.getUserId() + ".png");
        data.put("totalInvited", 0);
        data.put("totalReward", new BigDecimal("0.00"));
        return Result.success(data);
    }
}
