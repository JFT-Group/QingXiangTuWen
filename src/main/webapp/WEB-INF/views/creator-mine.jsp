<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageScript" value="mine"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="${fn:escapeXml(csrfToken)}">
    <meta name="ctx" content="${ctx}">
    <title>我的内容 · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <link rel="stylesheet" href="${ctx}/static/css/b.css">
</head>
<body class="qx-app">
<%@ include file="common/b-sprite.jspf" %>
<%@ include file="common/b-topbar.jspf" %>

<div class="qx-shell">
    <%@ include file="common/b-sidebar.jspf" %>

    <main class="qx-main">
        <div class="mine-shell">
            <section class="mine-head">
                <div class="mine-avatar-wrap">
                    <div class="mine-avatar" id="mine-avatar">
                        <c:choose>
                            <c:when test="${not empty avatarUrl}">
                                <img src="${ctx}${fn:escapeXml(avatarUrl)}" alt="头像">
                            </c:when>
                            <c:otherwise>${fn:escapeXml(fn:substring(loginUser.nickname, 0, 1))}</c:otherwise>
                        </c:choose>
                    </div>
                    <button class="mine-avatar-edit" type="button" id="avatar-trigger"
                            title="更换头像" aria-label="更换头像">
                        <svg class="icon icon-xs" aria-hidden="true"><use href="#i-image"/></svg>
                    </button>
                    <input type="file" id="avatar-input" accept="image/jpeg,image/png,image/webp" hidden>
                </div>
                <div>
                    <h1><c:out value="${loginUser.nickname}"/></h1>
                    <p class="mine-sub">@<c:out value="${loginUser.username}"/></p>
                </div>
                <div class="mine-stats">
                    <div class="mine-stat">
                        <strong>${counts.PUBLISHED}</strong>
                        <span>已发布</span>
                    </div>
                    <div class="mine-stat">
                        <strong>${counts.DRAFT}</strong>
                        <span>草稿</span>
                    </div>
                    <div class="mine-stat">
                        <strong>${counts.PENDING}</strong>
                        <span>待审核</span>
                    </div>
                </div>
                <a class="qx-btn qx-btn-fill" href="${ctx}/discover">去发现</a>
            </section>

            <nav class="mine-tabs" aria-label="内容状态">
                <a class="mine-tab ${empty status ? 'is-active' : ''}" href="${ctx}/creator/mine">
                    全部 <span class="count">${counts.ALL}</span>
                </a>
                <a class="mine-tab ${status == 'DRAFT' ? 'is-active' : ''}" href="${ctx}/creator/mine?status=DRAFT">
                    草稿 <span class="count">${counts.DRAFT}</span>
                </a>
                <a class="mine-tab ${status == 'PENDING' ? 'is-active' : ''}" href="${ctx}/creator/mine?status=PENDING">
                    待审核 <span class="count">${counts.PENDING}</span>
                </a>
                <a class="mine-tab ${status == 'PUBLISHED' ? 'is-active' : ''}" href="${ctx}/creator/mine?status=PUBLISHED">
                    已发布 <span class="count">${counts.PUBLISHED}</span>
                </a>
                <a class="mine-tab ${status == 'REJECTED' ? 'is-active' : ''}" href="${ctx}/creator/mine?status=REJECTED">
                    已驳回 <span class="count">${counts.REJECTED}</span>
                </a>
                <a class="mine-tab ${status == 'UNPUBLISHED' ? 'is-active' : ''}" href="${ctx}/creator/mine?status=UNPUBLISHED">
                    已下架 <span class="count">${counts.UNPUBLISHED}</span>
                </a>
            </nav>

            <c:choose>
                <c:when test="${empty articles.list}">
                    <div class="qx-empty">
                        <h2>这里还没有内容</h2>
                        <p>写下一篇，存成草稿或直接送审。</p>
                        <a class="qx-btn qx-btn-primary" href="${ctx}/creator/articles/new">
                            <svg class="icon icon-sm" aria-hidden="true"><use href="#i-plus"/></svg>
                            写图文
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <ul class="mine-list">
                        <c:forEach var="a" items="${articles.list}">
                            <li class="mine-row" data-article-id="${a.id}">
                                <a class="mine-cover" href="${ctx}/articles/${a.id}"
                                   style="background:${fn:escapeXml(a.coverColor)}"
                                   aria-label="${fn:escapeXml(a.title)}">
                                    <strong><c:out value="${a.title}"/></strong>
                                </a>
                                <div class="mine-body">
                                    <div class="mine-title-row">
                                        <c:choose>
                                            <c:when test="${a.status == 'DRAFT' or a.status == 'REJECTED'}">
                                                <a class="mine-title" href="${ctx}/creator/articles/${a.id}/edit">
                                                    <c:out value="${a.title}"/>
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a class="mine-title" href="${ctx}/articles/${a.id}">
                                                    <c:out value="${a.title}"/>
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                        <span class="status-chip status-${a.status}">
                                            <c:choose>
                                                <c:when test="${a.status == 'DRAFT'}">草稿</c:when>
                                                <c:when test="${a.status == 'PENDING'}">待审核</c:when>
                                                <c:when test="${a.status == 'PUBLISHED'}">已发布</c:when>
                                                <c:when test="${a.status == 'REJECTED'}">已驳回</c:when>
                                                <c:when test="${a.status == 'UNPUBLISHED'}">已下架</c:when>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="mine-meta">
                                        <span><c:out value="${empty a.categoryName ? '未分类' : a.categoryName}"/></span>
                                        <span>更新于 ${fn:substring(fn:replace(a.updateTime, 'T', ' '), 0, 16)}</span>
                                        <span>${a.likeCount} 赞</span>
                                    </div>
                                    <c:if test="${not empty a.rejectReason}">
                                        <p class="mine-reason"><c:out value="${a.rejectReason}"/></p>
                                    </c:if>
                                </div>
                                <div class="mine-actions">
                                    <c:choose>
                                        <c:when test="${a.status == 'DRAFT' or a.status == 'REJECTED'}">
                                            <a class="qx-icon-btn" href="${ctx}/creator/articles/${a.id}/edit"
                                               title="继续编辑" aria-label="继续编辑">
                                                <svg class="icon icon-sm" aria-hidden="true"><use href="#i-edit"/></svg>
                                            </a>
                                            <button class="qx-icon-btn" type="button" data-action="submit"
                                                    data-id="${a.id}" title="提交审核" aria-label="提交审核">
                                                <svg class="icon icon-sm" aria-hidden="true"><use href="#i-send"/></svg>
                                            </button>
                                        </c:when>
                                        <c:when test="${a.status == 'PENDING'}">
                                            <button class="qx-icon-btn" type="button" data-action="withdraw"
                                                    data-id="${a.id}" title="撤回" aria-label="撤回">
                                                <svg class="icon icon-sm" aria-hidden="true"><use href="#i-undo"/></svg>
                                            </button>
                                        </c:when>
                                        <c:when test="${a.status == 'PUBLISHED' or a.status == 'UNPUBLISHED'}">
                                            <a class="qx-icon-btn" href="${ctx}/articles/${a.id}"
                                               title="查看" aria-label="查看">
                                                <svg class="icon icon-sm" aria-hidden="true"><use href="#i-eye"/></svg>
                                            </a>
                                            <a class="qx-icon-btn" href="${ctx}/creator/articles/${a.id}/edit"
                                               title="编辑" aria-label="编辑">
                                                <svg class="icon icon-sm" aria-hidden="true"><use href="#i-edit"/></svg>
                                            </a>
                                        </c:when>
                                    </c:choose>
                                    <button class="qx-icon-btn qx-btn-danger" type="button" data-action="delete"
                                            data-id="${a.id}" title="删除" aria-label="删除">
                                        <svg class="icon icon-sm" aria-hidden="true"><use href="#i-trash"/></svg>
                                    </button>
                                </div>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</div>

<%@ include file="common/b-mobile-nav.jspf" %>
<%@ include file="common/b-scripts.jspf" %>
</body>
</html>
