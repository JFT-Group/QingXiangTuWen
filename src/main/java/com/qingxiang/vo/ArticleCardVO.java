package com.qingxiang.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信息流卡片：封面标题 + 作者 + 分类 + 互动计数。
 */
@Data
public class ArticleCardVO {

    private Long id;
    private String title;
    private String coverColor;
    private String mediaType;
    private String coverUrl;
    private String status;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private String rejectReason;
    private LocalDateTime submitTime;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
