<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.RecurringDeposit" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    List<RecurringDeposit> rds = (List<RecurringDeposit>) request.getAttribute("rds");
    if (rds == null) { response.sendRedirect(ctx + "/myRDs"); return; }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="My Recurring Deposits"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="rd"/>
</jsp:include>

<h1 class="page-title">My Recurring Deposits</h1>
<div class="actions">
    <a class="btn btn-primary" href="<%= ctx %>/bookRD">Book new RD</a>
</div>

<% if (rds.isEmpty()) { %>
    <div class="alert alert-warning mt">You have no Recurring Deposits yet.</div>
<% } else { %>
    <div class="card mt table-wrap">
        <table class="data">
            <tr><th>RD ID</th><th>Account</th><th class="num">Monthly</th><th>Paid</th>
                <th class="num">Rate</th><th>Next debit</th><th>Status</th></tr>
            <% for (RecurringDeposit rd : rds) { %>
                <tr>
                    <td><%= rd.getRdId() %></td>
                    <td><%= ViewUtil.escape(rd.getAccno()) %></td>
                    <td class="num"><%= ViewUtil.money(rd.getMonthlyAmount()) %></td>
                    <td><%= rd.getInstallmentsPaid() %> / <%= rd.getNoOfMonths() %></td>
                    <td class="num"><%= rd.getInterestRate() %>%</td>
                    <td><%= "ACTIVE".equals(rd.getStatus()) ? ViewUtil.dateTime(rd.getNextDebitDate()) : "-" %></td>
                    <td><span class="badge <%= ViewUtil.badge(rd.getStatus()) %>"><%= ViewUtil.escape(rd.getStatus()) %></span>
                        <%= rd.isTestMode() ? "<span class=\"muted\">(test)</span>" : "" %></td>
                </tr>
            <% } %>
        </table>
    </div>
<% } %>

<jsp:include page="/includes/footer.jsp"/>