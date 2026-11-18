<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>登录 · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
</head>
<body class="auth-body">
<main class="auth-shell">
    <a class="brand" href="${ctx}/">轻享<span>图文</span></a>
    <h1>登录</h1>
    <p class="auth-sub">用你的账号进入轻享图文。</p>

    <c:if test="${param.registered == '1'}">
        <p class="notice" role="status">注册成功，请登录。</p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="alert" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <form class="auth-form" method="post" action="${ctx}/login">
        <label class="field">
            <span>用户名</span>
            <input type="text" name="username" value="${fn:escapeXml(username)}"
                   autocomplete="username" placeholder="4–50 位字母、数字或下划线" required autofocus>
        </label>
        <label class="field">
            <span>口令</span>
            <input type="password" name="password" autocomplete="current-password"
                   placeholder="至少 6 位" required>
        </label>
        <button class="btn btn-primary btn-block" type="submit">登录</button>
    </form>

    <p class="auth-foot">还没有账号？<a href="${ctx}/register">注册</a></p>
</main>
</body>
</html>
