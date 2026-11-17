<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="utilities.ViewUtil"%>
<%
    // Receives plain (unescaped) text via jsp:param and escapes it here, once.
    String ctx = request.getContextPath();
    boolean ok = "true".equals(request.getParameter("ok"));
    String rTitle = request.getParameter("rtitle");
    String message = request.getParameter("message");
    // A servlet that rejected the input sets "errorMessage" with the specific
    // reason. On a failure, show it instead of the page's generic default text.
    String err = (String) request.getAttribute("errorMessage");
    if (!ok && err != null && !err.isEmpty()) {
        message = err;
    }
    String big = request.getParameter("big");
    String note = request.getParameter("note");
    String href = request.getParameter("href");
    String label = request.getParameter("label");
    if (href == null || href.isEmpty()) href = "/dashboard.jsp";
    if (label == null || label.isEmpty()) label = "Back to Dashboard";
%>
<div class="card form-card result">
	<div class="icon <%= ok ? "icon-success" : "icon-error" %>"><%= ok ? "&#10003;" : "&#10005;" %></div>
	<h1 class="page-title"><%= ViewUtil.escape(rTitle) %></h1>
	<% if (big != null && !big.isEmpty()) { %><div class="big-id"><%= ViewUtil.escape(big) %></div>
	<% } %>
	<% if (message != null && !message.isEmpty()) { %><p class="muted"><%= ViewUtil.escape(message) %></p>
	<% } %>
	<% if (note != null && !note.isEmpty()) { %><div
		class="alert alert-warning"><%= ViewUtil.escape(note) %></div>
	<% } %>
	<a class="btn btn-primary" href="<%= ctx + href %>"><%= ViewUtil.escape(label) %></a>
</div>