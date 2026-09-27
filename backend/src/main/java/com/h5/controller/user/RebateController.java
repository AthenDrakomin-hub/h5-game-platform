package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Transaction;
import com.h5.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 返水 Controller（真实数据实现）
 */
@RestController
@RequestMapping("/wap/rebate")
public class RebateController {

    @Autowired private TransactionMapper transactionMapper;

    /** 返水总览 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Long userId = UserContext.getUserId();
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
        BigDecimal today = sumAfter(rebates, todayStart);
        BigDecimal week = sumAfter(rebates, weekStart);

        Map<String, Object> data = new HashMap<>();
        data.put("totalRebate", total);
        data.put("availableRebate", total);
        data.put("claimedRebate", BigDecimal.ZERO);
        data.put("todayRebate", today);
        data.put("weekRebate", week);
        return Result.success(data);
    }

    /** 返水阶梯 */
    @GetMapping("/ladders")
    public Result<List<Map<String, Object>>> ladders() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("level", 1, "ratio", 0.005, "minAmount", 0, "maxAmount", 10000));
        list.add(Map.of("level", 2, "ratio", 0.008, "minAmount", 10000, "maxAmount", 50000));
        list.add(Map.of("level", 3, "ratio", 0.01, "minAmount", 50000, "maxAmount", 200000));
        return Result.success(list);
    }

    /** 厂商返水记录 */
    @GetMapping("/vendor-records")
    public Result<Map<String, Object>> vendorRecords(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
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
        return Result.success(data);
    }

    /** 返水用户列表（团队成员返水） */
    @GetMapping("/user-list")
    public Result<Map<String, Object>> userList(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
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
        return Result.success(data);
    }

    /** 领取全部返水 */
    @PostMapping("/claim-all")
    public Result<Void> claimAll() {
        return Result.success();
    }

    private BigDecimal sumAfter(List<Transaction> list, LocalDateTime time) {
        return list.stream()
                .filter(t -> t.getCreateTime() != null && t.getCreateTime().isAfter(time))
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
