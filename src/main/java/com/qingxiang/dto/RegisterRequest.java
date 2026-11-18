package com.qingxiang.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {

    @NotBlank(message = "请输入用户名")
    @Pattern(regexp = "^[A-Za-z0-9_]{4,50}$", message = "用户名需为 4–50 位字母、数字或下划线")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 72, message = "密码长度需为 6–72 位")
    private String password;

    @NotBlank(message = "请输入昵称")
    @Size(max = 50, message = "昵称最长 50 字")
    private String nickname;
}
