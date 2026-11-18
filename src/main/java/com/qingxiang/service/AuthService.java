package com.qingxiang.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingxiang.dto.LoginRequest;
import com.qingxiang.dto.LoginResult;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.dto.RegisterRequest;
import com.qingxiang.entity.Notification;
import com.qingxiang.entity.SysRole;
import com.qingxiang.entity.SysUser;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.mapper.NotificationMapper;
import com.qingxiang.mapper.SysUserMapper;
import com.qingxiang.util.PasswordUtil;
import com.qingxiang.vo.LoginVO;
import com.qingxiang.vo.RegisterVO;
import com.qingxiang.vo.UserVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 注册与登录：密码只以 BCrypt 形态落库；登录结果由控制器写入会话。
 */
@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    public AuthService(SysUserMapper userMapper,
                       NotificationMapper notificationMapper,
                       UserService userService) {
        this.userMapper = userMapper;
        this.notificationMapper = notificationMapper;
        this.userService = userService;
    }

    /** 注册默认创作者（含阅读能力）。 */
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (userService.findByUsername(username) != null) {
            throw new BusinessException(400, "用户名已被占用");
        }
        SysRole creator = userService.findRoleByCode("CREATOR");
        if (creator == null) {
            throw new BusinessException(500, "角色配置缺失，请联系管理员");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setSalt("");
        user.setNickname(request.getNickname().trim());
        user.setFollowerCount(0);
        user.setFollowingCount(0);
        user.setRoleId(creator.getId());
        user.setStatus(1);
        user.setSessionVersion(0);
        user.setDeleted(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册同名账号：唯一键兜底，与预检同一文案
            throw new BusinessException(400, "用户名已被占用");
        }

        RegisterVO vo = new RegisterVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRoleCode(creator.getCode());
        return vo;
    }

    /** 校验密码与账号状态，返回会话所需的登录态。 */
    public LoginResult login(LoginRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        SysUser user = userService.findByUsername(username);
        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码不正确");
        }
        if (!Objects.equals(user.getStatus(), 1)) {
            throw new BusinessException(403, "账号已停用，请联系审核者");
        }

        SysRole role = userService.findRole(user.getRoleId());
        if (role == null) {
            throw new BusinessException(500, "角色配置缺失，请联系管理员");
        }
        List<String> permissions = userService.permissionCodes(role.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getNickname(),
                role.getId(), role.getCode(), user.getSessionVersion(), Set.copyOf(permissions));
        return new LoginResult(loginUser, toVO(user, role), permissions);
    }

    public LoginVO toLoginVO(LoginResult result, String csrfToken) {
        LoginVO vo = new LoginVO();
        vo.setUser(result.user());
        vo.setPermissions(result.permissions());
        vo.setCsrfToken(csrfToken);
        vo.setUnreadCount(unreadCount(result.loginUser().getId()));
        return vo;
    }

    /** 当前用户信息：从库里重读，避免会话里的昵称 / 角色与库脱节。 */
    public LoginVO profile(LoginUser loginUser, String csrfToken) {
        SysUser user = userService.findById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(401, "会话已失效，请重新登录");
        }
        SysRole role = userService.findRole(user.getRoleId());
        if (role == null) {
            throw new BusinessException(500, "角色配置缺失，请联系管理员");
        }

        LoginVO vo = new LoginVO();
        vo.setUser(toVO(user, role));
        vo.setPermissions(userService.permissionCodes(role.getId()));
        vo.setCsrfToken(csrfToken);
        vo.setUnreadCount(unreadCount(user.getId()));
        return vo;
    }

    public long unreadCount(Long userId) {
        Long count = notificationMapper.selectCount(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getIsRead, 0));
        return count == null ? 0 : count;
    }

    private UserVO toVO(SysUser user, SysRole role) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setBio(user.getBio());
        vo.setRoleCode(role.getCode());
        vo.setRoleName(role.getName());
        vo.setStatus(Objects.equals(user.getStatus(), 1) ? "ACTIVE" : "DISABLED");
        vo.setFollowerCount(user.getFollowerCount() == null ? 0 : user.getFollowerCount());
        vo.setFollowingCount(user.getFollowingCount() == null ? 0 : user.getFollowingCount());
        return vo;
    }
}
