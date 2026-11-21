package com.qingxiang.vo;

import lombok.Data;

/**
 * 编辑页专用：只返回作者需要回填的字段。
 */
@Data
public class ArticleEditVO {

    private Long id;
    private String title;
    private String content;
    private String mediaType;
    private String coverUrl;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String mediaUrlsRaw;
    private java.util.List<String> mediaUrls;
    private Long categoryId;
    private String coverColor;
    private String status;
    private String rejectReason;
}
