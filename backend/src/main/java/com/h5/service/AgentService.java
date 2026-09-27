package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.User;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 代理服务（代理中心/团队/返佣）
 */
@Service
public class AgentService {

    @Autowired private UserMapper userMapper;

    /**
     * 获取代理信息
     */
    public Map<String, Object> getAgentInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Collections.emptyMap();

        // 团队人数（邀请码匹配 invited_by 或 invite_code）
        Long teamCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getInvitedBy, userId)
        );

        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", user.getInviteCode());
        data.put("teamCount", teamCount);
        data.put("vipLevel", user.getVipLevel());
        data.put("balance", user.getBalance());
        return data;
    }

    /**
     * 获取团队成员列表
     */
    public Map<String, Object> getTeamMembers(Long userId, int page, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> pageResult =
                userMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize),
                        new LambdaQueryWrapper<User>()
                                .eq(User::getInvitedBy, userId)
                                .orderByDesc(User::getCreateTime)
                );

        List<Map<String, Object>> members = new ArrayList<>();
        for (User u : pageResult.getRecords()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("vipLevel", u.getVipLevel());
            m.put("createTime", u.getCreateTime());
            members.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", members);
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    /**
     * 代理推广链接
     */
    public Map<String, Object> getPromoteLink(Long userId, String domain) {
        User user = userMapper.selectById(userId);
        if (user == null) return Collections.emptyMap();

        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", user.getInviteCode());
        data.put("registerLink", domain + "/register?invite=" + user.getInviteCode());
        data.put("telegramLink", "https://t.me/share/url?url=" + domain + "/register?invite=" + user.getInviteCode());
        return data;
    }
}
