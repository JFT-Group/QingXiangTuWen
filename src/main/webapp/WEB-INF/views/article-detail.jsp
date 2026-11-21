<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="${fn:escapeXml(csrfToken)}">
    <meta name="ctx" content="${ctx}">
    <title><c:out value="${article.title}"/> · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <link rel="stylesheet" href="${ctx}/static/css/b.css">
</head>
<body class="qx-app">
<%@ include file="common/b-sprite.jspf" %>
<%@ include file="common/b-topbar.jspf" %>

<main class="article-page">
    <div class="article-page-head">
        <a class="qx-back" href="${ctx}/discover">
            <svg class="icon icon-sm" aria-hidden="true"><use href="#i-chevron-left"/></svg>
            返回发现
        </a>
        <div style="display:flex;gap:8px;align-items:center">
            <c:if test="${canEdit}">
                <a class="qx-btn qx-btn-fill" href="${ctx}/creator/articles/${article.id}/edit">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-edit"/></svg>
                    编辑
                </a>
            </c:if>
            <c:if test="${canReview}">
                <a class="qx-btn qx-btn-outline" href="${ctx}/admin/reviews?id=${article.id}">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-shield"/></svg>
                    去审核
                </a>
            </c:if>
        </div>
    </div>

    <div class="qx-detail ${article.mediaType != 'TEXT' ? 'is-media' : ''}">
        <div class="qx-detail-media" style="background:${article.mediaType == 'TEXT' ? fn:escapeXml(article.coverColor) : '#000'}">
            <c:choose>
                <c:when test="${article.mediaType == 'IMAGE'}">
                    <div class="media-gallery">
                        <c:forEach var="u" items="${article.mediaUrls}">
                            <img src="${ctx}${fn:escapeXml(u)}" alt="">
                        </c:forEach>
                    </div>
                </c:when>
                <c:when test="${article.mediaType == 'VIDEO'}">
                    <c:forEach var="u" items="${article.mediaUrls}" varStatus="st">
                        <c:if test="${st.first}">
                            <video class="media-video" controls preload="metadata"
                                   src="${ctx}${fn:escapeXml(u)}"></video>
                        </c:if>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <span class="cover-cat">
                        <c:out value="${empty article.categoryName ? '未分类' : article.categoryName}"/>
                    </span>
                    <strong class="cover-title"><c:out value="${article.title}"/></strong>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="qx-detail-side">
            <div class="detail-author">
                <div class="detail-avatar">
                    ${fn:escapeXml(fn:substring(article.authorName, 0, 1))}
                </div>
                <div class="detail-author-name"><c:out value="${article.authorName}"/></div>
                <button class="detail-follow" type="button" disabled title="关注功能由成员C接入">关注</button>
            </div>

            <h1 class="detail-title"><c:out value="${article.title}"/></h1>

            <c:if test="${article.status != 'PUBLISHED'}">
                <div class="detail-status">
                    <span class="status-chip status-${article.status}"><c:out value="${article.statusLabel}"/></span>
                </div>
            </c:if>
            <c:if test="${not empty article.rejectReason}">
                <div class="detail-reason"><c:out value="${article.rejectReason}"/></div>
            </c:if>

            <div class="detail-content">
                <c:out value="${article.content}" escapeXml="false"/>
            </div>

            <div class="detail-meta">
                <span>
                    <c:choose>
                        <c:when test="${not empty article.publishTime}">
                            ${fn:substring(fn:replace(article.publishTime, 'T', ' '), 0, 16)}
                        </c:when>
                        <c:otherwise>
                            ${fn:substring(fn:replace(article.updateTime, 'T', ' '), 0, 16)}
                        </c:otherwise>
                    </c:choose>
                </span>
                <span><c:out value="${empty article.categoryName ? '未分类' : article.categoryName}"/></span>
            </div>

            <div class="detail-divider"></div>
            <div class="detail-comments-title">评论</div>
            <div class="detail-comments-empty">还没有评论</div>

            <div class="detail-actions">
                <button class="qx-icon-btn" type="button" disabled title="互动模块由成员C接入">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-heart"/></svg>
                    ${article.likeCount}
                </button>
                <button class="qx-icon-btn" type="button" disabled title="互动模块由成员C接入">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-bookmark"/></svg>
                    ${article.favoriteCount}
                </button>
                <button class="qx-icon-btn" type="button" disabled title="互动模块由成员C接入">
                    <svg class="icon icon-sm" aria-hidden="true"><use href="#i-message"/></svg>
                    ${article.commentCount}
                </button>
            </div>
        </div>
    </div>
</main>

<%@ include file="common/b-mobile-nav.jspf" %>
<%@ include file="common/b-scripts.jspf" %>
</body>
</html>
