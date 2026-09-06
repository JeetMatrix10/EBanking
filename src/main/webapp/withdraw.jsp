<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="java.util.List"%>
<%@ page import="model.Account"%>
<%
List<Account> accounts = (List<Account>) request.getAttribute("accounts");
if (accounts == null) {
	response.sendRedirect("withdraw");
	return;
}
%><!DOCTYPE html>
<html>
<head>
<title>Withdraw - EBanking</title>
</head>
<body>
	<h2>Withdraw Funds</h2>

	<form action="withdraw" method="post">
		<label>Account Number:</label><br> <select name="accno" required>
			<%
			for (Account acc : accounts) {
			%>
			<option value="<%=acc.getAccno()%>">
				<%=acc.getAccno()%> (<%=acc.getAccounttype()%> - Balance:
				<%=acc.getBalance()%>)
			</option>
			<%
			}
			%>
		</select><br> <br> <label>Amount:</label><br> <input
			type="number" step="0.01" min="0.01" name="amount" required><br>
		<br> <input type="submit" value="Withdraw">
	</form>

	<p>
		<a href="dashboard.jsp">Back to Dashboard</a>
	</p>
</body>
</html>