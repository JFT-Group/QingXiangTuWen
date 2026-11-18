package com.qingxiang.vo;

import lombok.Data;

/**
 * 对外用户信息；不带 password / salt / sessionVersion。
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;

    private String bio;

    private String roleCode;

    private String roleName;

    /** ACTIVE / DISABLED。 */
    private String status;

    private Integer followerCount;

    private Integer followingCount;
}
