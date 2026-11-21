package com.qingxiang.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 详情：卡片字段 + 正文 + 审核信息 + 互动占位字段。
 */
@Data
public class ArticleDetailVO {

    private Long id;
    private String title;
    private String content;
    private String mediaType;
    private String coverUrl;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String mediaUrlsRaw;
    private java.util.List<String> mediaUrls;
    private String coverColor;
    private String status;
    private String statusLabel;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private Long categoryId;
    private String categoryName;

    private String rejectReason;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;

    private Boolean liked = Boolean.FALSE;
    private Boolean favorited = Boolean.FALSE;
    private Boolean followedAuthor = Boolean.FALSE;

    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
    private String reviewerName;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 审核详情才有；普通详情为 null。 */
    private List<ReviewLogVO> reviewLogs;
}
