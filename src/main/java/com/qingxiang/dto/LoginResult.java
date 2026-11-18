package com.qingxiang.dto;

import com.qingxiang.vo.UserVO;

import java.util.List;

/**
 * 认证成功后服务层交给控制器的三件套。
 */
public record LoginResult(LoginUser loginUser, UserVO user, List<String> permissions) {
}
