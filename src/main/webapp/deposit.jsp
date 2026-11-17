<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Account" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();

    // Same servlet-first guard as before: this page needs the "accounts"
    // list that DepositServlet.doGet() prepares.
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) {
        response.sendRedirect("deposit");
        return;
    }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Deposit"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="deposit"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Deposit Funds</h1>
    <p class="page-sub">Add money to one of your accounts.</p>

<% if (accounts.isEmpty()) { %>
    <div class="alert alert-warning">You do not have an account yet.</div>
<% } else { %>
    <form action="<%= ctx %>/deposit" method="post">
        <div class="form-group">
            <label for="accno">Account</label>
            <select id="accno" name="accno" required>
                <% for (Account acc : accounts) { %>
                    <option value="<%= ViewUtil.escape(acc.getAccno()) %>">
                        <%= ViewUtil.escape(acc.getAccno()) %> - <%= ViewUtil.escape(acc.getAccounttype()) %>
                        (<%= ViewUtil.money(acc.getBalance()) %>)
                    </option>
                <% } %>
            </select>
        </div>
        <div class="form-group">
            <label for="amount">Amount (&#8377;)</label>
            <input type="number" id="amount" name="amount" step="0.01" min="0.01" required>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Deposit</button>
    </form>
<% } %>
</div>

<jsp:include page="/includes/footer.jsp"/>