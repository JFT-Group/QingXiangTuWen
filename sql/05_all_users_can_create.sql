-- ============================================================
-- 所有登录用户都具备创作能力；REVIEWER 只是额外拥有审核台
-- 依赖：先执行 sql/04_seed.sql
-- 执行：mysql ... < sql/05_all_users_can_create.sql
-- ============================================================
SET NAMES utf8mb4;
USE qingxiang;

INSERT IGNORE INTO sys_permission (role_id, code, name)
SELECT r.id, p.code, p.name
FROM sys_role r
JOIN (
    SELECT 'article:create' AS code, '新建内容' AS name
    UNION ALL SELECT 'article:update', '编辑自己的内容'
    UNION ALL SELECT 'article:delete', '删除自己的内容'
    UNION ALL SELECT 'article:submit', '提交审核 / 撤回'
) p
WHERE r.code IN ('READER', 'CREATOR', 'REVIEWER');
