package com.qingxiang.interceptor;

import com.qingxiang.util.CsrfUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 非 GET/HEAD/OPTIONS 请求必须带正确 token；登录、注册在注册处排除。
 */
public class CsrfInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object expected = session == null ? null : session.getAttribute(CsrfUtil.SESSION_TOKEN);
        String actual = request.getHeader(CsrfUtil.HEADER);
        if (actual == null || actual.isEmpty()) {
            actual = request.getParameter(CsrfUtil.PARAM);
        }

        if (expected instanceof String token && CsrfUtil.validate(token, actual)) {
            return true;
        }

        response.setStatus(403);
        if (isApi(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"页面已过期，请刷新后重试\",\"data\":null}");
        } else {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>403</h1><p>页面已过期，请刷新后重试</p>");
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
