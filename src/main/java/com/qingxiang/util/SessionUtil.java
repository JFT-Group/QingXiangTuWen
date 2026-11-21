package com.qingxiang.util;

import com.qingxiang.dto.LoginUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 登录后的会话收口：换 sessionId 防会话固定，写入登录态并轮换 CSRF token。
 */
public final class SessionUtil {

    private SessionUtil() {
    }

    public static void start(HttpServletRequest request, LoginUser loginUser) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(LoginUser.SESSION_KEY, loginUser);
        CsrfUtil.rotate(session);
    }

    public static void invalidate(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /** 资料修改后同步会话里的昵称；角色/权限/版本保持不变，不轮换 sessionId。 */
    public static void updateNickname(HttpServletRequest request, String nickname) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object attribute = session.getAttribute(LoginUser.SESSION_KEY);
        if (!(attribute instanceof LoginUser user) || nickname == null) {
            return;
        }
        session.setAttribute(LoginUser.SESSION_KEY, new LoginUser(user.getId(), user.getUsername(),
                nickname, user.getRoleId(), user.getRoleCode(), user.getSessionVersion(),
                user.getPermissions()));
    }
}
