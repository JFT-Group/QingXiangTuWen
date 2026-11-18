package com.qingxiang.service;

import com.qingxiang.dto.LoginRequest;
import com.qingxiang.dto.LoginResult;
import com.qingxiang.dto.RegisterRequest;
import com.qingxiang.entity.SysRole;
import com.qingxiang.entity.SysUser;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.mapper.NotificationMapper;
import com.qingxiang.mapper.SysUserMapper;
import com.qingxiang.util.PasswordUtil;
import com.qingxiang.vo.RegisterVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String HASH = PasswordUtil.encode("123456");

    private SysUserMapper userMapper;
    private NotificationMapper notificationMapper;
    private UserService userService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userMapper = mock(SysUserMapper.class);
        notificationMapper = mock(NotificationMapper.class);
        userService = mock(UserService.class);
        authService = new AuthService(userMapper, notificationMapper, userService);
    }

    @Test
    void loginReturnsPermissionsAndSessionVersion() {
        SysUser user = user(2L, "creator", 1, 0);
        when(userService.findByUsername("creator")).thenReturn(user);
        when(userService.findRole(2L)).thenReturn(role(2L, "CREATOR", "创作者"));
        when(userService.permissionCodes(2L)).thenReturn(List.of("article:create", "article:submit"));

        LoginResult result = authService.login(login("creator", "123456"));

        assertEquals(2L, result.loginUser().getId());
        assertEquals("CREATOR", result.loginUser().getRoleCode());
        assertEquals(0, result.loginUser().getSessionVersion());
        assertTrue(result.loginUser().hasPermission("article:create"));
        assertEquals(2, result.permissions().size());
        assertEquals("creator", result.user().getUsername());
    }

    @Test
    void wrongPasswordGets401() {
        when(userService.findByUsername("creator")).thenReturn(user(2L, "creator", 1, 0));

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.login(login("creator", "wrong-pass")));
        assertEquals(401, e.getStatus());
    }

    @Test
    void disabledAccountGets403() {
        when(userService.findByUsername("reader")).thenReturn(user(3L, "reader", 0, 0));

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.login(login("reader", "123456")));
        assertEquals(403, e.getStatus());
    }

    @Test
    void duplicateUsernameGets400() {
        when(userService.findByUsername("creator")).thenReturn(user(2L, "creator", 1, 0));

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.register(register("creator", "小享")));
        assertEquals(400, e.getStatus());
    }

    @Test
    void registerCreatesCreatorAccount() {
        when(userService.findByUsername("newbie")).thenReturn(null);
        when(userService.findRoleByCode("CREATOR")).thenReturn(role(2L, "CREATOR", "创作者"));
        doAnswer(invocation -> {
            invocation.getArgument(0, SysUser.class).setId(9L);
            return 1;
        }).when(userMapper).insert(any(SysUser.class));

        RegisterVO vo = authService.register(register("newbie", "新同学"));

        assertEquals(9L, vo.getId());
        assertEquals("newbie", vo.getUsername());
        assertEquals("CREATOR", vo.getRoleCode());
    }

    private LoginRequest login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private RegisterRequest register(String username, String nickname) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("123456");
        request.setNickname(nickname);
        return request;
    }

    private SysUser user(Long id, String username, int status, int sessionVersion) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(HASH);
        user.setNickname("测试用户");
        user.setRoleId(2L);
        user.setStatus(status);
        user.setSessionVersion(sessionVersion);
        user.setFollowerCount(0);
        user.setFollowingCount(0);
        return user;
    }

    private SysRole role(Long id, String code, String name) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setCode(code);
        role.setName(name);
        return role;
    }
}
