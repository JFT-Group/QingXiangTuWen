package com.qingxiang.interceptor;

import com.qingxiang.dto.LoginUser;
import com.qingxiang.service.UserService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录校验 + 会话版本校验。
 *
 * <p>“哪些路径不拦”由 SpringMvcConfig#addInterceptors 集中声明。每次请求回库核对
 * status 与 session_version：封禁、改密、改角色后旧会话立即失效。</p>
 */
public class LoginInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object attribute = session == null ? null : session.getAttribute(LoginUser.SESSION_KEY);
        LoginUser loginUser = attribute instanceof LoginUser user ? user : null;

        if (loginUser != null
                && !userService.isSessionValid(loginUser.getId(), loginUser.getSessionVersion())) {
            session.invalidate();
            loginUser = null;
        }

        if (loginUser != null) {
            return true;
        }

        if (isApi(request)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
        return false;
    }

    private boolean isApi(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.startsWith("/api/");
    }
}
