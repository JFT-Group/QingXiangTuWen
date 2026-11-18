-- ============================================================
-- 轻享图文 · M1 模块 A（账号、权限与治理）建表
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 < sql/01_core.sql
-- B/C 的表由 02_content.sql / 03_social.sql 追加；本脚本只负责自己的表。
-- ============================================================
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS qingxiang
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE qingxiang;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- sys_role 角色
-- ------------------------------------------------------------
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(20)  NOT NULL COMMENT 'READER / CREATOR / REVIEWER',
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200) NOT NULL DEFAULT '',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色';

-- ------------------------------------------------------------
-- sys_user 账号（M1 单角色：role_id 直连 sys_role）
-- ------------------------------------------------------------
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL COMMENT '登录名',
    password        VARCHAR(72)  NOT NULL COMMENT 'BCrypt 哈希',
    salt            VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '兼容位；BCrypt 自带盐',
    nickname        VARCHAR(50)  NOT NULL,
    avatar_url      VARCHAR(512) NULL,
    bio             VARCHAR(200) NULL,
    follower_count  INT UNSIGNED NOT NULL DEFAULT 0,
    following_count INT UNSIGNED NOT NULL DEFAULT 0,
    role_id         BIGINT       NOT NULL,
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 / 0 停用',
    session_version INT          NOT NULL DEFAULT 0 COMMENT '封禁/改密/改角色时 +1',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_user_role (role_id),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '账号';

-- ------------------------------------------------------------
-- sys_permission 角色权限（角色直连权限码）
-- ------------------------------------------------------------
CREATE TABLE sys_permission (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    role_id     BIGINT      NOT NULL,
    code        VARCHAR(50) NOT NULL COMMENT '如 article:review',
    name        VARCHAR(50) NOT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_id, code),
    KEY idx_permission_role (role_id),
    CONSTRAINT fk_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色权限';

-- ------------------------------------------------------------
-- operation_log 操作日志（只追加；当前切片先建表，写入口在切片 2）
-- ------------------------------------------------------------
CREATE TABLE operation_log (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    operator_id BIGINT        NOT NULL,
    action      VARCHAR(50)   NOT NULL COMMENT 'ARTICLE_APPROVE / USER_ROLE ...',
    target_type VARCHAR(20)   NOT NULL COMMENT 'ARTICLE / USER / CATEGORY / COMMENT',
    target_id   BIGINT        NOT NULL DEFAULT 0,
    result      VARCHAR(10)   NOT NULL COMMENT 'SUCCESS / FAIL',
    reason      VARCHAR(500)  NULL,
    detail      VARCHAR(2000) NULL,
    ip          VARCHAR(45)   NULL,
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operator_time (operator_id, create_time),
    KEY idx_target (target_type, target_id),
    KEY idx_log_time (create_time),
    CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志';

-- ------------------------------------------------------------
-- notification 站内通知（不做逻辑删除）
-- ------------------------------------------------------------
CREATE TABLE notification (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    receiver_id BIGINT       NOT NULL,
    actor_id    BIGINT       NULL COMMENT '系统通知可空',
    type        VARCHAR(30)  NOT NULL COMMENT 'ARTICLE_APPROVED / COMMENT / LIKE ...',
    target_type VARCHAR(20)  NOT NULL COMMENT 'ARTICLE / COMMENT / USER',
    target_id   BIGINT       NOT NULL DEFAULT 0,
    content     VARCHAR(300) NOT NULL DEFAULT '',
    is_read     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notify_receiver (receiver_id, is_read, create_time),
    KEY idx_notify_target (target_type, target_id),
    CONSTRAINT fk_notify_receiver FOREIGN KEY (receiver_id) REFERENCES sys_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_notify_actor FOREIGN KEY (actor_id) REFERENCES sys_user (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '站内通知';
