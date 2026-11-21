<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="user" value="${account.user}"/>
<c:set var="pageScript" value="account"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="${fn:escapeXml(csrfToken)}">
    <meta name="ctx" content="${ctx}">
    <title>账号中心 · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <link rel="stylesheet" href="${ctx}/static/css/b.css">
</head>
<body class="qx-app">
<%@ include file="common/b-sprite.jspf" %>
<%@ include file="common/b-topbar.jspf" %>

<div class="qx-shell">
    <%@ include file="common/b-sidebar.jspf" %>

    <main class="qx-main">
        <div class="account account-page">
            <section class="profile" aria-labelledby="account-name">
                <div class="me-avatar-wrap">
                    <div class="avatar me-avatar" id="me-avatar">
                        <c:choose>
                            <c:when test="${not empty user.avatarUrl}">
                                <img src="${ctx}${fn:escapeXml(user.avatarUrl)}" alt="头像">
                            </c:when>
                            <c:otherwise>${fn:escapeXml(fn:substring(user.nickname, 0, 1))}</c:otherwise>
                        </c:choose>
                    </div>
                    <button class="me-avatar-edit" type="button" id="avatar-trigger"
                            title="更换头像" aria-label="更换头像">
                        <svg class="icon" aria-hidden="true"><use href="#i-image"/></svg>
                    </button>
                    <input type="file" id="avatar-input" accept="image/jpeg,image/png,image/webp" hidden>
                </div>
                <div class="profile-main">
                    <h1 id="account-name"><c:out value="${user.nickname}"/></h1>
                    <p class="handle">@<c:out value="${user.username}"/></p>
                    <p class="bio" id="me-bio" ${empty user.bio ? 'hidden' : ''}><c:out value="${user.bio}"/></p>
                    <span class="role-chip"><c:out value="${user.roleName}"/></span>
                </div>
                <div class="profile-actions">
                    <button class="qx-btn qx-btn-fill" type="button" id="edit-toggle"
                            aria-expanded="false" aria-controls="profile-edit-panel">
                        <svg class="icon icon-sm" aria-hidden="true"><use href="#i-edit"/></svg>
                        编辑资料
                    </button>
                </div>
            </section>

            <section class="stats" aria-label="账号数据">
                <div class="stat">
                    <strong>${user.followerCount}</strong>
                    <span>粉丝</span>
                </div>
                <div class="stat">
                    <strong>${user.followingCount}</strong>
                    <span>关注</span>
                </div>
                <div class="stat">
                    <strong>${account.unreadCount}</strong>
                    <span>未读通知</span>
                </div>
            </section>

            <section class="panel" id="profile-edit-panel" aria-labelledby="profile-edit-title" hidden>
                <h2 id="profile-edit-title">编辑资料</h2>
                <p class="muted">昵称会显示在图文卡片、详情和评论区。</p>
                <form class="account-form" id="profile-form" novalidate>
                    <label for="profile-nickname">
                        昵称
                        <input id="profile-nickname" name="nickname" type="text" maxlength="50"
                               value="${fn:escapeXml(user.nickname)}" autocomplete="nickname" required>
                    </label>
                    <label for="profile-bio">
                        简介
                        <textarea id="profile-bio" name="bio" maxlength="200"
                                  placeholder="一句话介绍自己"><c:out value="${user.bio}"/></textarea>
                    </label>
                    <div class="form-actions">
                        <button class="qx-btn qx-btn-ghost" type="button" id="profile-cancel">取消</button>
                        <button class="qx-btn qx-btn-primary" type="submit" id="profile-save">保存</button>
                    </div>
                </form>
            </section>

            <section class="panel" aria-labelledby="cap-title">
                <h2 id="cap-title">你可以做什么</h2>
                <p class="muted">所有登录用户都可以浏览、发布和管理自己的内容；审核者额外拥有审核与分类管理入口。</p>
                <ul class="cap-grid">
                    <c:forEach var="cap" items="${capabilities}">
                        <li class="cap-card">
                            <span class="cap-mark" aria-hidden="true">✓</span>
                            <div>
                                <strong><c:out value="${cap.title}"/></strong>
                                <p><c:out value="${cap.description}"/></p>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </section>
        </div>
    </main>
</div>

<%@ include file="common/b-mobile-nav.jspf" %>
<%@ include file="common/b-scripts.jspf" %>
</body>
</html>
