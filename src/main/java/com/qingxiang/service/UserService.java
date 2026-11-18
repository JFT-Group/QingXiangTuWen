package com.qingxiang.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingxiang.entity.SysPermission;
import com.qingxiang.entity.SysRole;
import com.qingxiang.entity.SysUser;
import com.qingxiang.mapper.SysPermissionMapper;
import com.qingxiang.mapper.SysRoleMapper;
import com.qingxiang.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 账号 / 角色 / 权限的只读查询，供认证、拦截器与会话校验复用。
 */
@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    public UserService(SysUserMapper userMapper,
                       SysRoleMapper roleMapper,
                       SysPermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public SysUser findByUsername(String username) {
        if (username == null) {
            return null;
        }
        return userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0));
    }

    public SysUser findById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getId, id)
                .eq(SysUser::getDeleted, 0));
    }

    public SysRole findRole(Long roleId) {
        return roleId == null ? null : roleMapper.selectById(roleId);
    }

    public SysRole findRoleByCode(String code) {
        return roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, code));
    }

    public List<String> permissionCodes(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return permissionMapper.selectList(Wrappers.<SysPermission>lambdaQuery()
                        .eq(SysPermission::getRoleId, roleId)
                        .orderByAsc(SysPermission::getCode))
                .stream()
                .map(SysPermission::getCode)
                .toList();
    }

    /**
     * 每次请求校验：账号未删除、未停用、session_version 与登录时一致。
     * 封禁 / 改密 / 改角色后版本 +1，旧会话立即失效。
     */
    public boolean isSessionValid(Long userId, Integer sessionVersion) {
        if (userId == null || sessionVersion == null) {
            return false;
        }
        SysUser current = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getStatus, SysUser::getSessionVersion)
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDeleted, 0));
        return current != null
                && Objects.equals(current.getStatus(), 1)
                && Objects.equals(current.getSessionVersion(), sessionVersion);
    }
}
