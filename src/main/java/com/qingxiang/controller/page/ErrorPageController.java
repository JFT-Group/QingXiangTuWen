package com.qingxiang.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 容器 404/500 转发到 /error 的友好页。
 */
@Controller
public class ErrorPageController {

    @GetMapping("/error")
    public String error(Model model) {
        if (!model.containsAttribute("code")) {
            model.addAttribute("code", 404);
            model.addAttribute("message", "页面不存在或已被移除");
        }
        return "error";
    }
}
