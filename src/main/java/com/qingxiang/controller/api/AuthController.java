package com.qingxiang.controller.api;

import com.qingxiang.dto.LoginRequest;
import com.qingxiang.dto.LoginResult;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.dto.RegisterRequest;
import com.qingxiang.dto.Result;
import com.qingxiang.service.AuthService;
import com.qingxiang.util.CsrfUtil;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.util.SessionUtil;
import com.qingxiang.vo.LoginVO;
import com.qingxiang.vo.RegisterVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

/**
 * 认证接口：注册、登录、退出、当前用户、刷新 CSRF。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<RegisterVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResult result = authService.login(request);
        SessionUtil.start(httpRequest, result.loginUser());
        String csrfToken = (String) httpRequest.getSession().getAttribute(CsrfUtil.SESSION_TOKEN);
        return Result.ok(authService.toLoginVO(result, csrfToken));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        SessionUtil.invalidate(httpRequest);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<LoginVO> me(HttpServletRequest httpRequest) {
        LoginUser loginUser = CurrentUser.get();
        HttpSession session = httpRequest.getSession(true);
        return Result.ok(authService.profile(loginUser, CsrfUtil.ensure(session)));
    }

    @GetMapping("/csrf")
    public Result<Map<String, String>> csrf(HttpServletRequest httpRequest) {
        String token = CsrfUtil.rotate(httpRequest.getSession(true));
        return Result.ok(Map.of("csrfToken", token));
    }
}
