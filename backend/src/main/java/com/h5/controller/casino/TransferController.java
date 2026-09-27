package com.h5.controller.casino;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 娱乐城转账 Controller（真实数据实现）
 */
@RestController
@RequestMapping("/wap/transfer")
public class TransferController {

    @Autowired private UserMapper userMapper;
    @Autowired private TransactionMapper transactionMapper;

    /** 各平台钱包余额 */
    @GetMapping("/balances")
    public Result<List<Map<String, Object>>> balances() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> main = new HashMap<>();
        main.put("platform", "main");
        main.put("name", "主钱包");
        main.put("balance", user != null && user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
        list.add(main);
        return Result.success(list);
    }

    /** 转账记录 */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        Page<Transaction> pageResult = transactionMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .and(w -> w.eq(Transaction::getType, "transfer_in")
                                .or().eq(Transaction::getType, "transfer_out"))
                        .orderByDesc(Transaction::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }
}
