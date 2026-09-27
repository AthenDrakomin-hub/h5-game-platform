package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Order;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 代理中心 Controller（真实数据实现）
 */
@RestController
@RequestMapping("/wap/agent")
public class AgentController {

    @Autowired private UserMapper userMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private TransactionMapper transactionMapper;

    @Value("${site.domain:https://h5.goodspage.cn}")
    private String siteDomain;

    /** 代理总览 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);

        List<User> members = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getInvitedBy, userId)
        );
        List<Long> memberIds = members.stream().map(User::getId).collect(Collectors.toList());

        BigDecimal totalRecharge = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        if (!memberIds.isEmpty()) {
            List<Order> rechargeOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .in(Order::getUserId, memberIds)
                            .eq(Order::getType, "recharge")
                            .eq(Order::getStatus, "success")
            );
            totalRecharge = rechargeOrders.stream()
                    .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Transaction> commissions = transactionMapper.selectList(
                    new LambdaQueryWrapper<Transaction>()
                            .eq(Transaction::getUserId, userId)
                            .eq(Transaction::getType, "commission")
            );
            totalCommission = commissions.stream()
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", user != null ? user.getInviteCode() : "INV" + userId);
        data.put("inviteLink", siteDomain + "/register?invite=" + (user != null ? user.getInviteCode() : "INV" + userId));
        data.put("totalMembers", members.size());
        data.put("activeMembers", (int) members.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count());
        data.put("totalRecharge", totalRecharge);
        data.put("totalCommission", totalCommission);
        data.put("availableCommission", totalCommission);
        return Result.success(data);
    }

    /** 代理统计（今日/本周/本月） */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = UserContext.getUserId();
        List<User> members = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getInvitedBy, userId)
        );
        List<Long> memberIds = members.stream().map(User::getId).collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.toLocalDate().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        Map<String, Object> data = new HashMap<>();
        data.put("todayMembers", countMembersAfter(members, todayStart));
        data.put("weekMembers", countMembersAfter(members, weekStart));
        data.put("monthMembers", countMembersAfter(members, monthStart));

        if (!memberIds.isEmpty()) {
            data.put("todayRecharge", sumRechargeAfter(memberIds, todayStart));
            data.put("weekRecharge", sumRechargeAfter(memberIds, weekStart));
            data.put("monthRecharge", sumRechargeAfter(memberIds, monthStart));
        } else {
            data.put("todayRecharge", BigDecimal.ZERO);
            data.put("weekRecharge", BigDecimal.ZERO);
            data.put("monthRecharge", BigDecimal.ZERO);
        }
        data.put("todayCommission", BigDecimal.ZERO);
        return Result.success(data);
    }

    /** 团队成员列表 */
    @GetMapping("/members")
    public Result<Map<String, Object>> members(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        Page<User> pageResult = userMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<User>()
                        .eq(User::getInvitedBy, userId)
                        .orderByDesc(User::getCreateTime)
        );

        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : pageResult.getRecords()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("vipLevel", u.getVipLevel());
            m.put("balance", u.getBalance());
            m.put("status", u.getStatus());
            m.put("createTime", u.getCreateTime());
            list.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /** 佣金记录 */
    @GetMapping("/records")
    public Result<Map<String, Object>> records(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        Page<Transaction> pageResult = transactionMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "commission")
                        .orderByDesc(Transaction::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    /** 成员统计 */
    @GetMapping("/member-stats")
    public Result<Map<String, Object>> memberStats() {
        Long userId = UserContext.getUserId();
        List<User> members = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getInvitedBy, userId)
        );
        long active = members.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count();
        long todayNew = members.stream()
                .filter(m -> m.getCreateTime() != null && m.getCreateTime().toLocalDate().equals(LocalDate.now()))
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("total", members.size());
        data.put("active", active);
        data.put("inactive", members.size() - active);
        data.put("todayNew", todayNew);
        return Result.success(data);
    }

    /** 推广看板 */
    @GetMapping("/promote-dashboard")
    public Result<Map<String, Object>> promoteDashboard() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        List<User> members = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getInvitedBy, userId)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", user != null ? user.getInviteCode() : "INV" + userId);
        data.put("inviteLink", siteDomain + "/register?invite=" + (user != null ? user.getInviteCode() : "INV" + userId));
        data.put("qrCode", "/uploads/qrcode/invite_" + userId + ".svg");
        data.put("totalClicks", 0);
        data.put("totalRegisters", members.size());
        data.put("conversionRate", members.size() > 0 ? "100%" : "0%");
        return Result.success(data);
    }

    /** 工作台佣金统计 */
    @GetMapping("/workbench-stats")
    public Result<Map<String, Object>> workbenchStats() {
        Long userId = UserContext.getUserId();
        List<Transaction> commissions = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "commission")
        );
        BigDecimal total = commissions.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal today = sumTxAfter(commissions, todayStart);
        BigDecimal week = sumTxAfter(commissions, weekStart);
        BigDecimal month = sumTxAfter(commissions, monthStart);

        Map<String, Object> data = new HashMap<>();
        data.put("todayCommission", today);
        data.put("weekCommission", week);
        data.put("monthCommission", month);
        data.put("totalCommission", total);
        return Result.success(data);
    }

    /** 返佣比例 */
    @GetMapping("/rebate-ratio")
    public Result<Map<String, Object>> rebateRatio() {
        Map<String, Object> data = new HashMap<>();
        data.put("currentRatio", 0.01);
        data.put("level", 1);
        data.put("nextLevelRatio", 0.02);
        data.put("nextLevelRecharge", 10000);
        return Result.success(data);
    }

    /** 代理等级列表 */
    @GetMapping("/rate-scope")
    public Result<List<Map<String, Object>>> rateScope() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("level", 1, "ratio", 0.01, "minRecharge", 0));
        list.add(Map.of("level", 2, "ratio", 0.02, "minRecharge", 10000));
        list.add(Map.of("level", 3, "ratio", 0.03, "minRecharge", 50000));
        return Result.success(list);
    }

    /** 亏损救助汇总 */
    @GetMapping("/loss-summary")
    public Result<Map<String, Object>> lossSummary() {
        Long userId = UserContext.getUserId();
        List<Transaction> bets = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "bet")
        );
        BigDecimal totalLoss = bets.stream()
                .map(t -> t.getAmount() != null ? t.getAmount().abs() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("totalLoss", totalLoss);
        data.put("availableRescue", totalLoss.multiply(new BigDecimal("0.05")));
        data.put("claimedRescue", BigDecimal.ZERO);
        return Result.success(data);
    }

    /** 亏损救助记录 */
    @GetMapping("/loss-records")
    public Result<Map<String, Object>> lossRecords(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        Page<Transaction> pageResult = transactionMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "loss_rescue")
                        .orderByDesc(Transaction::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    /** 佣金提现 */
    @PostMapping("/withdraw")
    public Result<Void> withdraw(@RequestBody Map<String, Object> params) {
        return Result.success();
    }

    /** 创建成员（代理代注册） */
    @PostMapping("/create-member")
    public Result<Map<String, Object>> createMember(@RequestBody Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", params.get("username"));
        data.put("password", params.get("password"));
        return Result.success(data);
    }

    // ==================== 内部工具 ====================
    private long countMembersAfter(List<User> members, LocalDateTime time) {
        return members.stream()
                .filter(m -> m.getCreateTime() != null && m.getCreateTime().isAfter(time))
                .count();
    }

    private BigDecimal sumRechargeAfter(List<Long> memberIds, LocalDateTime time) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .in(Order::getUserId, memberIds)
                        .eq(Order::getType, "recharge")
                        .eq(Order::getStatus, "success")
                        .ge(Order::getCreateTime, time)
        );
        return orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumTxAfter(List<Transaction> list, LocalDateTime time) {
        return list.stream()
                .filter(t -> t.getCreateTime() != null && t.getCreateTime().isAfter(time))
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
