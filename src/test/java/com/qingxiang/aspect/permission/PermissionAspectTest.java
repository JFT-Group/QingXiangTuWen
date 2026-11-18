package com.qingxiang.aspect.permission;

import com.qingxiang.dto.LoginUser;
import com.qingxiang.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionAspectTest {

    private final PermissionAspect aspect = new PermissionAspect();
    private ProceedingJoinPoint joinPoint;
    private MethodSignature signature;

    @BeforeEach
    void setUp() throws Exception {
        joinPoint = mock(ProceedingJoinPoint.class);
        signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(Sample.class.getMethod("secured"));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void anonymousRequestGets401() {
        bindSession(null);

        BusinessException e = assertThrows(BusinessException.class, () -> aspect.check(joinPoint));
        assertEquals(401, e.getStatus());
    }

    @Test
    void missingPermissionGets403() {
        bindSession(new LoginUser(2L, "creator", "小享", 2L, "CREATOR", 0, Set.of("article:create")));

        BusinessException e = assertThrows(BusinessException.class, () -> aspect.check(joinPoint));
        assertEquals(403, e.getStatus());
    }

    @Test
    void matchingPermissionProceeds() throws Throwable {
        bindSession(new LoginUser(1L, "reviewer", "审核员", 3L, "REVIEWER", 0, Set.of("user:manage")));
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.check(joinPoint));
        verify(joinPoint).proceed();
    }

    private void bindSession(LoginUser user) {
        MockHttpSession session = new MockHttpSession();
        if (user != null) {
            session.setAttribute(LoginUser.SESSION_KEY, user);
        }
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    static class Sample {
        @RequirePermission("user:manage")
        public void secured() {
        }
    }
}
