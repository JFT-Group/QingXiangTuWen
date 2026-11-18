package com.qingxiang.aspect.permission;

import com.qingxiang.dto.LoginUser;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.util.CurrentUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Controller 权限切面：拦截器管“有没有登录”，本切面管“有没有权限码”。
 *
 * <p>必须由 SpringMvcConfig 以 @Bean 注册在子容器；放父容器会静默失效。
 * 资源归属（作者只能改自己的内容）仍在 Service 复核。</p>
 */
@Aspect
public class PermissionAspect {

    @Around("@annotation(com.qingxiang.aspect.permission.RequirePermission)")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        LoginUser user = CurrentUser.get();
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequirePermission permission = signature.getMethod().getAnnotation(RequirePermission.class);
        if (permission == null || !user.hasPermission(permission.value())) {
            throw new BusinessException(403, "无权限执行该操作");
        }
        return joinPoint.proceed();
    }
}
