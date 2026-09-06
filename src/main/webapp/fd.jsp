<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="model.Account"%>
<%
List<Account> accounts = (List<Account>) request.getAttribute("accounts");
if (accounts == null) {
	response.sendRedirect("bookFD");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Fixed Deposit - EBanking</title>
</head>
<body>
	<h2>Book a Fixed Deposit</h2>

	<form action="bookFD" method="post">
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
		</select><br> <br> <label>Number of Years:</label><br> <input
			type="number" name="years" min="1" required><br> <br>
		<label>Interest Rate (%):</label><br> <input type="number"
			step="0.01" name="interestRate" required><br> <br>
		<label>Amount:</label><br> <input type="number" step="0.01"
			min="0.01" name="amount" required><br> <br> <label>
			<input type="checkbox" name="testMode"> Test mode (treat 1
			Year as 5 minutes, for demo purposes)
		</label><br> <br> <input type="submit" value="Book FD">
	</form>

	<p>
		<a href="dashboard.jsp">Back to Dashboard</a>
	</p>
</body>
</html>