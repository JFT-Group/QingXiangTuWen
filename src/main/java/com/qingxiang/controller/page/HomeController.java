package com.qingxiang.controller.page;

import com.qingxiang.util.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 入口路由：登录后去账号页，未登录去登录页。
 * 信息流页由模块 B 在切片 1 接入。
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return CurrentUser.get() == null ? "redirect:/login" : "redirect:/me";
    }
}
