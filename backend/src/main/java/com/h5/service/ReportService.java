package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.Bet;
import com.h5.entity.Order;
import com.h5.mapper.BetMapper;
import com.h5.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表 Service（盈亏统计/资金流水报表）
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired private OrderMapper orderMapper;
    @Autowired private BetMapper betMapper;

    /**
     * 盈亏统计
     * @param startDate 开始日期 yyyy-MM-dd，null则本月1号
     * @param endDate 结束日期 yyyy-MM-dd，null则今天
     */
    public Map<String, Object> getProfitReport(Long userId, String startDate, String endDate) {
        LocalDateTime start = parseDate(startDate, LocalDate.now().withDayOfMonth(1).atStartOfDay());
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        // 充值统计
        List<Order> rechargeOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getType, "recharge")
                        .eq(Order::getStatus, "success")
                        .ge(Order::getCreateTime, start)
                        .le(Order::getCreateTime, end)
        );
        BigDecimal totalRecharge = sumOrderAmount(rechargeOrders);

        // 提现统计
        List<Order> withdrawOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getType, "withdraw")
                        .eq(Order::getStatus, "success")
                        .ge(Order::getCreateTime, start)
                        .le(Order::getCreateTime, end)
        );
        BigDecimal totalWithdraw = sumOrderAmount(withdrawOrders);

        // 投注统计
        List<Bet> bets = betMapper.selectList(
                new LambdaQueryWrapper<Bet>()
                        .eq(Bet::getUserId, userId)
                        .ge(Bet::getCreateTime, start)
                        .le(Bet::getCreateTime, end)
        );
        BigDecimal totalBet = bets.stream()
                .map(b -> b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWin = bets.stream()
                .filter(b -> "win".equals(b.getStatus()))
                .map(b -> b.getWinAmount() != null ? b.getWinAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 净盈亏 = 中奖 - 投注
        BigDecimal netProfit = totalWin.subtract(totalBet);

        // 按日统计
        Map<String, Map<String, BigDecimal>> dailyMap = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Bet b : bets) {
            if (b.getCreateTime() == null) continue;
            String day = b.getCreateTime().format(fmt);
            Map<String, BigDecimal> dayData = dailyMap.computeIfAbsent(day, k -> {
                Map<String, BigDecimal> m = new HashMap<>();
                m.put("bet", BigDecimal.ZERO);
                m.put("win", BigDecimal.ZERO);
                return m;
            });
            dayData.put("bet", dayData.get("bet").add(b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO));
            if ("win".equals(b.getStatus())) {
                dayData.put("win", dayData.get("win").add(b.getWinAmount() != null ? b.getWinAmount() : BigDecimal.ZERO));
            }
        }

        List<Map<String, Object>> daily = new ArrayList<>();
        for (Map.Entry<String, Map<String, BigDecimal>> entry : dailyMap.entrySet()) {
            Map<String, Object> d = new HashMap<>();
            d.put("date", entry.getKey());
            d.put("betAmount", entry.getValue().get("bet"));
            d.put("winAmount", entry.getValue().get("win"));
            d.put("netProfit", entry.getValue().get("win").subtract(entry.getValue().get("bet")));
            daily.add(d);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalRecharge", totalRecharge);
        data.put("totalWithdraw", totalWithdraw);
        data.put("totalBet", totalBet);
        data.put("totalWin", totalWin);
        data.put("netProfit", netProfit);
        data.put("rechargeCount", rechargeOrders.size());
        data.put("withdrawCount", withdrawOrders.size());
        data.put("betCount", bets.size());
        data.put("daily", daily);
        return data;
    }

    private BigDecimal sumOrderAmount(List<Order> orders) {
        return orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDateTime parseDate(String dateStr, LocalDateTime defaultValue) {
        if (dateStr == null || dateStr.isEmpty()) return defaultValue;
        try {
            return LocalDate.parse(dateStr).atStartOfDay();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
