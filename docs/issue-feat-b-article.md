> 状态：`feat/b-article` 已合入 `main`（含 SQL 修复与 A 的账号中心）。**不需要再合并这个分支**。
> 本 issue 只跟踪 B 的剩余工作。B 从 §0 开始按顺序做；合并流程在 §4，按命令复制即可。
> 当前 `main` 通过 `mvn clean test`（79/79）；A 本机已跑通发布、多媒体、审核前状态机与账号中心。

## 0. B 现在要做什么（先看这里）

一句话：**发布/多媒体已经做完，剩下「审核台 + 分类管理」两块功能，以及 4 个收尾项。**

按顺序：

1. **先拉最新 `main`，开新分支**（不要继续用旧的 `feat/b-article`）：
   ```bash
   git clone https://github.com/JFT-Group/QingXiangTuWen.git qingxiang-b
   cd qingxiang-b
   git switch -c feat/b-review-category
   ```
   已有仓库的：
   ```bash
   git switch main
   git pull origin main
   git switch -c feat/b-review-category
   ```
2. **审核台**：按 `docs/接口文档.md` §8 实现
   - `GET /api/v1/reviews`、`GET /api/v1/reviews/{id}`
   - `POST /api/v1/reviews/{id}/approve`、`/reject`
   - `POST /api/v1/reviews/batch`（逐条事务、部分成功）
   - `POST /api/v1/admin/articles/{id}/unpublish`、`/republish`
   - 页面 `/admin/reviews`（导航已经指过去）
3. **分类管理**：按 `docs/接口文档.md` §7 实现
   - `GET /api/v1/categories/tree`（公开）
   - `/api/v1/admin/categories` 的新增/编辑/启停/删除
   - 页面 `/admin/categories`
4. **文档**
   - 新建 `docs/开发文档-成员B.md`（按模板）
   - 更新 `docs/接口文档.md` §6/§7/§8/§9：媒体字段、`POST /files/media`、分类是否必填
   - README 的安装顺序已经由 A 补好，不要回退
5. **收尾**
   - `sql/02b_media.sql` 改为幂等或删除
   - 清理 P1-4 死代码；修正 `ArticleService.java:31` 注释
   - 集成测试不要依赖本机开发库的固定文章标题/昵称
   - `04_seed_b.sql` 不要写死用户 id
6. **自测后提 PR**（§4）

> 注意：A 已经改过你的 `sql/05_creator_permissions.sql`、`b-sidebar.jspf`、`b-topbar.jspf`、`mine.js`、`README.md`。**先把 `main` 拉下来再动手**，不要用旧分支覆盖这些文件。

## 1. 验收标准

完成审核台后，下面这条链路必须能走通：

1. `creator/123456` 写一篇图文 → 提交审核。
2. `reviewer/123456` 打开「审核工作台」→ 看到待审列表 → 通过。
3. 发现页出现这篇文章；作者收到站内通知。
4. 另一篇被驳回，作者能看到驳回理由并修改重提。
5. 批量通过 2 条 + 其中 1 条已被处理：返回 `successCount/failCount/failures`，失败项状态不变。
6. 分类：新增子分类、改名、排序、停用（有内容/子分类时 409）、删除。
7. 越权：`creator` 调审核接口 403；`reader` 调创作接口 403。
8. `mvn clean test` 全绿，PR 里附命令输出和页面截图。

## 2. 问题台账（B 需要处理）

### P0

| # | 问题 | 状态 |
|---|---|---|
| P0-1 | `/api/v1/reviews/**`、`/api/v1/categories/**`、`/admin/reviews`、`/admin/categories` 全是 404 | **B 待做** |
| P0-2 | 没有「通过/驳回」，文章永远停在 PENDING | **B 待做** |
| P0-3 | `sql/02_content.sql:47` 中文句号导致建表 1064 | A 已代修（`1c52d5e`），后续由 B 维护 |
| P0-4 | `docs/开发文档-成员B.md` 缺失；B 的接口章节未同步 | **B 待做**（README 已由 A 补） |
| P0-5 | 头像资料更新 405 | 已解决：A 提供 `PUT /api/v1/me/profile`，`mine.js` 已接 |
| P0-6 | `sql/02b_media.sql` 在新库重复加列 | **B 待做** |

### P1

| # | 问题 | 状态 |
|---|---|---|
| P1-1 | 匿名写接口返回 403 而非 401 | 已解决：`ApiWriteAuthInterceptor`（A） |
| P1-2 | 契约漂移：媒体字段、`/files/media`、分类必填未写进接口文档 | **B 待做** |
| P1-3 | 阅读者被授予创作权 | 已解决：`sql/05_creator_permissions.sql` 只给 CREATOR/REVIEWER，READER 权限已删并 +`session_version` |
| P1-4 | 死代码与 `ReviewActionService` 错误注释 | **B 待做** |
| P1-5 | 分类软删除 + `(parent_id,name)` 唯一键导致名称不可复用 | **B 待做** |
| P1-6 | 集成测试依赖本机开发库固定数据 | **B 待做** |
| P1-7 | `04_seed_b.sql` 写死用户 id | **B 待做** |
| P1-8 | 视频只校验 16 字节 `ftyp` | M1 可接受，答辩说明即可 |
| P1-9 | API 时间无时区，文档示例带 `+08:00` | 可选，顺手对齐 |
| P1-10 | B 曾直接改 A 的共享文件 | 已由 A 合并；以后改 A 文件先提 PR |

## 3. 账号中心与导航（已完成，B 不要改）

- `/me` 已按「顶栏 + 左侧索引 + 移动端导航」实现，账号中心高亮，无返回按钮。
- 头像上传 + 资料修改已接到 `PUT /api/v1/me/profile`。
- `b-topbar.jspf` 头像链接已指向 `/me`；`b-sidebar.jspf` 已支持 `active == 'account'`。
- 这部分归 A：`me.jsp`、`account.js`、`app.css`、`MePageController`、A 的 Java/测试。

## 4. 合并步骤（小白版，逐步复制）

### 4.1 第一次准备

```bash
git clone https://github.com/JFT-Group/QingXiangTuWen.git qingxiang-b
cd qingxiang-b
git config user.name "你的名字"
git config user.email "你的邮箱"
git status
```

### 4.2 每次开发前

```bash
git switch main
git pull origin main
git switch -c feat/b-review-category
```

> 分支名一次任务一个；不要多人共用 `main` 直接改。

### 4.3 写完后自测

```bash
# 1. 建库（本机 MySQL 已启动）
mysql -uroot -p --default-character-set=utf8mb4 < sql/01_core.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql/02_content.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql/04_seed.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql/04_seed_b.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql/05_creator_permissions.sql

# 2. 配置数据库密码
#    复制 src/main/resources/common/db.properties.example
#    为 src/main/resources/common/db.properties，填本机 MySQL 密码

# 3. 测试
mvn -B clean test

# 4. 启动
mvn -B -DskipTests clean package
mvn -B -Prun cargo:run
# http://127.0.0.1:18080/qingxiang/
```

演示账号：`creator` / `reviewer` / `reader`，密码均 `123456`。

### 4.4 提交并推送

```bash
git status                  # 确认只包含你这次的文件
git add <你改的文件>
git commit -m "feat(b-review): 完成审核台与分类管理"
git push -u origin feat/b-review-category
```

### 4.5 在网页上提 PR

1. 打开 https://github.com/JFT-Group/QingXiangTuWen
2. 页面顶部会出现 `Compare & pull request`，点它。
3. `base` 选 `main`，`compare` 选 `feat/b-review-category`。
4. 标题写清楚；描述里贴上：
   - 做了哪些接口/页面
   - `mvn clean test` 结果
   - 主链路截图（审核通过、驳回、分类管理）
5. 点 `Create pull request`。
6. **不要自己点 Merge**，等 A 看完再合；至少让另一位组员点过。

### 4.6 主分支有新提交时

```bash
git switch main
git pull origin main
git switch feat/b-review-category
git merge main
# 有冲突就改文件，改完：
git add <冲突文件>
git commit
git push
```

### 4.7 三条红线

- 不要 `git push -f origin main`。
- 不要直接往 `main` 提交；一律走分支 + PR。
- 不要改 A 的 `me.jsp`、`account.js`、A 的 Java/测试；要改先在本 issue 里说。

## 5. 遇到问题怎么问

把这三样贴到群里/issue：

1. 你执行到哪一步（命令原文）。
2. `git status` 的输出。
3. 完整报错（不要只说「跑不起来」）。

常见问题：

- `Port 18080 already in use`：先关掉旧的 Tomcat/Qingxiang 窗口，或 `netstat -ano | findstr :18080` 找 PID。
- `Access denied for user 'root'`：`db.properties` 密码不对。
- `Table 'qingxiang.article' doesn't exist`：§4.3 的建库顺序没执行完。
- JSP 404/500：先看 `target/cargo/configurations/tomcat9x/logs/qingxiang.log` 的最后 30 行。
