<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.List" %>
<%@ page import="dao.AccountDao" %>
<%@ page import="model.Account" %>
<%@ page import="model.Customer" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();

    // AuthFilter guarantees a logged-in customer reaches this page.
    Customer customer = (Customer) session.getAttribute("loggedInCustomer");

    // Fetched inline because login redirects straight here, with no servlet
    // doGet() in front of it to supply the data (same reason as before).
    List<Account> myAccounts = new AccountDao().getAccountsByCid(customer.getCid());
    BigDecimal total = BigDecimal.ZERO;
    for (Account a : myAccounts) {
        total = total.add(a.getBalance());
    }

    // Set only when we arrive here via AccountServlet (/checkBalance).
    Account checked = (Account) request.getAttribute("account");
    Boolean accessDenied = (Boolean) request.getAttribute("accessDenied");
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Dashboard"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="dashboard"/>
</jsp:include>

<h1 class="page-title">Welcome, <%= ViewUtil.escape(customer.getName()) %></h1>
<p class="page-sub">Customer ID: <strong><%= ViewUtil.escape(customer.getCid()) %></strong></p>

<% if (myAccounts.isEmpty()) { %>
    <div class="alert alert-warning">You do not have an account yet. Please contact the bank to have one created.</div>
<% } else { %>
    <h2 class="section-title">Your accounts
        <span class="muted">&middot; total balance <%= ViewUtil.money(total) %></span></h2>
    <div class="grid grid-accounts">
        <% for (Account a : myAccounts) { %>
            <div class="account-card">
                <div class="account-type"><%= ViewUtil.escape(a.getAccounttype()) %> account</div>
                <div class="account-no"><%= ViewUtil.escape(a.getAccno()) %></div>
                <div class="account-label">Available balance</div>
                <div class="account-balance"><%= ViewUtil.money(a.getBalance()) %></div>
            </div>
        <% } %>
    </div>
<% } %>

<h2 class="section-title">Quick actions</h2>
<div class="grid grid-3">
    <a class="tile" href="<%= ctx %>/deposit"><strong>Deposit</strong><span>Add money to an account</span></a>
    <a class="tile" href="<%= ctx %>/withdraw"><strong>Withdraw</strong><span>Take money out</span></a>
    <a class="tile" href="<%= ctx %>/transfer"><strong>Fund Transfer</strong><span>Send money to another account</span></a>
    <a class="tile" href="<%= ctx %>/viewTransactions"><strong>Transaction History</strong><span>See past activity</span></a>
    <a class="tile" href="<%= ctx %>/bookFD"><strong>Book Fixed Deposit</strong><span>Lock in a rate</span></a>
    <a class="tile" href="<%= ctx %>/myFDs"><strong>My Fixed Deposits</strong><span>View your FDs</span></a>
    <a class="tile" href="<%= ctx %>/prematureWithdrawFD"><strong>Withdraw FD Early</strong><span>Close an FD before maturity</span></a>
    <a class="tile" href="<%= ctx %>/bookRD"><strong>Book Recurring Deposit</strong><span>Save every month</span></a>
    <a class="tile" href="<%= ctx %>/myRDs"><strong>My Recurring Deposits</strong><span>View your RDs</span></a>
    <a class="tile" href="<%= ctx %>/profile"><strong>My Profile</strong><span>Update contact details</span></a>
</div>

<% if (!myAccounts.isEmpty()) { %>
    <h2 class="section-title">Check a single account</h2>
    <div class="card">
        <form action="<%= ctx %>/checkBalance" method="get" class="inline-form">
            <select name="accno" required>
                <% for (Account a : myAccounts) { %>
                    <option value="<%= ViewUtil.escape(a.getAccno()) %>">
                        <%= ViewUtil.escape(a.getAccno()) %> (<%= ViewUtil.escape(a.getAccounttype()) %>)
                    </option>
                <% } %>
            </select>
            <button class="btn btn-primary" type="submit">Check Balance</button>
        </form>

        <% if (checked != null) { %>
            <div class="alert alert-success mt">
                Account <strong><%= ViewUtil.escape(checked.getAccno()) %></strong> balance:
                <strong><%= ViewUtil.money(checked.getBalance()) %></strong>
            </div>
        <% } else if (accessDenied != null && accessDenied) { %>
            <div class="alert alert-error mt">Access denied. This account does not belong to you.</div>
        <% } %>
    </div>
<% } %>

<jsp:include page="/includes/footer.jsp"/>