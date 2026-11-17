<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Account" %>
<%@ page import="model.Transaction" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) { response.sendRedirect(ctx + "/viewTransactions"); return; }
    List<Transaction> transactions = (List<Transaction>) request.getAttribute("transactions");
    Boolean accessDenied = (Boolean) request.getAttribute("accessDenied");
    String selected = (String) request.getAttribute("accno");
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Transaction History"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="history"/>
</jsp:include>

<h1 class="page-title">Transaction History</h1>
<p class="page-sub">Pick an account to see its activity.</p>

<div class="card">
    <form action="<%= ctx %>/viewTransactions" method="get" class="inline-form">
        <select name="accno" required>
            <% for (Account acc : accounts) { %>
                <option value="<%= ViewUtil.escape(acc.getAccno()) %>"
                    <%= acc.getAccno().equals(selected) ? "selected" : "" %>>
                    <%= ViewUtil.escape(acc.getAccno()) %> (<%= ViewUtil.escape(acc.getAccounttype()) %>)
                </option>
            <% } %>
        </select>
        <button class="btn btn-primary" type="submit">View</button>
    </form>
</div>

<% if (accessDenied != null && accessDenied) { %>
    <div class="alert alert-error mt">Access denied. This account does not belong to you.</div>
<% } else if (transactions != null && !transactions.isEmpty()) { %>
    <div class="card mt table-wrap">
        <table class="data">
            <tr><th>ID</th><th>Date</th><th>Type</th><th>From</th><th>To</th><th class="num">Amount</th></tr>
            <% for (Transaction t : transactions) {
                   boolean received = "TRANSFER".equals(t.getType()) && selected != null
                                      && selected.equals(t.getBenaccno()); %>
                <tr>
                    <td><%= t.getTransactionId() %></td>
                    <td><%= ViewUtil.dateTime(t.getTransdt()) %></td>
                    <td><%= ViewUtil.escape(t.getType()) %><%= received ? " (received)" : "" %></td>
                    <td><%= ViewUtil.escape(t.getSaccno()) %></td>
                    <td><%= t.getBenaccno() != null ? ViewUtil.escape(t.getBenaccno()) : "-" %></td>
                    <td class="num"><%= ViewUtil.money(t.getAmount()) %></td>
                </tr>
            <% } %>
        </table>
    </div>
<% } else if (selected != null) { %>
    <div class="alert alert-warning mt">No transactions found for this account.</div>
<% } %>

<jsp:include page="/includes/footer.jsp"/>