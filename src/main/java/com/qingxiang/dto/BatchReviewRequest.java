package com.qingxiang.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class BatchReviewRequest {

    @NotEmpty(message = "请选择要处理的内容")
    @Size(max = 50, message = "一次最多处理 50 条")
    private List<Long> ids;

    @NotNull(message = "请选择审核动作")
    private String action;

    @Size(max = 500, message = "理由最多 500 字")
    private String reason;
}
