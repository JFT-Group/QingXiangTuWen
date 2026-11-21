<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageScript" value="feed"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="${fn:escapeXml(csrfToken)}">
    <meta name="ctx" content="${ctx}">
    <title>发现 · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <link rel="stylesheet" href="${ctx}/static/css/b.css">
</head>
<body class="qx-app">
<%@ include file="common/b-sprite.jspf" %>
<%@ include file="common/b-topbar.jspf" %>

<div class="qx-shell">
    <%@ include file="common/b-sidebar.jspf" %>

    <main class="qx-main" id="main">
        <nav class="channels" aria-label="分类频道">
            <a class="channel ${empty categoryId ? 'is-active' : ''}" href="${ctx}/discover">推荐</a>
            <c:forEach var="cat" items="${categories}">
                <a class="channel ${categoryId == cat.id ? 'is-active' : ''}"
                   href="${ctx}/discover?categoryId=${cat.id}">
                    <c:out value="${cat.name}"/>
                </a>
            </c:forEach>
        </nav>

        <div class="feed-head">
            <h1>
                <c:choose>
                    <c:when test="${not empty keyword}">“<c:out value="${keyword}"/>” 的结果</c:when>
                    <c:otherwise>发现</c:otherwise>
                </c:choose>
            </h1>
            <span class="muted">${articles.total} 篇</span>
        </div>

        <div class="feed" id="feed">
            <c:forEach var="a" items="${articles.list}">
                <article class="feed-card">
                    <c:choose>
                        <c:when test="${a.mediaType == 'IMAGE' and not empty a.coverUrl}">
                            <a class="cover cover--media" href="${ctx}/articles/${a.id}" data-detail="${a.id}"
                               aria-label="${fn:escapeXml(a.title)}">
                                <img class="cover-img" src="${ctx}${fn:escapeXml(a.coverUrl)}" alt="">
                                <span class="cover-cat">
                                    <c:out value="${empty a.categoryName ? '未分类' : a.categoryName}"/>
                                </span>
                            </a>
                        </c:when>
                        <c:when test="${a.mediaType == 'VIDEO'}">
                            <a class="cover cover--media" href="${ctx}/articles/${a.id}" data-detail="${a.id}"
                               style="background:${fn:escapeXml(a.coverColor)}"
                               aria-label="${fn:escapeXml(a.title)}">
                                <span class="cover-cat">
                                    <c:out value="${empty a.categoryName ? '未分类' : a.categoryName}"/>
                                </span>
                                <span class="cover-play">
                                    <svg class="icon" aria-hidden="true"><use href="#i-play"/></svg>
                                </span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="cover" href="${ctx}/articles/${a.id}" data-detail="${a.id}"
                               style="background:${fn:escapeXml(a.coverColor)}"
                               aria-label="${fn:escapeXml(a.title)}">
                                <span class="cover-cat">
                                    <c:out value="${empty a.categoryName ? '未分类' : a.categoryName}"/>
                                </span>
                                <strong class="cover-title"><c:out value="${a.title}"/></strong>
                            </a>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${a.mediaType != 'TEXT'}">
                        <a class="feed-title" href="${ctx}/articles/${a.id}" data-detail="${a.id}">
                            <c:out value="${a.title}"/>
                        </a>
                    </c:if>
                    <div class="feed-meta">
                        <span class="feed-author">
                            <span class="mini-avatar">${fn:escapeXml(fn:substring(a.authorName, 0, 1))}</span>
                            <span><c:out value="${a.authorName}"/></span>
                        </span>
                        <span class="feed-stats">
                            <svg class="icon icon-xs" aria-hidden="true"><use href="#i-heart"/></svg>
                            ${a.likeCount}
                        </span>
                    </div>
                </article>
            </c:forEach>
        </div>

        <c:if test="${empty articles.list}">
            <div class="feed-empty">
                <svg class="icon" aria-hidden="true"><use href="#i-search"/></svg>
                <h2>还没有匹配的内容</h2>
                <p>换个关键词或分类再试试。</p>
                <c:choose>
                    <c:when test="${loginUser.hasPermission('article:create')}">
                        <a class="qx-btn qx-btn-primary" href="${ctx}/creator/articles/new">写第一篇</a>
                    </c:when>
                    <c:otherwise>
                        <a class="qx-btn qx-btn-fill" href="${ctx}/discover">看看全部</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <c:if test="${articles.total > articles.page * articles.size}">
            <div style="text-align:center;padding:18px 0 8px">
                <button class="qx-btn qx-btn-fill" id="load-more"
                        data-page="${articles.page}" data-size="${articles.size}">
                    加载更多
                </button>
            </div>
        </c:if>

        <div class="skeleton-grid" id="feed-skeleton" hidden>
            <c:forEach begin="1" end="10">
                <div class="skel-card">
                    <div class="skel-cover"></div>
                    <div class="skel-line"></div>
                    <div class="skel-line short"></div>
                </div>
            </c:forEach>
        </div>
    </main>
</div>

<div class="qx-modal" id="detail-modal" hidden>
    <div class="qx-detail" role="dialog" aria-modal="true" aria-label="内容详情">
        <button class="qx-detail-close" id="detail-close" type="button" aria-label="关闭">
            <svg class="icon icon-sm" aria-hidden="true"><use href="#i-x"/></svg>
        </button>
        <div class="qx-detail-media" id="detail-media">
            <span class="cover-cat" id="detail-cover-cat"></span>
            <strong class="cover-title" id="detail-cover-title"></strong>
        </div>
        <div class="qx-detail-side">
            <div class="detail-author">
                <div class="detail-avatar" id="detail-avatar"></div>
                <div class="detail-author-name" id="detail-author"></div>
                <button class="detail-follow" type="button" disabled title="关注功能由成员C接入">关注</button>
            </div>
            <h2 class="detail-title" id="detail-title"></h2>
            <div class="detail-content" id="detail-content"></div>
            <div class="detail-meta" id="detail-meta"></div>
            <div class="detail-divider"></div>
            <div class="detail-comments-title">评论</div>
            <div class="detail-comments-empty">还没有评论</div>
            <div class="detail-actions">
                <button class="qx-icon-btn" type="button" disabled title="互动模块由成员C接入">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-heart"/></svg>
                    <span id="detail-like">0</span>
                </button>
                <button class="qx-icon-btn" type="button" disabled title="互动模块由成员C接入">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-bookmark"/></svg>
                    <span id="detail-favorite">0</span>
                </button>
                <button class="qx-icon-btn" type="button" disabled title="互动模块由成员C接入">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-message"/></svg>
                    <span id="detail-comment">0</span>
                </button>
                <a class="qx-icon-btn" id="detail-open" href="#">
                    打开
                    <svg class="icon icon-xs" aria-hidden="true"><use href="#i-corner"/></svg>
                </a>
            </div>
        </div>
    </div>
</div>

<%@ include file="common/b-mobile-nav.jspf" %>
<%@ include file="common/b-scripts.jspf" %>
</body>
</html>
