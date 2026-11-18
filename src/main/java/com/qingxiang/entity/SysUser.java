package com.qingxiang.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号。M1 单角色：role_id 直连 sys_role；deleted 显式过滤，不用 @TableLogic。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希；任何 VO 都不得带出。 */
    private String password;

    private String salt;

    private String nickname;

    private String avatarUrl;

    private String bio;

    private Integer followerCount;

    private Integer followingCount;

    private Long roleId;

    /** 1 正常 / 0 停用。 */
    private Integer status;

    /** 封禁、改密、改角色时 +1；旧会话立即失效。 */
    private Integer sessionVersion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer deleted;
}
