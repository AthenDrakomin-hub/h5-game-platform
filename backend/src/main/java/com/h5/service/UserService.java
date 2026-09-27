package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.entity.*;
import com.h5.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户服务
 * 处理用户信息、密码、VIP、银行卡、钱包等用户域逻辑
 */
@Service
public class UserService {

    @Autowired private UserMapper userMapper;
    @Autowired private UserBankCardMapper bankCardMapper;
    @Autowired private UserWalletMapper walletMapper;
    @Autowired private MessageMapper messageMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 用户信息 ====================

    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");
        return user;
    }

    public Map<String, Object> getUserInfo(Long userId) {
        User user = getUserById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("balance", user.getBalance());
        data.put("frozenBalance", user.getFrozenBalance());
        data.put("vipLevel", user.getVipLevel());
        data.put("isTrial", user.getIsTrial());
        data.put("inviteCode", user.getInviteCode());
        data.put("hasFundPassword", user.getFundPassword() != null && !user.getFundPassword().isEmpty());
        data.put("createTime", user.getCreateTime());
        return data;
    }

    @Transactional
    public void updateProfile(Long userId, String nickname, String avatar, String phone, String email) {
        User user = getUserById(userId);
        if (nickname != null) user.setNickname(nickname);
        if (avatar != null) user.setAvatar(avatar);
        if (phone != null) user.setPhone(phone);
        if (email != null) user.setEmail(email);
        userMapper.updateById(user);
    }

    // ==================== 密码管理 ====================

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Transactional
    public void setFundPassword(Long userId, String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException("资金密码长度不能少于6位");
        }
        User user = getUserById(userId);
        user.setFundPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    @Transactional
    public void changeFundPassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (user.getFundPassword() == null || user.getFundPassword().isEmpty()) {
            throw new BusinessException("请先设置资金密码");
        }
        if (!passwordEncoder.matches(oldPassword, user.getFundPassword())) {
            throw new BusinessException("原资金密码错误");
        }
        user.setFundPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // ==================== 银行卡管理 ====================

    public List<UserBankCard> getBankCards(Long userId) {
        return bankCardMapper.selectList(
                new LambdaQueryWrapper<UserBankCard>()
                        .eq(UserBankCard::getUserId, userId)
                        .orderByDesc(UserBankCard::getCreateTime)
        );
    }

    @Transactional
    public UserBankCard addBankCard(Long userId, String bankName, String branchName,
                                     String cardNumber, String cardHolder, String phone) {
        if (cardNumber == null || cardNumber.length() < 10) {
            throw new BusinessException("银行卡号格式不正确");
        }
        UserBankCard card = new UserBankCard();
        card.setUserId(userId);
        card.setBankName(bankName);
        card.setBranchName(branchName != null ? branchName : "");
        card.setCardNumber(cardNumber);
        card.setCardHolder(cardHolder);
        card.setPhone(phone != null ? phone : "");
        card.setIsDefault(0);
        card.setStatus(1);
        bankCardMapper.insert(card);
        return card;
    }

    @Transactional
    public void deleteBankCard(Long userId, Long cardId) {
        UserBankCard card = bankCardMapper.selectById(cardId);
        if (card == null || !card.getUserId().equals(userId)) {
            throw new BusinessException("银行卡不存在");
        }
        bankCardMapper.deleteById(cardId);
    }

    // ==================== USDT 钱包管理 ====================

    public List<UserWallet> getWallets(Long userId) {
        return walletMapper.selectList(
                new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getUserId, userId)
                        .orderByDesc(UserWallet::getCreateTime)
        );
    }

    @Transactional
    public UserWallet addWallet(Long userId, String chain, String address, String label) {
        if (address == null || address.length() < 20) {
            throw new BusinessException("钱包地址格式不正确");
        }
        UserWallet wallet = new UserWallet();
        wallet.setUserId(userId);
        wallet.setChain(chain != null ? chain : "TRC20");
        wallet.setAddress(address);
        wallet.setLabel(label != null ? label : "");
        wallet.setIsDefault(0);
        wallet.setStatus(1);
        walletMapper.insert(wallet);
        return wallet;
    }

    @Transactional
    public void deleteWallet(Long userId, Long walletId) {
        UserWallet wallet = walletMapper.selectById(walletId);
        if (wallet == null || !wallet.getUserId().equals(userId)) {
            throw new BusinessException("钱包地址不存在");
        }
        walletMapper.deleteById(walletId);
    }

    // ==================== 消息中心 ====================

    public Map<String, Object> getMessages(Long userId, int page, int pageSize) {
        Page<Message> pageResult = messageMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0))
                        .orderByDesc(Message::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    public long getUnreadCount(Long userId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0))
                        .eq(Message::getIsRead, 0)
        );
    }

    @Transactional
    public void markMessageRead(Long userId, Long messageId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg != null && (msg.getUserId().equals(userId) || msg.getUserId() == 0)) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Message> unread = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0))
                        .eq(Message::getIsRead, 0)
        );
        for (Message msg : unread) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
    }
}
