package com.qingxiang.controller.page;

import com.qingxiang.dto.LoginRequest;
import com.qingxiang.dto.LoginResult;
import com.qingxiang.dto.RegisterRequest;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.service.AuthService;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.util.SessionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 登录 / 注册 / 退出的 JSP 流程。
 */
@Controller
public class AuthPageController {

    private final AuthService authService;

    public AuthPageController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return CurrentUser.get() == null ? "login" : "redirect:/discover";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        Model model) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        try {
            LoginResult result = authService.login(loginRequest);
            SessionUtil.start(request, result.loginUser());
            return "redirect:/discover";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        if (CurrentUser.get() != null) {
            return "redirect:/discover";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterRequest form,
                           BindingResult binding,
                           HttpServletRequest request,
                           Model model) {
        if (binding.hasErrors()) {
            var error = binding.getFieldError();
            model.addAttribute("error", error == null ? "请检查输入" : error.getDefaultMessage());
            return "register";
        }
        try {
            authService.register(form);

            // 注册成功直接建立会话，不再让用户回登录页重输一次
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(form.getUsername());
            loginRequest.setPassword(form.getPassword());
            LoginResult result = authService.login(loginRequest);
            SessionUtil.start(request, result.loginUser());
            return "redirect:/discover";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        SessionUtil.invalidate(request);
        return "redirect:/login";
    }
}
