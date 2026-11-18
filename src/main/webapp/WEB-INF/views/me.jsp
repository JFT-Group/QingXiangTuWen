<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="user" value="${account.user}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>账号中心 · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
</head>
<body class="app-body">
<header class="topbar">
    <a class="brand" href="${ctx}/">轻享<span>图文</span></a>
    <div class="topbar-right">
        <span class="who"><c:out value="${user.nickname}"/></span>
        <form class="inline-form" method="post" action="${ctx}/logout">
            <input type="hidden" name="_csrf" value="${fn:escapeXml(account.csrfToken)}">
            <button class="btn btn-ghost" type="submit">退出</button>
        </form>
    </div>
</header>

<main class="account">
    <section class="profile" aria-labelledby="account-name">
        <div class="avatar" aria-hidden="true">${fn:escapeXml(fn:substring(user.nickname, 0, 1))}</div>
        <div class="profile-main">
            <h1 id="account-name"><c:out value="${user.nickname}"/></h1>
            <p class="handle">@<c:out value="${user.username}"/></p>
            <c:if test="${not empty user.bio}">
                <p class="bio"><c:out value="${user.bio}"/></p>
            </c:if>
            <span class="role-chip">
                <c:out value="${user.roleName}"/> · <c:out value="${user.roleCode}"/>
            </span>
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

    <section class="panel" aria-labelledby="perm-title">
        <h2 id="perm-title">当前权限</h2>
        <p class="muted">权限码来自角色，后端切面按接口校验；前端隐藏按钮只是体验。</p>
        <ul class="perm-list">
            <c:forEach var="code" items="${account.permissions}">
                <li>${fn:escapeXml(code)}</li>
            </c:forEach>
            <c:if test="${empty account.permissions}">
                <li class="empty">阅读权限，无需管理权限码</li>
            </c:if>
        </ul>
    </section>
</main>
</body>
</html>
