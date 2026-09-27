package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Order;
import com.h5.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交易记录 Controller（委托 OrderService）
 */
@RestController
@RequestMapping("/wap/transaction")
public class TransactionController {

    @Autowired private OrderService orderService;

    /** 交易记录列表 */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type) {
        return Result.success(orderService.getTransactions(UserContext.getUserId(), page, pageSize, type));
    }

    /** 充值订单列表 */
    @GetMapping("/recharge-list")
    public Result<Map<String, Object>> rechargeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(orderService.getUserOrders(UserContext.getUserId(), page, pageSize, "recharge", status));
    }

    /** 提现订单列表 */
    @GetMapping("/withdraw-list")
    public Result<Map<String, Object>> withdrawList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(orderService.getUserOrders(UserContext.getUserId(), page, pageSize, "withdraw", status));
    }

    /** 订单详情 */
    @GetMapping("/detail/{orderNo}")
    public Result<Order> detail(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(UserContext.getUserId(), orderNo));
    }
}
