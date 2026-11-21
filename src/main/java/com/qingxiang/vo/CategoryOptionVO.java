package com.qingxiang.vo;

import lombok.Data;

/**
 * 编辑页分类下拉项：只列启用的末级分类，path 为“美食 / 探店”。
 */
@Data
public class CategoryOptionVO {

    private Long id;
    private String name;
    private String path;
    private Integer level;
}
