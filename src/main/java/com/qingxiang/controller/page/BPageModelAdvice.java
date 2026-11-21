package com.qingxiang.controller.page;

import com.qingxiang.dto.LoginUser;
import com.qingxiang.entity.SysUser;
import com.qingxiang.service.UserService;
import com.qingxiang.util.CurrentUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 给 B 页面补充当前用户头像 URL；A 的 me.jsp 忽略该属性即可。
 */
@ControllerAdvice(basePackages = "com.qingxiang.controller.page")
public class BPageModelAdvice {

    private final UserService userService;

    public BPageModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addAvatar(Model model) {
        LoginUser loginUser = CurrentUser.get();
        if (loginUser == null) {
            return;
        }
        SysUser user = userService.findById(loginUser.getId());
        if (user != null) {
            model.addAttribute("avatarUrl", user.getAvatarUrl());
        }
    }
}
