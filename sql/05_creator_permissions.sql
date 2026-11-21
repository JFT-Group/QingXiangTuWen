-- ============================================================
-- 创作权限只给创作者与审核者；阅读者保持只读。
-- 依赖：先执行 sql/04_seed.sql
-- 执行：mysql ... < sql/05_creator_permissions.sql
-- 可重复执行：先让阅读者会话失效并移除误授的文章权限，再补齐创作者/审核者。
-- ============================================================
SET NAMES utf8mb4;
USE qingxiang;

-- 阅读者若曾被授予文章权限，先 +1 会话版本，令其已登录会话立即失效。
UPDATE sys_user u
JOIN sys_role r ON r.id = u.role_id
SET u.session_version = u.session_version + 1
WHERE r.code = 'READER'
  AND EXISTS (
      SELECT 1
      FROM sys_permission p
      WHERE p.role_id = u.role_id
        AND p.code IN ('article:create', 'article:update', 'article:delete', 'article:submit')
  );

DELETE p
FROM sys_permission p
JOIN sys_role r ON r.id = p.role_id
WHERE r.code = 'READER'
  AND p.code IN ('article:create', 'article:update', 'article:delete', 'article:submit');

INSERT IGNORE INTO sys_permission (role_id, code, name)
SELECT r.id, p.code, p.name
FROM sys_role r
JOIN (
    SELECT 'article:create' AS code, '新建内容' AS name
    UNION ALL SELECT 'article:update', '编辑自己的内容'
    UNION ALL SELECT 'article:delete', '删除自己的内容'
    UNION ALL SELECT 'article:submit', '提交审核 / 撤回'
) p
WHERE r.code IN ('CREATOR', 'REVIEWER');
