package com.qingxiang.interceptor;

import com.qingxiang.dto.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiWriteAuthInterceptorTest {

    private final ApiWriteAuthInterceptor interceptor = new ApiWriteAuthInterceptor();

    @AfterEach
    void clearRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void publicGetPasses() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/articles");
        bind(request);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void anonymousWriteGets401BeforeCsrf() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/articles");
        MockHttpServletResponse response = new MockHttpServletResponse();
        bind(request);

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"未登录\""));
    }

    @Test
    void loggedInWritePasses() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/articles");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LoginUser.SESSION_KEY, new LoginUser(2L, "creator", "创作者",
                2L, "CREATOR", 0, Set.of("article:create")));
        request.setSession(session);
        bind(request);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    private void bind(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
