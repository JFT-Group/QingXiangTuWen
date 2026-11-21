package com.qingxiang.dto;

import lombok.Data;

/**
 * 信息流/我的内容/审核队列共用的查询条件。
 */
@Data
public class ArticleQuery {

    private String keyword;

    private Long categoryId;

    private Long authorId;

    /** latest / oldest；白名单在服务层收敛。 */
    private String sort = "latest";

    /** 我的内容用：DRAFT / PENDING / PUBLISHED / REJECTED / UNPUBLISHED。 */
    private String status;

    /** 服务层由 status 转出的 TINYINT，供 SQL 条件使用。 */
    private Integer statusCode;

    private int page = 1;

    private int size = 10;
}
