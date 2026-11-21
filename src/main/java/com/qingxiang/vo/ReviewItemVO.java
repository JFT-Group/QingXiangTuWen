package com.qingxiang.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核工作台列表行：只给识别与选择所需字段。
 */
@Data
public class ReviewItemVO {

    private Long id;
    private String title;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private String coverColor;
    private LocalDateTime submitTime;
    private LocalDateTime updateTime;
}
