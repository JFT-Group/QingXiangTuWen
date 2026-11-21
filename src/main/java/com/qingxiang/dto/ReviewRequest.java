package com.qingxiang.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ReviewRequest {

    @NotBlank(message = "请填写驳回理由")
    @Size(max = 500, message = "理由最多 500 字")
    private String reason;
}
