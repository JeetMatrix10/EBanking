<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.FixedDeposit" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    List<FixedDeposit> activeFDs = (List<FixedDeposit>) request.getAttribute("activeFDs");
    if (activeFDs == null) { response.sendRedirect(ctx + "/prematureWithdrawFD"); return; }
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Withdraw FD Early"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="fd"/>
</jsp:include>

<div class="card form-card wide">
    <h1 class="page-title">Withdraw a Fixed Deposit early</h1>
    <div class="alert alert-warning">A 1% penalty is deducted from your interest rate, and interest is paid only for the time the deposit was actually held.</div>

<% if (activeFDs.isEmpty()) { %>
    <div class="alert alert-warning">You have no active Fixed Deposits eligible for early withdrawal.</div>
<% } else { %>
    <form action="<%= ctx %>/prematureWithdrawFD" method="post"
          onsubmit="return confirm('Close this FD early? The penalty cannot be undone.');">
        <div class="form-group">
            <label for="fdId">Select Fixed Deposit</label>
            <select id="fdId" name="fdId" required>
                <% for (FixedDeposit fd : activeFDs) { %>
                    <option value="<%= fd.getFdId() %>">
                        FD #<%= fd.getFdId() %> - <%= ViewUtil.escape(fd.getAccno()) %>
                        - <%= ViewUtil.money(fd.getAmount()) %> @ <%= fd.getInterestRate() %>%
                    </option>
                <% } %>
            </select>
        </div>
        <button class="btn btn-danger btn-block" type="submit">Withdraw Early</button>
    </form>
<% } %>
</div>

<jsp:include page="/includes/footer.jsp"/>