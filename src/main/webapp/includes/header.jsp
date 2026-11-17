<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Customer" %>
<%@ page import="model.Admin" %>
<%@ page import="utilities.ViewUtil" %>
<%
    // Why these come from request parameters: pages include this header with
    // <jsp:param name="title" .../>, <jsp:param name="nav" .../> (public,
    // customer or admin) and <jsp:param name="active" .../> (which menu item
    // to highlight). One header file then serves every kind of page.
    String ctx = request.getContextPath();
    String pageTitle = request.getParameter("title");
    String nav = request.getParameter("nav");
    String active = request.getParameter("active");
    if (pageTitle == null) pageTitle = "NetBanking";
    if (nav == null) nav = "public";
    if (active == null) active = "";

    Customer navCustomer = (Customer) session.getAttribute("loggedInCustomer");
    Admin navAdmin = (Admin) session.getAttribute("loggedInAdmin");

    String home = ctx + "/index.jsp";
    if ("customer".equals(nav)) home = ctx + "/dashboard.jsp";
    else if ("admin".equals(nav)) home = ctx + "/admin/adminDashboard.jsp";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= ViewUtil.escape(pageTitle) %> - EBanking</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>
<header class="navbar">
  <div class="navbar-inner">
    <a class="brand" href="<%= home %>">
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor"
           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 10l9-6 9 6"/><path d="M5 10v8M9 10v8M15 10v8M19 10v8"/><path d="M3 20h18"/>
      </svg>
      <span>EBanking</span>
    </a>

    <nav class="nav-links">
    <% if ("customer".equals(nav)) { %>
      <a href="<%= ctx %>/dashboard.jsp" class="<%= "dashboard".equals(active) ? "active" : "" %>">Dashboard</a>
      <a href="<%= ctx %>/deposit" class="<%= "deposit".equals(active) ? "active" : "" %>">Deposit</a>
      <a href="<%= ctx %>/withdraw" class="<%= "withdraw".equals(active) ? "active" : "" %>">Withdraw</a>
      <a href="<%= ctx %>/transfer" class="<%= "transfer".equals(active) ? "active" : "" %>">Transfer</a>
      <a href="<%= ctx %>/viewTransactions" class="<%= "history".equals(active) ? "active" : "" %>">History</a>
      <a href="<%= ctx %>/myFDs" class="<%= "fd".equals(active) ? "active" : "" %>">Fixed Deposits</a>
      <a href="<%= ctx %>/myRDs" class="<%= "rd".equals(active) ? "active" : "" %>">Recurring</a>
      <a href="<%= ctx %>/profile" class="<%= "profile".equals(active) ? "active" : "" %>">Profile</a>
      <% if (navCustomer != null) { %><span class="nav-user"><%= ViewUtil.escape(navCustomer.getName()) %></span><% } %>
      <a href="<%= ctx %>/logout">Logout</a>
    <% } else if ("admin".equals(nav)) { %>
      <a href="<%= ctx %>/admin/adminDashboard.jsp" class="<%= "dashboard".equals(active) ? "active" : "" %>">Dashboard</a>
      <a href="<%= ctx %>/admin/account.jsp" class="<%= "account".equals(active) ? "active" : "" %>">Create Account</a>
      <a href="<%= ctx %>/admin/accountUpd.jsp" class="<%= "balance".equals(active) ? "active" : "" %>">Update Balance</a>
      <a href="<%= ctx %>/admin/users.jsp" class="<%= "customers".equals(active) ? "active" : "" %>">Customers</a>
      <% if (navAdmin != null) { %><span class="nav-user"><%= ViewUtil.escape(navAdmin.getUsername()) %> (Admin)</span><% } %>
      <a href="<%= ctx %>/adminLogout">Logout</a>
    <% } else { %>
      <a href="<%= ctx %>/index.jsp" class="<%= "home".equals(active) ? "active" : "" %>">Home</a>
      <a href="<%= ctx %>/login.jsp" class="<%= "login".equals(active) ? "active" : "" %>">Login</a>
      <a href="<%= ctx %>/register.jsp" class="<%= "register".equals(active) ? "active" : "" %>">Register</a>
      <a href="<%= ctx %>/admin/adminLogin.jsp">Admin</a>
    <% } %>
    </nav>
  </div>
</header>
<main class="container">