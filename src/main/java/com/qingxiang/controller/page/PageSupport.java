package com.qingxiang.controller.page;

import com.qingxiang.util.CsrfUtil;
import com.qingxiang.util.CurrentUser;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

final class PageSupport {

    private PageSupport() {
    }

    static void common(Model model, HttpServletRequest request) {
        model.addAttribute("loginUser", CurrentUser.get());
        model.addAttribute("csrfToken", CsrfUtil.ensure(request.getSession(true)));
    }
}
