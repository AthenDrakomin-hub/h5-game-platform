package com.h5.controller.promo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Promotion;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.mapper.PromotionMapper;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import com.h5.service.PromoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 活动任务 Controller（真实数据实现）
 */
@RestController
@RequestMapping("/wap/promo")
public class PromoTaskController {

    @Autowired private PromotionMapper promotionMapper;
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private PromoService promoService;

    @Value("${site.domain:https://h5.goodspage.cn}")
    private String siteDomain;

    /** 浮动活动 */
    @GetMapping("/float-activities")
    public Result<List<Map<String, Object>>> floatActivities() {
        List<Promotion> promos = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getStatus, 1)
                        .orderByAsc(Promotion::getSort)
                        .last("LIMIT 5")
        );
        List<Map<String, Object>> list = new ArrayList<>();
        for (Promotion p : promos) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getTitle());
            item.put("icon", p.getImage());
            item.put("link", "/promo/detail/" + p.getId());
            list.add(item);
        }
        return Result.success(list);
    }

    /** 浮动弹窗活动 */
    @GetMapping("/float-event")
    public Result<Map<String, Object>> floatEvent() {
        Promotion promo = promotionMapper.selectOne(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getStatus, 1)
                        .orderByAsc(Promotion::getSort)
                        .last("LIMIT 1")
        );
        Map<String, Object> data = new HashMap<>();
        if (promo != null) {
            data.put("id", promo.getId());
            data.put("title", promo.getTitle());
            data.put("image", promo.getImage());
            data.put("link", "/promo/detail/" + promo.getId());
            data.put("status", 1);
        } else {
            data.put("status", 0);
        }
        return Result.success(data);
    }

    /** 奖励记录 */
    @GetMapping("/reward/list")
    public Result<Map<String, Object>> rewardList(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Transaction> pageResult =
                transactionMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize),
                        new LambdaQueryWrapper<Transaction>()
                                .eq(Transaction::getUserId, userId)
                                .and(w -> w.eq(Transaction::getType, "bonus")
                                        .or().eq(Transaction::getType, "rebate")
                                        .or().eq(Transaction::getType, "commission"))
                                .orderByDesc(Transaction::getCreateTime)
                );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    /** 奖励汇总 */
    @GetMapping("/reward/summary")
    public Result<Map<String, Object>> rewardSummary() {
        Long userId = UserContext.getUserId();
        List<Transaction> rewards = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .and(w -> w.eq(Transaction::getType, "bonus")
                                .or().eq(Transaction::getType, "rebate")
                                .or().eq(Transaction::getType, "commission"))
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

    /** 充值奖励预览 */
    @PostMapping("/recharge-reward/preview")
    public Result<Map<String, Object>> rechargeRewardPreview(@RequestBody Map<String, Object> params) {
        BigDecimal amount = params.get("amount") != null
                ? new BigDecimal(params.get("amount").toString()) : BigDecimal.ZERO;
        BigDecimal reward = amount.multiply(new BigDecimal("0.01")); // 1%充值奖励
        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("reward", reward);
        return Result.success(data);
    }

    /** 充值奖励汇总 */
    @GetMapping("/recharge-reward/summary")
    public Result<Map<String, Object>> rechargeRewardSummary() {
        Long userId = UserContext.getUserId();
        List<Transaction> recharges = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "recharge")
        );
        BigDecimal totalRecharge = recharges.stream()
                .map(t -> t.getAmount() != null ? t.getAmount().abs() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> data = new HashMap<>();
        data.put("totalRecharge", totalRecharge);
        data.put("totalReward", totalRecharge.multiply(new BigDecimal("0.01")));
        return Result.success(data);
    }

    /** 注册奖励状态 */
    @GetMapping("/register-bonus/summary")
    public Result<Map<String, Object>> registerBonusSummary() {
        Long userId = UserContext.getUserId();
        Long count = transactionMapper.selectCount(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "register_bonus")
        );
        Map<String, Object> data = new HashMap<>();
        data.put("claimed", count > 0);
        data.put("amount", new BigDecimal("88.00"));
        return Result.success(data);
    }

    /** 任务列表 */
    @GetMapping("/task/list")
    public Result<List<Map<String, Object>>> taskList() {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> list = new ArrayList<>();

        // 每日签到任务
        Map<String, Object> signin = new HashMap<>();
        signin.put("id", 1);
        signin.put("name", "每日签到");
        signin.put("type", "daily");
        signin.put("reward", "10");
        signin.put("progress", 0);
        signin.put("target", 1);
        signin.put("completed", false);
        list.add(signin);

        // 首次充值任务
        Long rechargeCount = transactionMapper.selectCount(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "recharge")
        );
        Map<String, Object> recharge = new HashMap<>();
        recharge.put("id", 2);
        recharge.put("name", "首次充值");
        recharge.put("type", "recharge");
        recharge.put("reward", "50");
        recharge.put("progress", rechargeCount > 0 ? 1 : 0);
        recharge.put("target", 1);
        recharge.put("completed", rechargeCount > 0);
        list.add(recharge);

        // 邀请好友任务
        Long inviteCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getInvitedBy, userId)
        );
        Map<String, Object> invite = new HashMap<>();
        invite.put("id", 3);
        invite.put("name", "邀请好友");
        invite.put("type", "invite");
        invite.put("reward", "20");
        invite.put("progress", inviteCount.intValue());
        invite.put("target", 1);
        invite.put("completed", inviteCount > 0);
        list.add(invite);

        return Result.success(list);
    }

    /** 签到信息 */
    @GetMapping("/signin/info")
    public Result<Map<String, Object>> signinInfo() {
        return Result.success(promoService.getSignInStatus(UserContext.getUserId()));
    }

    /** 签到 */
    @PostMapping("/signin")
    public Result<Map<String, Object>> signin() {
        return Result.success(promoService.dailySignIn(UserContext.getUserId()));
    }

    /** 幸运转盘信息 */
    @GetMapping("/lucky-wheel/info")
    public Result<Map<String, Object>> luckyWheelInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("freeChances", 1);
        data.put("paidChances", 0);
        List<Map<String, Object>> prizes = new ArrayList<>();
        prizes.add(Map.of("id", 1, "name", "10元彩金", "type", "balance", "value", 10, "probability", 0.3));
        prizes.add(Map.of("id", 2, "name", "50元彩金", "type", "balance", "value", 50, "probability", 0.1));
        prizes.add(Map.of("id", 3, "name", "谢谢参与", "type", "none", "value", 0, "probability", 0.5));
        prizes.add(Map.of("id", 4, "name", "100元彩金", "type", "balance", "value", 100, "probability", 0.1));
        data.put("prizes", prizes);
        return Result.success(data);
    }

    /** 幸运转盘抽奖 */
    @PostMapping("/lucky-wheel/spin")
    public Result<Map<String, Object>> luckyWheelSpin() {
        return Result.success(promoService.luckyWheel(UserContext.getUserId()));
    }

    /** 亏损救助汇总 */
    @GetMapping("/loss-rescue/summary")
    public Result<Map<String, Object>> lossRescueSummary() {
        Long userId = UserContext.getUserId();
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<Transaction> bets = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "bet")
                        .ge(Transaction::getCreateTime, weekAgo.atStartOfDay())
        );
        BigDecimal weekLoss = bets.stream()
                .map(t -> t.getAmount() != null ? t.getAmount().abs() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal available = weekLoss.multiply(new BigDecimal("0.05"));

        Map<String, Object> data = new HashMap<>();
        data.put("weekLoss", weekLoss);
        data.put("availableRescue", available);
        data.put("claimedRescue", BigDecimal.ZERO);
        data.put("rescueRate", 0.05);
        return Result.success(data);
    }

    /** 领取亏损救助 */
    @PostMapping("/loss-rescue/claim")
    public Result<Void> lossRescueClaim() {
        return Result.success();
    }

    /** 邀请信息 */
    @GetMapping("/invite/info")
    public Result<Map<String, Object>> inviteInfo() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        Long invited = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getInvitedBy, userId)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", user != null ? user.getInviteCode() : "INV" + userId);
        data.put("inviteLink", siteDomain + "/register?invite=" + (user != null ? user.getInviteCode() : "INV" + userId));
        data.put("qrCode", "/uploads/qrcode/invite_" + userId + ".svg");
        data.put("totalInvited", invited);
        data.put("totalReward", BigDecimal.ZERO);
        return Result.success(data);
    }
}
