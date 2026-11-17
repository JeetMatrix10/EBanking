<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.FixedDeposit" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    List<FixedDeposit> fds = (List<FixedDeposit>) request.getAttribute("fds");
    if (fds == null) { response.sendRedirect(ctx + "/myFDs"); return; }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="My Fixed Deposits"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="fd"/>
</jsp:include>

<h1 class="page-title">My Fixed Deposits</h1>
<div class="actions">
    <a class="btn btn-primary" href="<%= ctx %>/bookFD">Book new FD</a>
    <a class="btn btn-secondary" href="<%= ctx %>/prematureWithdrawFD">Withdraw an FD early</a>
</div>

<% if (fds.isEmpty()) { %>
    <div class="alert alert-warning mt">You have no Fixed Deposits yet.</div>
<% } else { %>
    <div class="card mt table-wrap">
        <table class="data">
            <tr><th>FD ID</th><th>Account</th><th class="num">Amount</th><th>Years</th>
                <th class="num">Rate</th><th>Booked</th><th>Matures</th><th>Status</th></tr>
            <% for (FixedDeposit fd : fds) { %>
                <tr>
                    <td><%= fd.getFdId() %></td>
                    <td><%= ViewUtil.escape(fd.getAccno()) %></td>
                    <td class="num"><%= ViewUtil.money(fd.getAmount()) %></td>
                    <td><%= fd.getNoOfYears() %></td>
                    <td class="num"><%= fd.getInterestRate() %>%</td>
                    <td><%= ViewUtil.dateTime(fd.getBookDate()) %></td>
                    <td><%= ViewUtil.dateTime(fd.getMaturityDate()) %></td>
                    <td><span class="badge <%= ViewUtil.badge(fd.getStatus()) %>"><%= ViewUtil.escape(fd.getStatus()) %></span></td>
                </tr>
            <% } %>
        </table>
    </div>
<% } %>

<jsp:include page="/includes/footer.jsp"/>