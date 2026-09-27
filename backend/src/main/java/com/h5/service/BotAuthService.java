package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.dto.TelegramAuthDTO;
import com.h5.entity.BotTelegramUser;
import com.h5.entity.User;
import com.h5.mapper.BotTelegramUserMapper;
import com.h5.mapper.UserMapper;
import com.h5.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BotAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BotTelegramUserMapper botTelegramUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Telegram 用户自动注册/登录
     * - 已绑定：更新信息，返回登录态
     * - 未绑定：自动创建平台账号 + 绑定关系，返回登录态
     */
    @Transactional
    public Map<String, Object> telegramAuth(TelegramAuthDTO dto, String ip) {
        if (dto.getTelegramId() == null) {
            throw new BusinessException("telegramId 不能为空");
        }

        // 查找绑定记录
        BotTelegramUser binding = botTelegramUserMapper.selectOne(
                new LambdaQueryWrapper<BotTelegramUser>()
                        .eq(BotTelegramUser::getTelegramId, dto.getTelegramId())
        );

        User user;
        if (binding != null) {
            // 已绑定，查找用户
            user = userMapper.selectById(binding.getUserId());
            if (user == null) {
                throw new BusinessException("绑定用户不存在");
            }
            if (user.getStatus() == 0) {
                throw new BusinessException("账号已被禁用");
            }
            // 更新绑定信息
            updateBindingInfo(binding, dto);
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateById(user);
        } else {
            // 未绑定，自动创建平台账号
            String username = "tg_" + dto.getTelegramId();
            // 检查用户名是否已存在（理论上不会冲突）
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username)
            );
            if (count > 0) {
                // 用户名已存在（可能是之前手动注册的），直接绑定
                user = userMapper.selectOne(
                        new LambdaQueryWrapper<User>().eq(User::getUsername, username)
                );
            } else {
                // 创建新用户
                String password = UUID.randomUUID().toString().substring(0, 16);
                String nickname = buildNickname(dto);

                user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setNickname(nickname);
                user.setAvatar(dto.getPhotoUrl() != null ? dto.getPhotoUrl() : "");
                user.setBalance(BigDecimal.ZERO);
                user.setFrozenBalance(BigDecimal.ZERO);
                user.setVipLevel(1);
                user.setIsTrial(0);
                user.setStatus(1);
                user.setInviteCode(generateInviteCode());
                user.setLastLoginTime(LocalDateTime.now());
                user.setLastLoginIp(ip);
                userMapper.insert(user);
            }

            // 创建绑定记录
            binding = new BotTelegramUser();
            binding.setTelegramId(dto.getTelegramId());
            binding.setUserId(user.getId());
            binding.setUsername(dto.getUsername() != null ? dto.getUsername() : "");
            binding.setFirstName(dto.getFirstName() != null ? dto.getFirstName() : "");
            binding.setLastName(dto.getLastName() != null ? dto.getLastName() : "");
            binding.setPhotoUrl(dto.getPhotoUrl() != null ? dto.getPhotoUrl() : "");
            binding.setLanguageCode(dto.getLanguageCode() != null ? dto.getLanguageCode() : "");
            binding.setIsPremium(0);
            binding.setInviteCode(dto.getInviteCode() != null ? dto.getInviteCode() : "");
            binding.setCreateTime(LocalDateTime.now());
            binding.setUpdateTime(LocalDateTime.now());
            botTelegramUserMapper.insert(binding);
        }

        return buildLoginResponse(user);
    }

    /**
     * 构建登录响应（与 AuthService 保持一致）
     */
    private Map<String, Object> buildLoginResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("balance", user.getBalance());
        data.put("vipLevel", user.getVipLevel());
        data.put("isTrial", user.getIsTrial());
        data.put("createTime", user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        return data;
    }

    private void updateBindingInfo(BotTelegramUser binding, TelegramAuthDTO dto) {
        boolean changed = false;
        if (dto.getUsername() != null && !dto.getUsername().equals(binding.getUsername())) {
            binding.setUsername(dto.getUsername());
            changed = true;
        }
        if (dto.getFirstName() != null && !dto.getFirstName().equals(binding.getFirstName())) {
            binding.setFirstName(dto.getFirstName());
            changed = true;
        }
        if (dto.getLastName() != null && !dto.getLastName().equals(binding.getLastName())) {
            binding.setLastName(dto.getLastName());
            changed = true;
        }
        if (dto.getPhotoUrl() != null && !dto.getPhotoUrl().equals(binding.getPhotoUrl())) {
            binding.setPhotoUrl(dto.getPhotoUrl());
            changed = true;
        }
        if (dto.getLanguageCode() != null && !dto.getLanguageCode().equals(binding.getLanguageCode())) {
            binding.setLanguageCode(dto.getLanguageCode());
            changed = true;
        }
        if (changed) {
            binding.setUpdateTime(LocalDateTime.now());
            botTelegramUserMapper.updateById(binding);
        }
    }

    private String buildNickname(TelegramAuthDTO dto) {
        if (dto.getFirstName() != null && !dto.getFirstName().isEmpty()) {
            String nick = dto.getFirstName();
            if (dto.getLastName() != null && !dto.getLastName().isEmpty()) {
                nick += dto.getLastName();
            }
            return nick.length() > 20 ? nick.substring(0, 20) : nick;
        }
        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            return dto.getUsername();
        }
        return "TG用户" + dto.getTelegramId();
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
