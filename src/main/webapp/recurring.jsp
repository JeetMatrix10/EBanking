<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Account" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) { response.sendRedirect(ctx + "/bookRD"); return; }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Book Recurring Deposit"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="rd"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Book a Recurring Deposit</h1>
    <p class="page-sub">A fixed amount is debited every month, and you get it back with interest at the end.</p>
<% if (accounts.isEmpty()) { %>
    <div class="alert alert-warning">You do not have an account yet.</div>
<% } else { %>
    <form action="<%= ctx %>/bookRD" method="post">
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
                <label for="months">Number of months</label>
                <input type="number" id="months" name="months" min="1" required>
            </div>
            <div class="form-group">
                <label for="interestRate">Interest rate (% p.a.)</label>
                <input type="number" id="interestRate" name="interestRate" step="0.01" min="0" required>
            </div>
        </div>
        <div class="form-group">
            <label for="amount">Monthly amount (&#8377;)</label>
            <input type="number" id="amount" name="amount" step="0.01" min="0.01" required>
        </div>
        <div class="form-group">
            <label class="check"><input type="checkbox" name="testMode">
                <span>Test mode (demo): each monthly installment becomes 5 minutes</span></label>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Book RD</button>
    </form>
<% } %>
</div>

<jsp:include page="/includes/footer.jsp"/>