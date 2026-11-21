package com.qingxiang.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_review")
public class ArticleReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long reviewerId;

    /** APPROVE / REJECT / UNPUBLISH / REPUBLISH / DELETE。 */
    private String action;

    private String reason;

    private Integer fromStatus;

    private Integer toStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
