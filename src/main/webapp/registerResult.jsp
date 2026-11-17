<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    String generatedCid = (String) request.getAttribute("generatedCid");
    Boolean success = (Boolean) request.getAttribute("success");

    // Guard first, before any HTML is sent (a redirect after output has
    // started would fail).
    if (success == null) {
        response.sendRedirect("register.jsp");
        return;
    }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Registration"/>
    <jsp:param name="nav" value="public"/>
    <jsp:param name="active" value="register"/>
</jsp:include>

<div class="card form-card result">
<% if (success) { %>
    <div class="icon icon-success">&#10003;</div>
    <h1 class="page-title">Registration successful</h1>
    <p class="muted">Your Customer ID is</p>
    <div class="big-id"><%= ViewUtil.escape(generatedCid) %></div>
    <p class="muted">Save this ID. You will need it to log in.</p>
    <a class="btn btn-primary" href="<%= ctx %>/login.jsp">Go to Login</a>
<% } else { %>
    <div class="icon icon-error">&#10005;</div>
    <h1 class="page-title">Registration failed</h1>
    <p class="muted">Please check your details and try again. An email address can only be registered once.</p>
    <a class="btn btn-primary" href="<%= ctx %>/register.jsp">Try Again</a>
<% } %>
</div>

<jsp:include page="/includes/footer.jsp"/>