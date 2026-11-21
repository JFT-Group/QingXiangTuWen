<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>注册 · 轻享图文</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
</head>
<body class="auth-body">
<main class="auth-shell">
    <a class="brand" href="${ctx}/">轻享<span>图文</span></a>
    <h1>注册</h1>
    <p class="auth-sub">注册后即可浏览、发布和管理自己的图文。</p>

    <c:if test="${not empty error}">
        <p class="alert" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <form class="auth-form" method="post" action="${ctx}/register">
        <label class="field">
            <span>用户名</span>
            <input type="text" name="username" value="${fn:escapeXml(form.username)}"
                   autocomplete="username" placeholder="4–50 位字母、数字或下划线" required autofocus>
        </label>
        <label class="field">
            <span>昵称</span>
            <input type="text" name="nickname" value="${fn:escapeXml(form.nickname)}"
                   autocomplete="nickname" placeholder="展示给其他读者" required>
        </label>
        <label class="field">
            <span>密码</span>
            <input type="password" name="password" autocomplete="new-password"
                   placeholder="6–72 位" required>
        </label>
        <button class="btn btn-primary btn-block" type="submit">创建账号</button>
    </form>

    <p class="auth-foot">已经有账号？<a href="${ctx}/login">登录</a></p>
</main>
</body>
</html>
