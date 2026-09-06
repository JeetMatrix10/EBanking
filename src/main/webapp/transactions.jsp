<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="model.Account"%>
<%@ page import="model.Transaction"%>
<%
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) {
        response.sendRedirect("viewTransactions");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
<title>Transaction History - EBanking</title>
</head>
<body>
	<h2>Transaction History</h2>

	<form action="viewTransactions" method="get">
		<label>Account Number:</label> <select name="accno" required>
			<% for (Account acc : accounts) { %>
			<option value="<%= acc.getAccno() %>">
				<%= acc.getAccno() %> (<%= acc.getAccounttype() %>)
			</option>
			<% } %>
		</select> <input type="submit" value="View">
	</form>

	<%
        // Why request.getAttribute, not request.getParameter, here: this page
        // is reached via forward() from the servlet, which already fetched
        // and set "transactions" as an ATTRIBUTE (a Java object), not a raw
        // request parameter (a string). getParameter would only work for
        // form-submitted strings, not for a List<Transaction> object.
        List<Transaction> transactions = (List<Transaction>) request.getAttribute("transactions");
        Boolean accessDenied = (Boolean) request.getAttribute("accessDenied");
    %>

	<% if (accessDenied != null && accessDenied) { %>
	<p style="color: red;">Access denied. This account does not belong
		to you.</p>

	<% } else if (transactions != null && !transactions.isEmpty()) { %>
	<table border="1" cellpadding="5">
		<tr>
			<th>Transaction ID</th>
			<th>From</th>
			<th>To</th>
			<th>Amount</th>
			<th>Type</th>
			<th>Date</th>
		</tr>
		<%
                // Why a plain for-loop instead of JSTL's <c:forEach>: JSTL
                // needs an extra tag library import/setup step — for a
                // project this size, a scriptlet loop is simpler and works
                // identically; we can switch to JSTL later if you want cleaner JSPs.
                for (Transaction t : transactions) {
            %>
		<tr>
			<td><%= t.getTransactionId() %></td>
			<td><%= t.getSaccno() %></td>
			<td><%= t.getBenaccno() != null ? t.getBenaccno() : "-" %></td>
			<td><%= t.getAmount() %></td>
			<td><%= t.getType() %></td>
			<td><%= t.getTransdt() %></td>
		</tr>
		<% } %>
	</table>
	<% } else if (request.getAttribute("accno") != null) { %>
	<p>No transactions found for this account.</p>
	<% } %>

	<p>
		<a href="dashboard.jsp">Back to Dashboard</a>
	</p>
</body>
</html>