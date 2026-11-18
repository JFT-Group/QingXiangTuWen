package com.qingxiang.dto;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 会话里的登录态：只放展示与鉴权必需字段，不放口令。
 */
@Getter
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String SESSION_KEY = "loginUser";

    private final Long id;
    private final String username;
    private final String nickname;
    private final Long roleId;
    private final String roleCode;
    private final int sessionVersion;
    private final Set<String> permissions;

    public LoginUser(Long id, String username, String nickname,
                     Long roleId, String roleCode, int sessionVersion, Set<String> permissions) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.sessionVersion = sessionVersion;
        this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public boolean hasPermission(String code) {
        return code != null && permissions.contains(code);
    }
}
