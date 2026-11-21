package com.qingxiang.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图文。status 存 TINYINT；deleted 显式过滤；version 由条件更新维护。
 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long authorId;

    private Long categoryId;

    private String title;

    private String content;

    private String contentType;

    /** TEXT / IMAGE / VIDEO。 */
    private String mediaType;

    /** 媒体 URL 的 JSON 数组。 */
    private String mediaUrls;

    /** 图片帖第一张作为封面；视频/文本为空。 */
    private String coverUrl;

    private String coverColor;

    /** 0 草稿 / 1 待审核 / 2 已发布 / 3 已驳回 / 4 已下架。 */
    private Integer status;

    private String rejectReason;

    private LocalDateTime submitTime;

    private LocalDateTime reviewTime;

    private Long reviewerId;

    private LocalDateTime publishTime;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer commentCount;

    @Version
    private Integer version;

    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
