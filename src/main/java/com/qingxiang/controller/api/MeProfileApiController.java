package com.qingxiang.controller.api;

import com.qingxiang.dto.LoginUser;
import com.qingxiang.dto.ProfileUpdateRequest;
import com.qingxiang.dto.Result;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.service.AuthService;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.util.SessionUtil;
import com.qingxiang.vo.UserVO;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 个人中心资料接口。路径按接口文档 §4.1：PUT /api/v1/me/profile。
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeProfileApiController {

    private final AuthService authService;

    public MeProfileApiController(AuthService authService) {
        this.authService = authService;
    }

    @PutMapping("/profile")
    public Result<UserVO> update(@Valid @RequestBody ProfileUpdateRequest request,
                                 HttpServletRequest httpRequest) {
        LoginUser loginUser = CurrentUser.get();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录");
        }
        UserVO user = authService.updateProfile(loginUser, request);
        SessionUtil.updateNickname(httpRequest, user.getNickname());
        return Result.ok(user);
    }
}
