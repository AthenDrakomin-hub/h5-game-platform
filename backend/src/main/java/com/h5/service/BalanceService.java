package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.h5.common.BusinessException;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 余额安全服务
 * 所有用户余额变动必须通过此服务，禁止 Controller 直接操作 users.balance
 *
 * 安全机制：
 * 1. 乐观锁（@Version）防止并发超扣
 * 2. 冲突自动重试（最多3次）
 * 3. 每次变动同步写 transactions 流水
 * 4. 余额不足时抛异常，事务回滚
 */
@Service
public class BalanceService {

    private static final Logger log = LoggerFactory.getLogger(BalanceService.class);
    private static final int MAX_RETRY = 3;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    /**
     * 扣减余额（投注、提现冻结等）
     *
     * @param userId      用户ID
     * @param amount      扣减金额（正数）
     * @param type        交易类型 bet/withdraw/transfer 等
     * @param refId       关联业务ID
     * @param refNo       关联业务单号
     * @param description 描述
     * @return 变动后的用户
     */
    @Transactional
    public User deductBalance(Long userId, BigDecimal amount, String type,
                              Long refId, String refNo, String description) {
        validateAmount(amount);

        User user = getUserOrThrow(userId);
        BigDecimal balanceBefore = user.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException("余额不足，当前余额: " + balanceBefore);
        }

        // 乐观锁扣减，带重试
        User updated = retryUpdate(() -> {
            User fresh = userMapper.selectById(userId);
            if (fresh == null) throw new BusinessException(401, "用户不存在");
            if (fresh.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("余额不足，当前余额: " + fresh.getBalance());
            }
            fresh.setBalance(fresh.getBalance().subtract(amount));
            int rows = userMapper.updateById(fresh);
            if (rows == 0) return null; // 乐观锁冲突，返回null触发重试
            return fresh;
        });

        // 写交易流水
        writeTransaction(userId, type, amount.negate(), balanceBefore,
                updated.getBalance(), refId, refNo, description);

        log.info("[余额扣减] userId={} amount={} type={} refNo={} before={} after={} desc={}",
                userId, amount, type, refNo, balanceBefore, updated.getBalance(), description);
        return updated;
    }

    /**
     * 增加余额（充值到账、中奖、退款等）
     */
    @Transactional
    public User addBalance(Long userId, BigDecimal amount, String type,
                           Long refId, String refNo, String description) {
        validateAmount(amount);

        User user = getUserOrThrow(userId);
        BigDecimal balanceBefore = user.getBalance();

        User updated = retryUpdate(() -> {
            User fresh = userMapper.selectById(userId);
            if (fresh == null) throw new BusinessException(401, "用户不存在");
            fresh.setBalance(fresh.getBalance().add(amount));
            int rows = userMapper.updateById(fresh);
            if (rows == 0) return null;
            return fresh;
        });

        writeTransaction(userId, type, amount, balanceBefore,
                updated.getBalance(), refId, refNo, description);

        log.info("[余额增加] userId={} amount={} type={} refNo={} before={} after={} desc={}",
                userId, amount, type, refNo, balanceBefore, updated.getBalance(), description);
        return updated;
    }

    /**
     * 冻结余额（提现申请时：balance → frozen）
     * 同时扣减 balance 和增加 frozen_balance
     */
    @Transactional
    public User freezeBalance(Long userId, BigDecimal amount, String refNo) {
        validateAmount(amount);

        User user = getUserOrThrow(userId);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足，当前余额: " + user.getBalance());
        }
        BigDecimal balanceBefore = user.getBalance();

        User updated = retryUpdate(() -> {
            User fresh = userMapper.selectById(userId);
            if (fresh == null) throw new BusinessException(401, "用户不存在");
            if (fresh.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("余额不足，当前余额: " + fresh.getBalance());
            }
            fresh.setBalance(fresh.getBalance().subtract(amount));
            fresh.setFrozenBalance(fresh.getFrozenBalance().add(amount));
            int rows = userMapper.updateById(fresh);
            if (rows == 0) return null;
            return fresh;
        });

        writeTransaction(userId, "withdraw", amount.negate(), balanceBefore,
                updated.getBalance(), null, refNo, "提现申请冻结");

        log.info("[余额冻结] userId={} amount={} refNo={} before={} after={} frozen={}",
                userId, amount, refNo, balanceBefore, updated.getBalance(), updated.getFrozenBalance());
        return updated;
    }

    /**
     * 确认冻结扣减（提现审核通过：只扣 frozen_balance，不影响 balance）
     */
    @Transactional
    public User confirmFreeze(Long userId, BigDecimal amount) {
        validateAmount(amount);

        User user = getUserOrThrow(userId);
        if (user.getFrozenBalance().compareTo(amount) < 0) {
            throw new BusinessException("冻结余额不足");
        }

        return retryUpdate(() -> {
            User fresh = userMapper.selectById(userId);
            if (fresh == null) throw new BusinessException(401, "用户不存在");
            if (fresh.getFrozenBalance().compareTo(amount) < 0) {
                throw new BusinessException("冻结余额不足");
            }
            fresh.setFrozenBalance(fresh.getFrozenBalance().subtract(amount));
            int rows = userMapper.updateById(fresh);
            if (rows == 0) return null;
            return fresh;
        });
    }

    /**
     * 解冻退回（提现审核拒绝：frozen → balance）
     */
    @Transactional
    public User unfreezeAndRefund(Long userId, BigDecimal amount, String refNo) {
        validateAmount(amount);

        User user = getUserOrThrow(userId);
        if (user.getFrozenBalance().compareTo(amount) < 0) {
            throw new BusinessException("冻结余额不足");
        }
        BigDecimal balanceBefore = user.getBalance();

        User updated = retryUpdate(() -> {
            User fresh = userMapper.selectById(userId);
            if (fresh == null) throw new BusinessException(401, "用户不存在");
            if (fresh.getFrozenBalance().compareTo(amount) < 0) {
                throw new BusinessException("冻结余额不足");
            }
            fresh.setBalance(fresh.getBalance().add(amount));
            fresh.setFrozenBalance(fresh.getFrozenBalance().subtract(amount));
            int rows = userMapper.updateById(fresh);
            if (rows == 0) return null;
            return fresh;
        });

        writeTransaction(userId, "refund", amount, balanceBefore,
                updated.getBalance(), null, refNo, "提现拒绝退回");

        return updated;
    }

    /**
     * 管理员直接调整余额（增加或扣减）
     */
    @Transactional
    public User adminAdjustBalance(Long userId, BigDecimal delta, String description) {
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("调整金额不能为0");
        }

        User user = getUserOrThrow(userId);
        BigDecimal balanceBefore = user.getBalance();

        if (delta.compareTo(BigDecimal.ZERO) < 0 && balanceBefore.compareTo(delta.abs()) < 0) {
            throw new BusinessException("余额不足，无法扣减");
        }

        User updated = retryUpdate(() -> {
            User fresh = userMapper.selectById(userId);
            if (fresh == null) throw new BusinessException(401, "用户不存在");
            BigDecimal newBalance = fresh.getBalance().add(delta);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("余额不足，无法扣减");
            }
            fresh.setBalance(newBalance);
            int rows = userMapper.updateById(fresh);
            if (rows == 0) return null;
            return fresh;
        });

        writeTransaction(userId, "admin_adjust", delta, balanceBefore,
                updated.getBalance(), null, null, description);

        return updated;
    }

    // ==================== 内部工具方法 ====================

    private User getUserOrThrow(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }
    }

    /**
     * 乐观锁重试模板
     * action 返回 null 表示冲突需重试，抛出异常表示业务错误
     */
    private User retryUpdate(java.util.function.Supplier<User> action) {
        BusinessException lastError = null;
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                User result = action.get();
                if (result != null) {
                    return result;
                }
                // 乐观锁冲突，短暂等待后重试
                if (i < MAX_RETRY - 1) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (BusinessException e) {
                // 业务异常（如余额不足）不重试，直接抛出
                throw e;
            }
        }
        throw new BusinessException("操作繁忙，请稍后重试（并发冲突）");
    }

    private void writeTransaction(Long userId, String type, BigDecimal amount,
                                   BigDecimal balanceBefore, BigDecimal balanceAfter,
                                   Long refId, String refNo, String description) {
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(balanceAfter);
        tx.setRefId(refId);
        tx.setRefNo(refNo != null ? refNo : "");
        tx.setDescription(description != null ? description : "");
        transactionMapper.insert(tx);
    }
}
