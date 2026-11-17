<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String ctx = request.getContextPath(); %>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Admin Login"/>
    <jsp:param name="nav" value="public"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Admin Login</h1>
    <p class="page-sub">Bank staff only.</p>
    <% if ("true".equals(request.getParameter("error"))) { %>
        <div class="alert alert-error">Invalid username or password.</div>
    <% } %>
    <form action="<%= ctx %>/adminLogin" method="post">
        <div class="form-group">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" required>
        </div>
        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Login</button>
    </form>
</div>

<jsp:include page="/includes/footer.jsp"/>