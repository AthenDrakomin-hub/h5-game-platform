package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.UserContext;
import com.h5.entity.Transaction;
import com.h5.entity.User;
import com.h5.entity.UserWallet;
import com.h5.mapper.TransactionMapper;
import com.h5.mapper.UserMapper;
import com.h5.mapper.UserWalletMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 钱包 Service
 * 封装钱包地址管理、余额宝转入转出、娱乐城转账查询
 * 所有余额变动必须通过 BalanceService（乐观锁+流水）
 */
@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    @Autowired private UserWalletMapper walletMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private BalanceService balanceService;

    // ==================== 钱包地址 ====================

    public List<UserWallet> listWallets(Long userId) {
        return walletMapper.selectList(
                new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getUserId, userId)
                        .eq(UserWallet::getStatus, 1)
                        .orderByDesc(UserWallet::getIsDefault)
                        .orderByDesc(UserWallet::getCreateTime)
        );
    }

    @Transactional
    public UserWallet addWallet(Long userId, UserWallet wallet) {
        if (wallet.getAddress() == null || wallet.getAddress().isEmpty()) {
            throw new BusinessException("钱包地址不能为空");
        }
        if (wallet.getChain() == null || wallet.getChain().isEmpty()) {
            throw new BusinessException("链类型不能为空");
        }
        wallet.setUserId(userId);
        wallet.setStatus(1);
        if (wallet.getIsDefault() == null) wallet.setIsDefault(0);

        // 设为默认时取消其他默认
        if (wallet.getIsDefault() == 1) {
            List<UserWallet> defaults = walletMapper.selectList(
                    new LambdaQueryWrapper<UserWallet>()
                            .eq(UserWallet::getUserId, userId)
                            .eq(UserWallet::getIsDefault, 1)
            );
            for (UserWallet w : defaults) {
                w.setIsDefault(0);
                walletMapper.updateById(w);
            }
        }
        walletMapper.insert(wallet);
        log.info("[钱包] 用户{}添加钱包地址 chain={} address={}", userId, wallet.getChain(), wallet.getAddress());
        return wallet;
    }

    @Transactional
    public void deleteWallet(Long userId, Long id) {
        UserWallet wallet = walletMapper.selectById(id);
        if (wallet == null || !wallet.getUserId().equals(userId)) {
            throw new BusinessException("钱包地址不存在");
        }
        walletMapper.deleteById(id);
        log.info("[钱包] 用户{}删除钱包地址 id={}", userId, id);
    }

    // ==================== 余额宝 ====================

    public Map<String, Object> yuebaoConfig() {
        Map<String, Object> data = new HashMap<>();
        data.put("annualRate", 0.08);
        data.put("dailyRate", 0.00022);
        data.put("minAmount", new BigDecimal("100"));
        data.put("maxAmount", new BigDecimal("1000000"));
        data.put("status", 1);
        return data;
    }

    public Map<String, Object> yuebaoInfo(Long userId) {
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        // 余额宝余额暂存主钱包，功能待完善
        data.put("balance", new BigDecimal("0.00"));
        data.put("totalProfit", new BigDecimal("0.00"));
        data.put("todayProfit", new BigDecimal("0.00"));
        data.put("yesterdayProfit", new BigDecimal("0.00"));
        data.put("availableBalance", user != null && user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
        return data;
    }

    public Map<String, Object> yuebaoRecords(Long userId, int page, int pageSize) {
        // 余额宝记录暂为空，功能待完善
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return data;
    }

    /**
     * 余额宝转入：从主钱包扣减，通过BalanceService保证乐观锁+流水
     */
    @Transactional
    public Map<String, Object> yuebaoTransferIn(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("转入金额必须大于0");
        }
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (amount.compareTo(user.getBalance()) > 0) {
            throw new BusinessException("余额不足");
        }

        String orderNo = "YB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + new Random().nextInt(10000);
        // 扣减主钱包余额（余额宝功能待完善，暂记为冻结）
        balanceService.subtractBalance(userId, amount, "yuebao_in", null, orderNo, "余额宝转入");

        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("balance", userMapper.selectById(userId).getBalance());
        log.info("[余额宝] 用户{}转入 amount={}", userId, amount);
        return data;
    }

    /**
     * 余额宝转出：加回主钱包，通过BalanceService
     */
    @Transactional
    public Map<String, Object> yuebaoTransferOut(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("转出金额必须大于0");
        }
        String orderNo = "YB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + new Random().nextInt(10000);
        balanceService.addBalance(userId, amount, "yuebao_out", null, orderNo, "余额宝转出");

        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("balance", userMapper.selectById(userId).getBalance());
        log.info("[余额宝] 用户{}转出 amount={}", userId, amount);
        return data;
    }

    // ==================== 娱乐城转账 ====================

    public List<Map<String, Object>> transferBalances(Long userId) {
        User user = userMapper.selectById(userId);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> main = new HashMap<>();
        main.put("platform", "main");
        main.put("name", "主钱包");
        main.put("balance", user != null && user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
        list.add(main);
        return list;
    }

    public Map<String, Object> transferRecords(Long userId, int page, int pageSize) {
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
        return data;
    }
}
