package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.entity.Order;
import com.h5.entity.Transaction;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单与交易流水服务（用户端查询）
 */
@Service
public class OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private TransactionMapper transactionMapper;

    /**
     * 用户订单列表（充值/提现）
     */
    public Map<String, Object> getUserOrders(Long userId, int page, int pageSize,
                                               String type, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        if (type != null && !type.isEmpty()) wrapper.eq(Order::getType, type);
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);

        Page<Order> pageResult = orderMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    /**
     * 订单详情
     */
    public Order getOrderDetail(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
        );
        if (order == null) throw new BusinessException("订单不存在");
        return order;
    }

    /**
     * 交易流水列表
     */
    public Map<String, Object> getTransactions(Long userId, int page, int pageSize, String type) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getUserId, userId)
                .orderByDesc(Transaction::getCreateTime);
        if (type != null && !type.isEmpty()) wrapper.eq(Transaction::getType, type);

        Page<Transaction> pageResult = transactionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    /**
     * 用户资金统计（充值总额/提现总额/投注总额/中奖总额）
     */
    public Map<String, Object> getFundSummary(Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRecharge", sumByType(userId, "recharge"));
        data.put("totalWithdraw", sumByType(userId, "withdraw").abs());
        data.put("totalBet", sumByType(userId, "bet").abs());
        data.put("totalWin", sumByType(userId, "win"));
        return data;
    }

    private BigDecimal sumByType(Long userId, String type) {
        List<Transaction> list = transactionMapper.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getUserId, userId)
                        .eq(Transaction::getType, type)
        );
        return list.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
