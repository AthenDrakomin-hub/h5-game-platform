package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.User;
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
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AgentService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock private UserMapper userMapper;
    @InjectMocks private AgentService agentService;

    private User agentUser;
    private User member1;
    private User member2;

    @BeforeEach
    void setUp() {
        agentUser = new User();
        agentUser.setId(1L);
        agentUser.setUsername("agent");
        agentUser.setInviteCode("INV001");
        agentUser.setBalance(new BigDecimal("5000"));

        member1 = new User();
        member1.setId(2L);
        member1.setUsername("member1");
        member1.setInvitedBy(1L);
        member1.setVipLevel(1);

        member2 = new User();
        member2.setId(3L);
        member2.setUsername("member2");
        member2.setInvitedBy(1L);
        member2.setVipLevel(2);
    }

    @Nested
    @DisplayName("代理信息")
    class AgentInfo {
        @Test
        @DisplayName("获取代理信息含团队人数")
        void testGetAgentInfo() {
            when(userMapper.selectById(1L)).thenReturn(agentUser);
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

            Map<String, Object> info = agentService.getAgentInfo(1L);
            assertEquals("INV001", info.get("inviteCode"));
            assertEquals(2L, info.get("teamCount"));
        }

        @Test
        @DisplayName("用户不存在返回空Map")
        void testUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);
            Map<String, Object> info = agentService.getAgentInfo(999L);
            assertTrue(info.isEmpty());
        }
    }

    @Nested
    @DisplayName("团队成员")
    class TeamMembers {
        @Test
        @DisplayName("分页查询团队成员")
        void testGetTeamMembers() {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(member1, member2));
            mockPage.setTotal(2);
            when(userMapper.selectPage(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
                    any(LambdaQueryWrapper.class))).thenReturn(mockPage);

            Map<String, Object> result = agentService.getTeamMembers(1L, 1, 10);
            assertEquals(2L, result.get("total"));
            assertNotNull(result.get("list"));
        }
    }

    @Nested
    @DisplayName("推广链接")
    class PromoteLink {
        @Test
        @DisplayName("生成推广链接")
        void testGetPromoteLink() {
            when(userMapper.selectById(1L)).thenReturn(agentUser);
            Map<String, Object> result = agentService.getPromoteLink(1L, "https://h5.test.com");
            assertEquals("INV001", result.get("inviteCode"));
            assertTrue(result.get("registerLink").toString().contains("INV001"));
        }
    }
}
