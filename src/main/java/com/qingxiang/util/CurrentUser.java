package com.qingxiang.util;

import com.qingxiang.dto.LoginUser;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

/**
 * 从当前请求的 HttpSession 取登录态；本项目不引入 Spring Security 过滤器链。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static LoginUser get() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        HttpSession session = servletAttributes.getRequest().getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute(LoginUser.SESSION_KEY);
        return user instanceof LoginUser loginUser ? loginUser : null;
    }
}
