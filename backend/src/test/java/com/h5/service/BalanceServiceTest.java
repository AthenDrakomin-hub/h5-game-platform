package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BalanceService 单元测试
 * 覆盖：扣减余额 / 加余额 / 冻结 / 确认冻结 / 解冻退回 / 管理员调整
 */
@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private BalanceService balanceService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setFrozenBalance(new BigDecimal("0.00"));
        testUser.setVersion(1);
    }

    // ==================== 扣减余额 ====================
    @Nested
    @DisplayName("扣减余额 deductBalance")
    class DeductBalance {

        @Test
        @DisplayName("正常扣减余额")
        void testDeductSuccess() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

            User result = balanceService.deductBalance(1L, new BigDecimal("200"),
                    "bet", 100L, "B001", "测试投注");

            assertEquals(0, result.getBalance().compareTo(new BigDecimal("800.00")));
            verify(userMapper, times(1)).updateById(any(User.class));
            verify(transactionMapper, times(1)).insert(any(Transaction.class));
        }

        @Test
        @DisplayName("余额不足抛出异常")
        void testDeductInsufficient() {
            when(userMapper.selectById(1L)).thenReturn(testUser);

            assertThrows(BusinessException.class, () ->
                    balanceService.deductBalance(1L, new BigDecimal("2000"),
                            "bet", 100L, "B001", "测试投注")
            );
            verify(userMapper, never()).updateById(any());
            verify(transactionMapper, never()).insert(any());
        }

        @Test
        @DisplayName("用户不存在抛出异常")
        void testDeductUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () ->
                    balanceService.deductBalance(999L, new BigDecimal("100"),
                            "bet", 100L, "B001", "测试")
            );
        }

        @Test
        @DisplayName("扣减金额为0抛出异常")
        void testDeductZeroAmount() {
            assertThrows(BusinessException.class, () ->
                    balanceService.deductBalance(1L, BigDecimal.ZERO,
                            "bet", 100L, "B001", "测试")
            );
        }
    }

    // ==================== 加余额 ====================
    @Nested
    @DisplayName("加余额 addBalance")
    class AddBalance {

        @Test
        @DisplayName("正常加余额")
        void testAddSuccess() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

            User result = balanceService.addBalance(1L, new BigDecimal("500"),
                    "recharge", 200L, "R001", "充值到账");

            assertEquals(0, result.getBalance().compareTo(new BigDecimal("1500.00")));
        }

        @Test
        @DisplayName("加余额金额为0抛出异常")
        void testAddZeroAmount() {
            assertThrows(BusinessException.class, () ->
                    balanceService.addBalance(1L, BigDecimal.ZERO,
                            "recharge", 200L, "R001", "测试")
            );
        }
    }

    // ==================== 冻结余额 ====================
    @Nested
    @DisplayName("冻结余额 freezeBalance")
    class FreezeBalance {

        @Test
        @DisplayName("正常冻结余额")
        void testFreezeSuccess() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

            User result = balanceService.freezeBalance(1L, new BigDecimal("300"),
                    "withdraw", 300L, "W001", "提现冻结");

            assertEquals(0, result.getBalance().compareTo(new BigDecimal("700.00")));
            assertEquals(0, result.getFrozenBalance().compareTo(new BigDecimal("300.00")));
        }

        @Test
        @DisplayName("冻结金额超过可用余额抛出异常")
        void testFreezeInsufficient() {
            when(userMapper.selectById(1L)).thenReturn(testUser);

            assertThrows(BusinessException.class, () ->
                    balanceService.freezeBalance(1L, new BigDecimal("1500"),
                            "withdraw", 300L, "W001", "提现冻结")
            );
        }
    }

    // ==================== 确认冻结扣减 ====================
    @Nested
    @DisplayName("确认冻结扣减 confirmFreeze")
    class ConfirmFreeze {

        @Test
        @DisplayName("正常确认冻结扣减")
        void testConfirmSuccess() {
            testUser.setFrozenBalance(new BigDecimal("300.00"));
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            User result = balanceService.confirmFreeze(1L, new BigDecimal("300"));

            assertEquals(0, result.getFrozenBalance().compareTo(BigDecimal.ZERO));
            // balance 不变（冻结时已扣）
            assertEquals(0, result.getBalance().compareTo(new BigDecimal("1000.00")));
        }
    }

    // ==================== 解冻退回 ====================
    @Nested
    @DisplayName("解冻退回 unfreezeAndRefund")
    class UnfreezeAndRefund {

        @Test
        @DisplayName("正常解冻退回")
        void testUnfreezeSuccess() {
            testUser.setBalance(new BigDecimal("700.00"));
            testUser.setFrozenBalance(new BigDecimal("300.00"));
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

            User result = balanceService.unfreezeAndRefund(1L, new BigDecimal("300"), "W001");

            assertEquals(0, result.getBalance().compareTo(new BigDecimal("1000.00")));
            assertEquals(0, result.getFrozenBalance().compareTo(BigDecimal.ZERO));
        }
    }

    // ==================== 管理员调整 ====================
    @Nested
    @DisplayName("管理员调整 adminAdjustBalance")
    class AdminAdjust {

        @Test
        @DisplayName("管理员加余额")
        void testAdminAdd() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

            User result = balanceService.adminAdjustBalance(1L, new BigDecimal("100"),
                    "管理员加款", "admin01");

            assertEquals(0, result.getBalance().compareTo(new BigDecimal("1100.00")));
        }

        @Test
        @DisplayName("管理员扣余额")
        void testAdminDeduct() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

            User result = balanceService.adminAdjustBalance(1L, new BigDecimal("-100"),
                    "管理员扣款", "admin01");

            assertEquals(0, result.getBalance().compareTo(new BigDecimal("900.00")));
        }
    }
}
