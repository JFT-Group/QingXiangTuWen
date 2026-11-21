package com.qingxiang.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewLogVO {

    private Long id;
    private String action;
    private String actionLabel;
    private Long reviewerId;
    private String reviewerName;
    private String reason;
    private String fromStatus;
    private String toStatus;
    private LocalDateTime createTime;
}
