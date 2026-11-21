<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageScript" value="editor"/>
<c:set var="editing" value="${mode == 'edit'}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="${fn:escapeXml(csrfToken)}">
    <meta name="ctx" content="${ctx}">
    <title>${editing ? '编辑图文' : '写图文'} · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <link rel="stylesheet" href="${ctx}/static/css/b.css">
</head>
<body class="qx-app">
<%@ include file="common/b-sprite.jspf" %>

<header class="qx-topbar qx-topbar-edit">
    <a class="qx-back" href="${ctx}/creator/mine">
        <svg class="icon icon-sm" aria-hidden="true"><use href="#i-chevron-left"/></svg>
        <span>我的内容</span>
    </a>
    <div style="display:flex;align-items:center;gap:10px;min-width:0">
        <c:if test="${editing}">
            <span class="status-chip status-${article.status}" id="editor-status-chip">
                <c:choose>
                    <c:when test="${article.status == 'DRAFT'}">草稿</c:when>
                    <c:when test="${article.status == 'PENDING'}">待审核</c:when>
                    <c:when test="${article.status == 'PUBLISHED'}">已发布</c:when>
                    <c:when test="${article.status == 'REJECTED'}">已驳回</c:when>
                    <c:when test="${article.status == 'UNPUBLISHED'}">已下架</c:when>
                </c:choose>
            </span>
        </c:if>
        <span class="muted" style="font-size:12px;color:var(--fg-mute)" id="save-state"></span>
    </div>
    <div class="qx-top-actions">
        <a class="qx-btn qx-btn-ghost" href="${ctx}/discover">发现</a>
        <button class="qx-btn qx-btn-fill" type="button" id="save-draft">保存草稿</button>
        <button class="qx-btn qx-btn-primary" type="button" id="submit-review">
            ${editing and (article.status == 'PUBLISHED' or article.status == 'UNPUBLISHED')
                    ? '保存并重新送审' : '提交审核'}
        </button>
        <div class="qx-user">
            <span class="qx-avatar-sm">${fn:escapeXml(fn:substring(loginUser.nickname, 0, 1))}</span>
        </div>
    </div>
</header>

<main class="editor-shell"
      id="editor-root"
      data-mode="${mode}"
      data-id="${editing ? article.id : ''}"
      data-cover="${editing ? fn:escapeXml(article.coverColor) : ''}"
      data-media-type="${editing ? article.mediaType : 'TEXT'}"
      data-media-urls="${editing ? fn:escapeXml(article.mediaUrlsRaw) : ''}"
      data-status="${editing ? article.status : 'DRAFT'}">
    <section class="editor-main" aria-label="编辑区">
        <div class="editor-types" role="group" aria-label="内容类型">
            <button class="editor-type" type="button" data-type="TEXT">文本</button>
            <button class="editor-type" type="button" data-type="IMAGE">图片</button>
            <button class="editor-type" type="button" data-type="VIDEO">视频</button>
        </div>
        <label class="sr-only" for="editor-title">标题</label>
        <input class="editor-title" id="editor-title" type="text" maxlength="120"
               placeholder="未命名" value="${editing ? fn:escapeXml(article.title) : ''}"
               autocomplete="off">

        <div class="editor-media" id="editor-media" hidden>
            <div class="media-tools">
                <button class="qx-btn qx-btn-fill" type="button" id="media-pick">添加图片</button>
                <span class="media-count" id="media-count" aria-live="polite"></span>
                <input type="file" id="media-input" accept="image/jpeg,image/png,image/webp" multiple hidden>
            </div>
            <div class="media-thumbs" id="media-thumbs"></div>
        </div>

        <label class="sr-only" for="editor-content">正文</label>
        <div class="editor-content" id="editor-content" contenteditable="true"
             role="textbox" aria-multiline="true" data-placeholder="写点什么..."><c:if test="${editing}"><c:out value="${article.content}" escapeXml="false"/></c:if></div>

        <p class="editor-error" id="editor-error" role="alert" hidden></p>

        <div class="editor-bar">
            <div class="editor-bar-left">
                <label class="sr-only" for="editor-category">分类</label>
                <select class="qx-select" id="editor-category">
                    <option value="">选择分类</option>
                    <c:forEach var="opt" items="${categories}">
                        <option value="${opt.id}" data-path="${fn:escapeXml(opt.path)}"
                                ${editing and article.categoryId == opt.id ? 'selected' : ''}>
                            <c:out value="${opt.path}"/>
                        </option>
                    </c:forEach>
                </select>
                <span class="editor-count" id="editor-count">0 字</span>
            </div>
            <div class="editor-bar-right">
                <c:if test="${editing and not empty article.rejectReason}">
                    <span class="editor-status-note" style="color:#9a3412">
                        <c:out value="${article.rejectReason}"/>
                    </span>
                </c:if>
            </div>
        </div>
    </section>

    <aside class="editor-aside" aria-label="封面预览">
        <div class="cover-preview" id="cover-preview">
            <span class="cover-cat" id="preview-cat">未分类</span>
            <strong class="cover-title" id="preview-title">未命名</strong>
        </div>
        <p class="preview-label">封面预览</p>
    </aside>
</main>

<%@ include file="common/b-scripts.jspf" %>
</body>
</html>
