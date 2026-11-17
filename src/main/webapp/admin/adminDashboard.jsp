<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Admin" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    Admin admin = (Admin) session.getAttribute("loggedInAdmin");   // AdminAuthFilter guarantees this exists
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Admin Dashboard"/>
    <jsp:param name="nav" value="admin"/>
    <jsp:param name="active" value="dashboard"/>
</jsp:include>

<h1 class="page-title">Welcome, <%= ViewUtil.escape(admin.getUsername()) %></h1>
<p class="page-sub">Administration</p>

<div class="grid grid-3">
    <a class="tile" href="<%= ctx %>/admin/account.jsp"><strong>Create Account</strong><span>Open a new account for a customer</span></a>
    <a class="tile" href="<%= ctx %>/admin/accountUpd.jsp"><strong>Update Balance</strong><span>Manual balance adjustment</span></a>
    <a class="tile" href="<%= ctx %>/admin/users.jsp"><strong>Manage Customers</strong><span>Update or delete a customer</span></a>
</div>

<jsp:include page="/includes/footer.jsp"/>