<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>出错了 · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
</head>
<body class="error-body">
<main class="error-box">
    <div class="error-code">${empty code ? 404 : code}</div>
    <h1>这里没有你要找的内容</h1>
    <p><c:out value="${empty message ? '页面不存在或已被移除' : message}"/></p>
    <a class="btn btn-primary" href="${ctx}/">返回首页</a>
</main>
</body>
</html>
