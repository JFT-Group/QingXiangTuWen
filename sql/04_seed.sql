-- ============================================================
-- 轻享图文 · 模块 A 种子数据
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 < sql/04_seed.sql
-- 演示账号：reviewer / creator / reader，口令均为 123456（BCrypt cost 10）
-- ============================================================
SET NAMES utf8mb4;
USE qingxiang;

INSERT INTO sys_role (id, code, name, description) VALUES
    (1, 'READER', '阅读者', '浏览、搜索、点赞收藏、评论、关注'),
    (2, 'CREATOR', '创作者', '阅读者能力 + 图文创作与提交审核'),
    (3, 'REVIEWER', '审核者', '阅读者能力 + 审核、分类、用户、日志、评论治理');

INSERT INTO sys_permission (role_id, code, name) VALUES
    (2, 'article:create', '新建内容'),
    (2, 'article:update', '编辑自己的内容'),
    (2, 'article:delete', '删除自己的内容'),
    (2, 'article:submit', '提交审核 / 撤回'),
    (3, 'article:review', '通过 / 驳回'),
    (3, 'article:unpublish', '下架 / 重新上架'),
    (3, 'article:batch', '批量审核'),
    (3, 'comment:manage', '评论治理'),
    (3, 'category:manage', '分类管理'),
    (3, 'user:manage', '用户管理'),
    (3, 'log:view', '查看操作日志');

-- 同一口令 123456；BCrypt 自带随机盐，salt 列保留空串兼容
INSERT INTO sys_user (id, username, password, salt, nickname, bio, role_id, status, session_version) VALUES
    (1, 'reviewer', '$2a$10$0xnIlqgdW2N3NugkRALFhOYq3hZ9mneo43Fhq7hI.XAqHLza3NgMW', '', '审核员小轻', '负责内容审核与分类治理', 3, 1, 0),
    (2, 'creator',  '$2a$10$0xnIlqgdW2N3NugkRALFhOYq3hZ9mneo43Fhq7hI.XAqHLza3NgMW', '', '创作者小享', '记录生活里的好物与好去处', 2, 1, 0),
    (3, 'reader',   '$2a$10$0xnIlqgdW2N3NugkRALFhOYq3hZ9mneo43Fhq7hI.XAqHLza3NgMW', '', '阅读者小图', '随便看看', 1, 1, 0);
