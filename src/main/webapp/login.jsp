<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Login"/>
    <jsp:param name="nav" value="public"/>
    <jsp:param name="active" value="login"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Customer Login</h1>
    <p class="page-sub">Sign in with your Customer ID or registered email.</p>

    <% if ("true".equals(request.getParameter("error"))) { %>
        <div class="alert alert-error">Invalid Customer ID / email or password. Please try again.</div>
    <% } %>

    <form action="<%= ctx %>/login" method="post">
        <div class="form-group">
            <label for="identifier">Customer ID or Email</label>
            <input type="text" id="identifier" name="identifier" required>
        </div>
        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Login</button>
    </form>

    <p class="hint mt">Don't have an account? <a href="<%= ctx %>/register.jsp">Register here</a></p>
</div>

<jsp:include page="/includes/footer.jsp"/>