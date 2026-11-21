package com.qingxiang.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 入口路由：统一进入发现页；未登录时由登录拦截器转到登录页。
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/discover";
    }
}
