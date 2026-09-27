package com.h5.service;

import com.h5.common.BusinessException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserMapper userMapper;
    @InjectMocks private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(new BCryptPasswordEncoder().encode("oldpass"));
        testUser.setBalance(new BigDecimal("1000"));
    }

    @Nested
    @DisplayName("用户信息")
    class UserInfo {
        @Test
        @DisplayName("正常获取用户信息")
        void testGetUserInfo() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            Map<String, Object> info = userService.getUserInfo(1L);
            assertEquals("testuser", info.get("username"));
            assertEquals(new BigDecimal("1000"), info.get("balance"));
        }

        @Test
        @DisplayName("用户不存在抛异常")
        void testUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);
            assertThrows(BusinessException.class, () -> userService.getUserInfo(999L));
        }
    }

    @Nested
    @DisplayName("密码管理")
    class PasswordManagement {
        @Test
        @DisplayName("修改密码成功")
        void testChangePasswordSuccess() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            assertDoesNotThrow(() -> userService.changePassword(1L, "oldpass", "newpass123"));
            verify(userMapper, times(1)).updateById(any(User.class));
        }

        @Test
        @DisplayName("原密码错误抛异常")
        void testWrongOldPassword() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            assertThrows(BusinessException.class, () -> userService.changePassword(1L, "wrongpass", "newpass"));
        }

        @Test
        @DisplayName("新密码太短抛异常")
        void testShortNewPassword() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            assertThrows(BusinessException.class, () -> userService.changePassword(1L, "oldpass", "123"));
        }

        @Test
        @DisplayName("设置资金密码成功")
        void testSetFundPassword() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            assertDoesNotThrow(() -> userService.setFundPassword(1L, "fund123"));
        }
    }

    @Nested
    @DisplayName("资料更新")
    class ProfileUpdate {
        @Test
        @DisplayName("更新昵称成功")
        void testUpdateNickname() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            assertDoesNotThrow(() -> userService.updateProfile(1L, "新昵称", null, null, null));
            assertEquals("新昵称", testUser.getNickname());
        }
    }
}
