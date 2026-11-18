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
}
