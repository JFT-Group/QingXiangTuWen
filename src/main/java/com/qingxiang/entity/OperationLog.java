package com.qingxiang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * operation_log 是 A 的表；B 只通过 OperationLogService 追加，不直接暴露给页面。
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;

    private String action;

    private String targetType;

    private Long targetId;

    private String result;

    private String reason;

    private String detail;

    private String ip;

    private LocalDateTime createTime;
}
