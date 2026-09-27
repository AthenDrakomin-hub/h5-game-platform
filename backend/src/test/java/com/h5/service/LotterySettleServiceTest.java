package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.Bet;
import com.h5.entity.DrawResult;
import com.h5.entity.User;
import com.h5.mapper.BetMapper;
import com.h5.service.settle.LotterySettleService;
import com.h5.service.settle.PlayRuleEngine;
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
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LotterySettleService 单元测试
 * 覆盖：中奖结算 / 未中奖结算 / 批量结算 / 无待结算投注 / 单注异常隔离
 */
@ExtendWith(MockitoExtension.class)
class LotterySettleServiceTest {

    @Mock private BetMapper betMapper;
    @Mock private PlayRuleEngine playRuleEngine;
    @Mock private BalanceService balanceService;

    @InjectMocks
    private LotterySettleService settleService;

    private Bet pendingBet;
    private DrawResult drawResult;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setBalance(new BigDecimal("500.00"));

        pendingBet = new Bet();
        pendingBet.setId(100L);
        pendingBet.setBetNo("B20240101001");
        pendingBet.setUserId(1L);
        pendingBet.setLotteryCode("jspk10");
        pendingBet.setPeriod("2024001");
        pendingBet.setPlayType("big");
        pendingBet.setNumbers("");
        pendingBet.setAmount(new BigDecimal("100"));
        pendingBet.setOdds(new BigDecimal("1.95"));
        pendingBet.setWinAmount(BigDecimal.ZERO);
        pendingBet.setStatus("pending");

        drawResult = new DrawResult();
        drawResult.setId(1L);
        drawResult.setLotteryCode("jspk10");
        drawResult.setPeriod("2024001");
        drawResult.setNumbers("06,07,08,09,10,01,02,03,04,05");
        drawResult.setStatus(1);
    }

    @Nested
    @DisplayName("中奖结算")
    class WinSettlement {
        @Test
        @DisplayName("中奖投注正确结算并发放奖金")
        void testWinBetSettled() {
            when(betMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(pendingBet));
            when(playRuleEngine.calculateWin(eq("jspk10"), eq("big"), anyString(), anyString(),
                    eq(new BigDecimal("100")), eq(new BigDecimal("1.95"))))
                    .thenReturn(new BigDecimal("195.00"));
            when(balanceService.addBalance(eq(1L), any(BigDecimal.class), eq("win"),
                    eq(100L), eq("B20240101001"), anyString()))
                    .thenReturn(testUser);

            Map<String, Object> result = settleService.settlePeriod(drawResult);

            assertEquals(1, result.get("total"));
            assertEquals(1, result.get("win"));
            assertEquals(0, result.get("lose"));
            assertEquals(0, ((BigDecimal) result.get("totalWinAmount")).compareTo(new BigDecimal("195.00")));

            verify(betMapper, times(1)).updateById(argThat(bet ->
                    "win".equals(bet.getStatus()) &&
                    bet.getWinAmount().compareTo(new BigDecimal("195.00")) == 0
            ));
            verify(balanceService, times(1)).addBalance(eq(1L), eq(new BigDecimal("195.00")),
                    eq("win"), eq(100L), eq("B20240101001"), anyString());
        }
    }

    @Nested
    @DisplayName("未中奖结算")
    class LoseSettlement {
        @Test
        @DisplayName("未中奖投注标记为lose且不发奖金")
        void testLoseBetSettled() {
            when(betMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(pendingBet));
            when(playRuleEngine.calculateWin(anyString(), anyString(), anyString(), anyString(),
                    any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            Map<String, Object> result = settleService.settlePeriod(drawResult);

            assertEquals(1, result.get("total"));
            assertEquals(0, result.get("win"));
            assertEquals(1, result.get("lose"));

            verify(betMapper, times(1)).updateById(argThat(bet ->
                    "lose".equals(bet.getStatus()) &&
                    bet.getWinAmount().compareTo(BigDecimal.ZERO) == 0
            ));
            verify(balanceService, never()).addBalance(anyLong(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("批量结算")
    class BatchSettlement {
        @Test
        @DisplayName("多个投注批量结算（一中一未中）")
        void testMultipleBetsSettled() {
            Bet bet2 = new Bet();
            bet2.setId(101L);
            bet2.setBetNo("B20240101002");
            bet2.setUserId(1L);
            bet2.setLotteryCode("jspk10");
            bet2.setPeriod("2024001");
            bet2.setPlayType("small");
            bet2.setAmount(new BigDecimal("50"));
            bet2.setOdds(new BigDecimal("1.95"));
            bet2.setStatus("pending");

            when(betMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(pendingBet, bet2));
            when(playRuleEngine.calculateWin(eq("jspk10"), eq("big"), anyString(), anyString(),
                    any(), any())).thenReturn(new BigDecimal("195.00"));
            when(playRuleEngine.calculateWin(eq("jspk10"), eq("small"), anyString(), anyString(),
                    any(), any())).thenReturn(BigDecimal.ZERO);
            when(balanceService.addBalance(anyLong(), any(), any(), any(), any(), any()))
                    .thenReturn(testUser);

            Map<String, Object> result = settleService.settlePeriod(drawResult);

            assertEquals(2, result.get("total"));
            assertEquals(1, result.get("win"));
            assertEquals(1, result.get("lose"));
            verify(betMapper, times(2)).updateById(any(Bet.class));
            verify(balanceService, times(1)).addBalance(anyLong(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("无待结算投注")
    class NoPendingBets {
        @Test
        @DisplayName("没有pending投注时返回全0统计")
        void testNoPendingBets() {
            when(betMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            Map<String, Object> result = settleService.settlePeriod(drawResult);

            assertEquals(0, result.get("total"));
            assertEquals(0, result.get("win"));
            assertEquals(0, result.get("lose"));
            verify(betMapper, never()).updateById(any());
            verify(balanceService, never()).addBalance(anyLong(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("单注异常隔离")
    class ExceptionIsolation {
        @Test
        @DisplayName("单注结算异常不影响其他投注")
        void testSingleBetExceptionIsolated() {
            Bet bet2 = new Bet();
            bet2.setId(101L);
            bet2.setBetNo("B20240101002");
            bet2.setUserId(1L);
            bet2.setLotteryCode("jspk10");
            bet2.setPeriod("2024001");
            bet2.setPlayType("small");
            bet2.setAmount(new BigDecimal("50"));
            bet2.setOdds(new BigDecimal("1.95"));
            bet2.setStatus("pending");

            when(betMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(pendingBet, bet2));
            when(playRuleEngine.calculateWin(eq("jspk10"), eq("big"), anyString(), anyString(),
                    any(), any())).thenThrow(new RuntimeException("玩法计算异常"));
            when(playRuleEngine.calculateWin(eq("jspk10"), eq("small"), anyString(), anyString(),
                    any(), any())).thenReturn(BigDecimal.ZERO);

            Map<String, Object> result = settleService.settlePeriod(drawResult);

            assertEquals(2, result.get("total"));
            assertEquals(0, result.get("win"));
            assertEquals(1, result.get("lose"));
            verify(betMapper, times(1)).updateById(any(Bet.class));
        }
    }

    @Nested
    @DisplayName("期结算状态检查")
    class PeriodSettlementCheck {
        @Test
        @DisplayName("无pending投注时返回已结算")
        void testPeriodSettled() {
            when(betMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            assertTrue(settleService.isPeriodSettled("jspk10", "2024001"));
        }

        @Test
        @DisplayName("有pending投注时返回未结算")
        void testPeriodNotSettled() {
            when(betMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
            assertFalse(settleService.isPeriodSettled("jspk10", "2024001"));
        }
    }
}
