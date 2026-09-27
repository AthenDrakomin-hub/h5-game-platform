package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.dto.LoginDTO;
import com.h5.dto.RegisterDTO;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import com.h5.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${site.trial_balance:2000}")
    private String trialBalance;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 登录
     */
    public Map<String, Object> login(LoginDTO dto, String ip) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername())
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        return buildLoginResponse(user);
    }

    /**
     * 注册
     */
    @Transactional
    public Map<String, Object> register(RegisterDTO dto, String ip) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null && !dto.getNickname().isEmpty()
                ? dto.getNickname() : dto.getUsername());
        user.setBalance(BigDecimal.ZERO);
        user.setFrozenBalance(BigDecimal.ZERO);
        user.setVipLevel(1);
        user.setIsTrial(0);
        user.setStatus(1);
        user.setInviteCode(generateInviteCode());
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.insert(user);

        return buildLoginResponse(user);
    }

    /**
     * 试玩登录
     */
    @Transactional
    public Map<String, Object> trialLogin(String ip) {
        // 生成随机试玩账号
        String username = "trial_" + UUID.randomUUID().toString().substring(0, 8).toLowerCase();
        String password = UUID.randomUUID().toString().substring(0, 12);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname("试玩玩家");
        user.setBalance(new BigDecimal(trialBalance));
        user.setFrozenBalance(BigDecimal.ZERO);
        user.setVipLevel(1);
        user.setIsTrial(1);
        user.setStatus(1);
        user.setInviteCode(generateInviteCode());
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.insert(user);

        return buildLoginResponse(user);
    }

    /**
     * 构建登录响应（匹配前端期望结构）
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

    /**
     * 生成邀请码
     */
    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
