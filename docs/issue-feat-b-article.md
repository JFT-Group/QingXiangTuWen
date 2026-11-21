> 审计对象：`feat/b-article` @ `28a80f6`（修复提交 `1c52d5e` 已合入 `main` 并推送）
> 本地部署：`http://127.0.0.1:18081/qingxiang/`（18080 被 MonaHub 占用，未杀进程）
> 验证：`mvn clean test` 76/76 通过；浏览器与 curl 均已跑通发布/多媒体/CSRF/越权/路径穿越

## 1. 结论

- 「文章发布 + 多媒体」切片可用：信息流、详情、编辑器、图片/视频上传、草稿、提交/撤回、删除、自动封面。
- **审核台与分类管理整块未实现**，但导航已挂入口，M1「发布 → 审核 → 上架」主闭环目前无法演示。
- 因此**本分支暂不建议合入 `main`**；先完成 §5 的 B 侧阻塞项。

## 2. P0（阻塞合并）

| # | 问题 | 证据 | 归属 |
|---|---|---|---|
| P0-1 | 审核 / 分类管理 API 与页面缺失 | `/api/v1/reviews`、`/api/v1/categories/tree`、`/admin/reviews`、`/admin/categories` 实测 404；导航链接见 `common/b-sidebar.jspf:18,22`、`common/b-topbar.jspf:23`、`common/b-mobile-nav.jspf:35`、`article-detail.jsp:34` | B |
| P0-2 | 无「通过 / 驳回」，新文章永远停在 PENDING | 状态机只有 submit/withdraw；审核者点「审核工作台」404 | B |
| P0-3 | `sql/02_content.sql:47` 用中文句号结尾，建表 1064 | `cover_url ... COMMENT '...'。` → MySQL `ERROR 1064`，整个 B 表无法创建；本地已修复并提交 `1c52d5e` | B |
| P0-4 | 安装文档与模块文档未更新 | `README.md:59` 仍只执行 `01_core` + `04_seed`，照做后 B 页面 500；`docs/` 无 `开发文档-成员B.md`；B 的接口章节未同步 | B（`接口文档.md` 需 A 合并） |
| P0-5 | 头像上传成功但资料更新失败 | `POST /api/v1/files/avatar` 200；随后 `PUT /api/v1/me/avatar` 405（未实现）。`mine.js:18` 调用了 A 尚未交付的接口 | A 已补 `PUT /api/v1/me/profile`；`mine.js` 已改为调用它 |
| P0-6 | `sql/02b_media.sql` 在新库重复加列 | 实测 `ERROR 1060 Duplicate column name 'media_type'` | B |

## 3. P1（合入前建议处理）

| # | 问题 | 证据 | 归属 |
|---|---|---|---|
| P1-1 | 匿名写文章返回 403「页面已过期」而非契约的 401 | `SpringMvcConfig` 把 `/api/v1/articles/**` 排除出登录拦截器，CSRF 先命中；`curl` 实测 403 | B 引入 / A 改共享配置 |
| P1-2 | 契约漂移 | `POST /articles` 文档要求分类必填，实现允许 null；新增 `mediaType/mediaUrls/coverUrl`、`POST /files/media` 未写入接口文档 | B |
| P1-3 | `05_all_users_can_create.sql` 给 READER 创作权，但 `接口文档.md:89` 仍写「阅读者发文章 403」 | `/auth/me` 实测 reader 拥有 `article:*` | B + 组内拍板 |
| P1-4 | 死代码与错误注释 | `ReviewAction`、`ReviewRequest`、`BatchReviewRequest`、`ArticleReview`、`selectReviewQueue/selectReviewLogs/adminDelete`、`NotificationService` 基本无调用；`ArticleService.java:31` 引用不存在的 `ReviewActionService` | B |
| P1-5 | 分类软删除 + 唯一键 `(parent_id,name)` 导致名称不可复用 | `02_content.sql` 唯一键未包含 `deleted` | B |
| P1-6 | 集成测试依赖本机开发库与种子数据 | `ArticleMapperIntegrationTest` 直接 `@SpringJUnitConfig(SpringConfig.class)` 连 `qingxiang` 库 | B |
| P1-7 | 种子写死 id | `04_seed_b.sql` 写死 author=2、notification receiver=1/actor=2 | B |
| P1-8 | 视频只校验 16 字节 `ftyp` | 16 字节假 mp4 可上传成功；M1 可接受，答辩前需说明 | B |
| P1-9 | API 时间无时区 | 实测 `2026-11-15T09:20:00`，文档示例带 `+08:00` | B / A |
| P1-10 | 共享文件被越权直接修改 | B 直接改了 A 的 `SpringMvcConfig`、`AuthPageController`、`me.jsp`、`register.jsp`、`PermissionCatalog`、`pom.xml`；规范要求 B 提 PR、A 合并 | 流程 / A 复核 |

## 4. 账号中心与全局导航不一致

### 4.1 现状

- `me.jsp` 仍是 A 切片 1 的独立布局：只引 `app.css`，自带旧 `.topbar`，无左侧索引、无移动端底栏（`me.jsp:12,15`）。
- B 的页面使用 `app.css + b.css`、`common/b-topbar.jspf`、`common/b-sidebar.jspf`、`common/b-mobile-nav.jspf`。
- `b-topbar.jspf:26` 的头像链接指向 `/creator/mine`，账号中心入口反而不在头像上。
- `b-sidebar.jspf:27` 的「账号中心」没有 `active` 高亮键。

### 4.2 推荐方案

| 页面类型 | 布局 | 理由 |
|---|---|---|
| 顶级目的地：发现 / 我的内容 / **账号中心** / 审核台 / 分类管理 | 固定顶栏 + 左侧索引，当前项高亮；**不出现返回按钮** | 侧栏是全局导航；账号中心是侧栏的同级目的地，不是从某页进入的子流程 |
| 任务页：写图文 / 编辑 | 顶栏 + 返回按钮；不显示左侧索引 | 编辑器是带未保存状态的任务流，返回用于“退出任务”，侧栏切换容易丢草稿 |
| 认证页：登录 / 注册 | 独立布局 | 无导航语义 |

结论：**账号中心采用「保留左侧索引」的方式**，不要加返回按钮；写图文继续保留返回按钮。移动端顶部保留返回、底部保留 4 项导航。

### 4.3 归属与实施顺序（重点）

- **页面归 A**：`/me`、`MePageController`、个人资料属于 A（`docs/模块划分与分工.md:26`），A 实现，B 不应改 `me.jsp`。
- **外壳归 B**：`common/b-*.jspf`、`b.css`、`PageSupport`、`BPageModelAdvice` 由 B 提供。
- **A 的落地顺序**：`feat/b-article` 已合并进 `main`，A 已按 §4.4 改造 `/me`；未合并时不要先复制外壳，否则必然冲突。
- B 侧两处小修已由 A 在合并后完成：头像链接改 `/me`；侧栏「账号中心」支持 `active` 高亮。

### 4.4 A 的实现清单（合并后）

1. `MePageController#me` 调用 `PageSupport.common(request, model)`（同包可直接调用），保留 `account`、`capabilities`；BPageModelAdvice 已补 `avatarUrl`。
2. `me.jsp` 引入 `app.css + b.css`，include `b-sprite / b-topbar / b-sidebar / b-mobile-nav / b-scripts`。
3. 外层改为 `<div class="qx-shell">` + `<main class="qx-main">`，`active="account"`；移除旧 `.topbar`。
4. 退出表单的 token 统一用 `${csrfToken}`（`PageSupport` 已提供），不再用 `account.csrfToken`。
5. 保留「你可以做什么」能力卡片；资料编辑/头像入口等 A 的 `/api/v1/me/profile` 落地后再加。

## 5. 合并前 Checklist

**B（阻塞）**

- [ ] 补 `/api/v1/reviews/**`、`/api/v1/admin/articles/{id}/unpublish|republish` 与审核台页面
- [ ] 补 `/api/v1/categories/**`、`/api/v1/admin/categories/**` 与分类管理页面
- [ ] 补 `docs/开发文档-成员B.md`；更新 `docs/接口文档.md` §6/§7/§8/§9（media 字段、`/files/media`、分类是否必填）
- [ ] 更新 `README.md` 安装顺序：`01_core → 02_content → 04_seed → 04_seed_b → 05`
- [ ] `02b_media.sql` 改为幂等或删除
- [ ] 修正 `ArticleService.java:31` 注释，清理 P1-4 死代码
- [ ] 处理 P1-3（READER 创作权）与文档冲突，组内确认
- [x] 推送 `1c52d5e` SQL 修复
- [x] `b-topbar.jspf:26` 头像链接 `/creator/mine` → `/me`
- [x] `b-sidebar.jspf:27` 账号中心增加 `active == 'account'` 高亮

**A（合并后 / 共享）**

- [x] 先合并 `feat/b-article`，再按 §4.4 改造 `/me`
- [x] 补 `/api/v1/me/profile`（含头像）；`mine.js` 已改为调用它
- [ ] 评估 `SpringMvcConfig` 的 article 登录排除，保证匿名写接口返回 401
- [ ] 复核 B 对 A 文件（`SpringMvcConfig`、`AuthPageController`、`me.jsp`、`register.jsp`、`PermissionCatalog`、`pom.xml`）的改动

**合并前统一验证**

- [ ] 空库执行 `01 → 02 → 04 → 04_seed_b → 05`
- [ ] `mvn clean test`
- [ ] 主链路：登录 → 写图文（文本/图片/视频）→ 提交审核 → 审核者通过 → 发现页可见 → 驳回 → 作者修改重提

## 6. 复核命令

```bash
# 建库
mysql --default-character-set=utf8mb4 < sql/01_core.sql
mysql --default-character-set=utf8mb4 < sql/02_content.sql
mysql --default-character-set=utf8mb4 < sql/04_seed.sql
mysql --default-character-set=utf8mb4 < sql/04_seed_b.sql
mysql --default-character-set=utf8mb4 < sql/05_all_users_can_create.sql

# 测试
mvn -B clean test

# 页面
mvn -B -DskipTests clean package
mvn -B -Prun cargo:run
```

演示账号：`creator` / `reviewer` / `reader`，密码均 `123456`。
