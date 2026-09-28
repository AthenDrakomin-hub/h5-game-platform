package com.h5.controller.promo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Transaction;
import com.h5.mapper.TransactionMapper;
import com.h5.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 抢红包 Controller
 * 简化版：每日限次抢红包，随机金额直接到账余额
 */
@RestController
@RequestMapping("/wap/red-packet")
public class RedPacketController {

    @Autowired private BalanceService balanceService;
    @Autowired private TransactionMapper transactionMapper;

    /** 每日最大抢包次数 */
    private static final int MAX_DAILY_COUNT = 3;
    /** 单次最小金额 */
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.10");
    /** 单次最大金额 */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("8.88");

    /**
     * 红包活动信息
     * GET /api/wap/red-packet/info
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = UserContext.getUserId();
        int todayCount = getTodayCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("enabled", true);
        data.put("startTime", "10:00");
        data.put("endTime", "22:00");
        data.put("prizePool", "888.00");
        data.put("maxCount", MAX_DAILY_COUNT);
        data.put("myCount", todayCount);
        data.put("canGrab", todayCount < MAX_DAILY_COUNT);
        data.put("minAmount", MIN_AMOUNT);
        data.put("maxAmount", MAX_AMOUNT);
        return Result.success(data);
    }

    /**
     * 抢红包
     * POST /api/wap/red-packet/grab
     */
    @PostMapping("/grab")
    public Result<Map<String, Object>> grab() {
        Long userId = UserContext.getUserId();

        // 检查今日次数
        int todayCount = getTodayCount(userId);
        if (todayCount >= MAX_DAILY_COUNT) {
            throw new BusinessException("今日抢包次数已用完");
        }

        // 随机金额
        Random random = new Random();
        double range = MAX_AMOUNT.subtract(MIN_AMOUNT).doubleValue();
        BigDecimal amount = MIN_AMOUNT.add(new BigDecimal(random.nextDouble() * range))
                .setScale(2, RoundingMode.DOWN);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = MIN_AMOUNT;
        }

        // 到账余额 + 写流水
        String orderNo = "RP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + random.nextInt(10000);
        balanceService.addBalance(userId, amount, "red_packet", null, orderNo, "抢红包获得");

        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("orderNo", orderNo);
        data.put("remaining", MAX_DAILY_COUNT - todayCount - 1);
        return Result.success(data);
    }

    /**
     * 抢包记录
     * GET /api/wap/red-packet/records?page=1&pageSize=20
     */
    @GetMapping("/records")
    public Result<Map<String, Object>> records(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = UserContext.getUserId();
        List<Transaction> list = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "red_packet")
                        .orderByDesc(Transaction::getCreateTime)
                        .last("LIMIT " + (page - 1) * pageSize + "," + pageSize)
        );

        List<Map<String, Object>> records = new ArrayList<>();
        for (Transaction tx : list) {
            Map<String, Object> r = new HashMap<>();
            r.put("id", tx.getId());
            r.put("username", "我");
            r.put("amount", tx.getAmount());
            r.put("time", tx.getCreateTime() != null ? tx.getCreateTime().toString() : "");
            records.add(r);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", records);
        data.put("total", records.size());
        return Result.success(data);
    }

    /** 查询今日抢包次数 */
    private int getTodayCount(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Long count = transactionMapper.selectCount(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, "red_packet")
                        .ge(Transaction::getCreateTime, start)
                        .lt(Transaction::getCreateTime, end)
        );
        return count != null ? count.intValue() : 0;
    }
}
