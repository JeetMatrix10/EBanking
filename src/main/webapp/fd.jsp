<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Account" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) { response.sendRedirect(ctx + "/bookFD"); return; }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Book Fixed Deposit"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="fd"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Book a Fixed Deposit</h1>
    <p class="page-sub">The amount is debited now and returned with interest at maturity.</p>
<% if (accounts.isEmpty()) { %>
    <div class="alert alert-warning">You do not have an account yet.</div>
<% } else { %>
    <form action="<%= ctx %>/bookFD" method="post">
        <div class="form-group">
            <label for="accno">Debit from account</label>
            <select id="accno" name="accno" required>
                <% for (Account acc : accounts) { %>
                    <option value="<%= ViewUtil.escape(acc.getAccno()) %>">
                        <%= ViewUtil.escape(acc.getAccno()) %> - <%= ViewUtil.escape(acc.getAccounttype()) %>
                        (<%= ViewUtil.money(acc.getBalance()) %>)
                    </option>
                <% } %>
            </select>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label for="years">Number of years</label>
                <input type="number" id="years" name="years" min="1" required>
            </div>
            <div class="form-group">
                <label for="interestRate">Interest rate (% p.a.)</label>
                <input type="number" id="interestRate" name="interestRate" step="0.01" min="0" required>
            </div>
        </div>
        <div class="form-group">
            <label for="amount">Amount (&#8377;)</label>
            <input type="number" id="amount" name="amount" step="0.01" min="0.01" required>
        </div>
        <div class="form-group">
            <label class="check"><input type="checkbox" name="testMode">
                <span>Test mode (demo): each year becomes 5 minutes</span></label>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Book FD</button>
    </form>
<% } %>
</div>

<jsp:include page="/includes/footer.jsp"/>