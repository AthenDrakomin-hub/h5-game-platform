package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.entity.Order;
import com.h5.entity.Transaction;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private TransactionMapper transactionMapper;
    @InjectMocks private OrderService orderService;

    private Order testOrder;
    private Transaction testTx;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD001");
        testOrder.setUserId(1L);
        testOrder.setType("recharge");
        testOrder.setAmount(new BigDecimal("100"));
        testOrder.setStatus("success");

        testTx = new Transaction();
        testTx.setId(1L);
        testTx.setUserId(1L);
        testTx.setType("recharge");
        testTx.setAmount(new BigDecimal("100"));
    }

    @Nested
    @DisplayName("用户订单查询")
    class UserOrders {
        @Test
        @DisplayName("分页查询用户订单")
        void testGetUserOrders() {
            Page<Order> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(testOrder));
            mockPage.setTotal(1);
            when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

            Map<String, Object> result = orderService.getUserOrders(1L, 1, 10, "recharge", "success");
            assertEquals(1L, result.get("total"));
            assertNotNull(result.get("list"));
        }
    }

    @Nested
    @DisplayName("交易流水查询")
    class Transactions {
        @Test
        @DisplayName("分页查询交易流水")
        void testGetTransactions() {
            Page<Transaction> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(testTx));
            mockPage.setTotal(1);
            when(transactionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

            Map<String, Object> result = orderService.getTransactions(1L, 1, 10, "recharge");
            assertEquals(1L, result.get("total"));
        }
    }

    @Nested
    @DisplayName("资金统计")
    class FundSummary {
        @Test
        @DisplayName("统计充值/提现/投注/中奖总额")
        void testGetFundSummary() {
            Transaction recharge = new Transaction();
            recharge.setType("recharge");
            recharge.setAmount(new BigDecimal("500"));
            Transaction bet = new Transaction();
            bet.setType("bet");
            bet.setAmount(new BigDecimal("-200"));
            Transaction win = new Transaction();
            win.setType("win");
            win.setAmount(new BigDecimal("150"));

            when(transactionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(recharge, bet, win));

            Map<String, Object> result = orderService.getFundSummary(1L);
            assertEquals(0, ((BigDecimal) result.get("totalRecharge")).compareTo(new BigDecimal("500")));
            assertEquals(0, ((BigDecimal) result.get("totalBet")).compareTo(new BigDecimal("200")));
            assertEquals(0, ((BigDecimal) result.get("totalWin")).compareTo(new BigDecimal("150")));
        }
    }

    @Nested
    @DisplayName("订单详情")
    class OrderDetail {
        @Test
        @DisplayName("正常查询订单详情")
        void testGetOrderDetail() {
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testOrder);
            Order result = orderService.getOrderDetail(1L, "ORD001");
            assertEquals("ORD001", result.getOrderNo());
        }

        @Test
        @DisplayName("订单不存在抛异常")
        void testOrderNotFound() {
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            assertThrows(RuntimeException.class, () -> orderService.getOrderDetail(1L, "NOTEXIST"));
        }
    }
}
