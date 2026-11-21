package com.qingxiang.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CategorySaveRequest {

    private Long parentId = 0L;

    @NotBlank(message = "请输入分类名称")
    @Size(max = 30, message = "分类名称最多 30 字")
    private String name;

    private Integer sort = 0;
}
