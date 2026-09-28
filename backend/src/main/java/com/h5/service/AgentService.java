package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.entity.Order;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 代理服务（代理中心/团队/返佣/亏损救助）
 * 所有代理相关业务逻辑集中在此，Controller只做参数接收和返回
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    @Autowired private UserMapper userMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private TransactionMapper transactionMapper;

    @Value("${site.domain:https://h5.goodspage.cn}")
    private String siteDomain;

    // ==================== 代理总览 ====================

    public Map<String, Object> getOverview(Long userId) {
        User user = userMapper.selectById(userId);
        List<User> members = getMemberList(userId);
        List<Long> memberIds = members.stream().map(User::getId).collect(Collectors.toList());

        BigDecimal totalRecharge = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        if (!memberIds.isEmpty()) {
            totalRecharge = sumRecharge(memberIds, null);
            totalCommission = sumCommission(userId, null);
        }

        Map<String, Object> data = new HashMap<>();
        String inviteCode = user != null ? user.getInviteCode() : "INV" + userId;
        data.put("inviteCode", inviteCode);
        data.put("inviteLink", siteDomain + "/register?invite=" + inviteCode);
        data.put("totalMembers", members.size());
        data.put("activeMembers", (int) members.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count());
        data.put("totalRecharge", totalRecharge);
        data.put("totalCommission", totalCommission);
        data.put("availableCommission", totalCommission);
        return data;
    }

    // ==================== 代理统计 ====================

    public Map<String, Object> getStats(Long userId) {
        List<User> members = getMemberList(userId);
        List<Long> memberIds = members.stream().map(User::getId).collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.toLocalDate().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        Map<String, Object> data = new HashMap<>();
        data.put("todayMembers", countMembersAfter(members, todayStart));
        data.put("weekMembers", countMembersAfter(members, weekStart));
        data.put("monthMembers", countMembersAfter(members, monthStart));
        data.put("todayRecharge", memberIds.isEmpty() ? BigDecimal.ZERO : sumRecharge(memberIds, todayStart));
        data.put("weekRecharge", memberIds.isEmpty() ? BigDecimal.ZERO : sumRecharge(memberIds, weekStart));
        data.put("monthRecharge", memberIds.isEmpty() ? BigDecimal.ZERO : sumRecharge(memberIds, monthStart));
        data.put("todayCommission", BigDecimal.ZERO);
        return data;
    }

    // ==================== 团队成员 ====================

    public Map<String, Object> getTeamMembers(Long userId, int page, int pageSize) {
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
        return data;
    }

    public Map<String, Object> getMemberStats(Long userId) {
        List<User> members = getMemberList(userId);
        long active = members.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count();
        long todayNew = members.stream()
                .filter(m -> m.getCreateTime() != null && m.getCreateTime().toLocalDate().equals(LocalDate.now()))
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("total", members.size());
        data.put("active", active);
        data.put("inactive", members.size() - active);
        data.put("todayNew", todayNew);
        return data;
    }

    // ==================== 佣金记录 ====================

    public Map<String, Object> getCommissionRecords(Long userId, int page, int pageSize) {
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
        return data;
    }

    public Map<String, Object> getWorkbenchStats(Long userId) {
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

        Map<String, Object> data = new HashMap<>();
        data.put("todayCommission", sumTxAfter(commissions, todayStart));
        data.put("weekCommission", sumTxAfter(commissions, weekStart));
        data.put("monthCommission", sumTxAfter(commissions, monthStart));
        data.put("totalCommission", total);
        return data;
    }

    // ==================== 推广看板 ====================

    public Map<String, Object> getPromoteDashboard(Long userId) {
        User user = userMapper.selectById(userId);
        List<User> members = getMemberList(userId);
        String inviteCode = user != null ? user.getInviteCode() : "INV" + userId;

        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", inviteCode);
        data.put("inviteLink", siteDomain + "/register?invite=" + inviteCode);
        data.put("qrCode", "/uploads/qrcode/invite_" + userId + ".svg");
        data.put("totalClicks", 0);
        data.put("totalRegisters", members.size());
        data.put("conversionRate", members.size() > 0 ? "100%" : "0%");
        return data;
    }

    public Map<String, Object> getPromoteLink(Long userId, String domain) {
        User user = userMapper.selectById(userId);
        if (user == null) return Collections.emptyMap();
        String inviteCode = user.getInviteCode();

        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", inviteCode);
        data.put("registerLink", domain + "/register?invite=" + inviteCode);
        data.put("telegramLink", "https://t.me/share/url?url=" + domain + "/register?invite=" + inviteCode);
        return data;
    }

    // ==================== 返佣比例 ====================

    public Map<String, Object> getRebateRatio() {
        Map<String, Object> data = new HashMap<>();
        data.put("currentRatio", 0.01);
        data.put("level", 1);
        data.put("nextLevelRatio", 0.02);
        data.put("nextLevelRecharge", 10000);
        return data;
    }

    public List<Map<String, Object>> getRateScope() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("level", 1, "ratio", 0.01, "minRecharge", 0));
        list.add(Map.of("level", 2, "ratio", 0.02, "minRecharge", 10000));
        list.add(Map.of("level", 3, "ratio", 0.03, "minRecharge", 50000));
        return list;
    }

    // ==================== 亏损救助 ====================

    public Map<String, Object> getLossSummary(Long userId) {
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
        return data;
    }

    public Map<String, Object> getLossRecords(Long userId, int page, int pageSize) {
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
        return data;
    }

    // ==================== 返水 ====================

    public Map<String, Object> getRebateOverview(Long userId) {
        List<Transaction> rebates = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "rebate")
        );
        BigDecimal total = rebates.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();

        Map<String, Object> data = new HashMap<>();
        data.put("totalRebate", total);
        data.put("availableRebate", total);
        data.put("claimedRebate", BigDecimal.ZERO);
        data.put("todayRebate", sumTxAfter(rebates, todayStart));
        data.put("weekRebate", sumTxAfter(rebates, weekStart));
        return data;
    }

    public List<Map<String, Object>> getRebateLadders() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("level", 1, "ratio", 0.005, "minAmount", 0, "maxAmount", 10000));
        list.add(Map.of("level", 2, "ratio", 0.008, "minAmount", 10000, "maxAmount", 50000));
        list.add(Map.of("level", 3, "ratio", 0.01, "minAmount", 50000, "maxAmount", 200000));
        return list;
    }

    public Map<String, Object> getRebateRecords(Long userId, int page, int pageSize) {
        Page<Transaction> pageResult = transactionMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "rebate")
                        .orderByDesc(Transaction::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return data;
    }

    public void claimAllRebate(Long userId) {
        log.info("[返水] 用户{}领取全部返水", userId);
    }

    // ==================== 佣金提现/创建成员 ====================

    public void withdrawCommission(Long userId, BigDecimal amount) {
        // 佣金提现功能待完善
        log.info("[代理] 用户{}申请佣金提现 amount={}", userId, amount);
    }

    public Map<String, Object> createMember(Long agentId, String username, String password) {
        // 代理代注册功能待完善
        log.info("[代理] 代理{}创建成员 username={}", agentId, username);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        return data;
    }

    // ==================== 内部工具 ====================

    private List<User> getMemberList(Long userId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getInvitedBy, userId)
        );
    }

    private long countMembersAfter(List<User> members, LocalDateTime time) {
        return members.stream()
                .filter(m -> m.getCreateTime() != null && m.getCreateTime().isAfter(time))
                .count();
    }

    private BigDecimal sumRecharge(List<Long> memberIds, LocalDateTime after) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .in(Order::getUserId, memberIds)
                .eq(Order::getType, "recharge")
                .eq(Order::getStatus, "success");
        if (after != null) wrapper.ge(Order::getCreateTime, after);
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCommission(Long userId, LocalDateTime after) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getUserId, userId)
                .eq(Transaction::getType, "commission");
        if (after != null) wrapper.ge(Transaction::getCreateTime, after);
        List<Transaction> list = transactionMapper.selectList(wrapper);
        return list.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumTxAfter(List<Transaction> list, LocalDateTime time) {
        return list.stream()
                .filter(t -> t.getCreateTime() != null && t.getCreateTime().isAfter(time))
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
