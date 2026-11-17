<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Account" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) { response.sendRedirect(ctx + "/transfer"); return; }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Fund Transfer"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="transfer"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Fund Transfer</h1>
    <p class="page-sub">Send money from your account to any other account.</p>
    <div class="alert alert-warning">A minimum balance of &#8377;5,000.00 should be maintained in the sending account. You will be warned if a transfer takes you below it.</div>
<% if (accounts.isEmpty()) { %>
    <div class="alert alert-warning">You do not have an account yet.</div>
<% } else { %>
    <form action="<%= ctx %>/transfer" method="post">
        <div class="form-group">
            <label for="saccno">From (your account)</label>
            <select id="saccno" name="saccno" required>
                <% for (Account acc : accounts) { %>
                    <option value="<%= ViewUtil.escape(acc.getAccno()) %>">
                        <%= ViewUtil.escape(acc.getAccno()) %> - <%= ViewUtil.escape(acc.getAccounttype()) %>
                        (<%= ViewUtil.money(acc.getBalance()) %>)
                    </option>
                <% } %>
            </select>
        </div>
        <div class="form-group">
            <label for="benaccno">Beneficiary account number</label>
            <input type="text" id="benaccno" name="benaccno" required>
        </div>
        <div class="form-group">
            <label for="amount">Amount (&#8377;)</label>
            <input type="number" id="amount" name="amount" step="0.01" min="0.01" required>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Transfer</button>
    </form>
<% } %>
</div>

<jsp:include page="/includes/footer.jsp"/>