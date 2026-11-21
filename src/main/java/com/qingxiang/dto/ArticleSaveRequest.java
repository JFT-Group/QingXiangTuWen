package com.qingxiang.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 新建/编辑文章入参。标题空串由服务层回退为“未命名”；正文必填在服务层校验。
 */
@Data
public class ArticleSaveRequest {

    @Size(max = 120, message = "标题最多 120 字")
    private String title;

    private String content;

    private Long categoryId;

    /** TEXT / IMAGE / VIDEO。 */
    private String mediaType;

    /** 图片最多 9 张；视频 1 个。 */
    private java.util.List<String> mediaUrls;
}
