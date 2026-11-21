package com.qingxiang.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 修改个人资料。字段为 null 表示不修改；空串表示清空（昵称不允许清空）。
 */
@Data
public class ProfileUpdateRequest {

    @Size(max = 50, message = "昵称最多 50 字")
    private String nickname;

    /** 只允许本站上传接口返回的图片地址。 */
    @Size(max = 512, message = "头像地址过长")
    private String avatarUrl;

    @Size(max = 200, message = "简介最多 200 字")
    private String bio;
}
