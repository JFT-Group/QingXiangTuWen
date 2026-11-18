package com.qingxiang.controller.page;

import com.qingxiang.dto.LoginUser;
import com.qingxiang.service.AuthService;
import com.qingxiang.util.CsrfUtil;
import com.qingxiang.util.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 账号中心：展示当前身份、角色与权限码，是切片 1 的登录后落地页。
 */
@Controller
public class MePageController {

    private final AuthService authService;

    public MePageController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public String me(HttpServletRequest request, Model model) {
        LoginUser loginUser = CurrentUser.get();
        String csrfToken = CsrfUtil.ensure(request.getSession(true));
        model.addAttribute("account", authService.profile(loginUser, csrfToken));
        return "me";
    }
}
