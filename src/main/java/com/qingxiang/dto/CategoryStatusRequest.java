package com.qingxiang.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CategoryStatusRequest {

    @NotBlank(message = "请选择状态")
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态只能是 ENABLED 或 DISABLED")
    private String status;
}
