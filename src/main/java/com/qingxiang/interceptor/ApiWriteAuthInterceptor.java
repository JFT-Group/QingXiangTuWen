package com.qingxiang.interceptor;

import com.qingxiang.util.CurrentUser;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API 写请求的登录前置：公开 GET 不受影响；POST/PUT/DELETE 未登录直接 401。
 *
 * <p>登录拦截器为了公开信息流排除了 /api/v1/articles/**，会让匿名写请求先撞上
 * CSRF 返回 403；本拦截器注册在 CSRF 之前，保证契约里的 401。</p>
 */
public class ApiWriteAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if (CurrentUser.get() != null) {
            return true;
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
        return false;
    }
}
