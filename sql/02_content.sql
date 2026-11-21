-- ============================================================
-- 轻享图文 · M1 模块 B（内容、分类与审核）建表
-- 执行顺序：01_core.sql -> 02_content.sql -> 04_seed.sql -> 04_seed_b.sql
-- 本脚本只负责 category / article / article_review，不改 A 的表。
-- ============================================================
SET NAMES utf8mb4;
USE qingxiang;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS article_review;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS category;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- category 多级分类树（parent_id = 0 表示根）
-- ------------------------------------------------------------
CREATE TABLE category (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '0 表示根分类',
    name        VARCHAR(30)  NOT NULL,
    level       TINYINT      NOT NULL DEFAULT 1 COMMENT '1..5',
    sort        INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用 / 0 停用',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_parent_name (parent_id, name),
    KEY idx_category_parent_sort (parent_id, sort, id),
    KEY idx_category_status (status, deleted)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '多级分类';

-- ------------------------------------------------------------
-- article 图文
-- status: 0 草稿 / 1 待审核 / 2 已发布 / 3 已驳回 / 4 已下架
-- ------------------------------------------------------------
CREATE TABLE article (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    author_id      BIGINT        NOT NULL,
    category_id    BIGINT        NULL COMMENT '草稿可暂不选；提交审核前必须是启用的末级分类',
    title          VARCHAR(120)  NOT NULL DEFAULT '未命名',
    content        MEDIUMTEXT    NOT NULL,
    content_type   VARCHAR(20)   NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT / HTML',
    media_type     VARCHAR(10)   NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT / IMAGE / VIDEO',
    media_urls     TEXT          NULL COMMENT '图片/视频 URL 的 JSON 数组；图片最多 9 张，视频 1 个',
    cover_url      VARCHAR(512)  NULL COMMENT '图片帖取第一张；视频/文本为空',
    cover_color    CHAR(7)       NOT NULL DEFAULT '#15803d' COMMENT '自动封面纯色',
    status         TINYINT       NOT NULL DEFAULT 0 COMMENT '0 草稿 / 1 待审核 / 2 已发布 / 3 已驳回 / 4 已下架',
    reject_reason  VARCHAR(500)  NULL COMMENT '最近一次驳回/下架理由',
    submit_time    DATETIME      NULL,
    review_time    DATETIME      NULL,
    reviewer_id    BIGINT        NULL,
    publish_time   DATETIME      NULL,
    like_count     INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT 'C 模块维护，B 只读',
    favorite_count INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT 'C 模块维护，B 只读',
    comment_count  INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT 'C 模块维护，B 只读',
    version        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁/条件更新',
    deleted        TINYINT       NOT NULL DEFAULT 0,
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_article_feed (status, deleted, publish_time, id),
    KEY idx_article_author (author_id, deleted, status, update_time),
    KEY idx_article_category (category_id, status, deleted, publish_time),
    KEY idx_article_review_queue (status, submit_time, id),
    KEY idx_article_title (title),
    CONSTRAINT fk_article_author FOREIGN KEY (author_id) REFERENCES sys_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT,
    CONSTRAINT fk_article_reviewer FOREIGN KEY (reviewer_id) REFERENCES sys_user (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '图文';

-- ------------------------------------------------------------
-- article_review 审核流水（只追加）
-- ------------------------------------------------------------
CREATE TABLE article_review (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    article_id  BIGINT       NOT NULL,
    reviewer_id BIGINT       NOT NULL,
    action      VARCHAR(20)  NOT NULL COMMENT 'APPROVE / REJECT / UNPUBLISH / REPUBLISH / DELETE',
    reason      VARCHAR(500) NULL,
    from_status TINYINT      NOT NULL,
    to_status   TINYINT      NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_review_article (article_id, create_time),
    KEY idx_review_reviewer (reviewer_id, create_time),
    CONSTRAINT fk_review_article FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE RESTRICT,
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES sys_user (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审核流水';
