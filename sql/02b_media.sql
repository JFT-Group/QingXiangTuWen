-- ============================================================
-- 轻享图文 · 媒体内容增量（已有库执行一次）
-- 在 sql/02_content.sql 之后执行；新库可跳过。
-- ============================================================
SET NAMES utf8mb4;
USE qingxiang;

ALTER TABLE article
    ADD COLUMN media_type VARCHAR(10) NOT NULL DEFAULT 'TEXT' AFTER content_type,
    ADD COLUMN media_urls TEXT NULL AFTER media_type,
    ADD COLUMN cover_url VARCHAR(512) NULL AFTER media_urls;
