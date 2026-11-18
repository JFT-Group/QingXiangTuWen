package com.qingxiang.vo;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {

    private UserVO user;

    private List<String> permissions;

    private String csrfToken;

    private long unreadCount;
}
