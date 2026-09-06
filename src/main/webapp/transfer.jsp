<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="model.Account"%>
<%
List<Account> accounts = (List<Account>) request.getAttribute("accounts");
if (accounts == null) {
	response.sendRedirect("transfer");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Fund Transfer - EBanking</title>
</head>
<body>
	<h2>Fund Transfer</h2>
	<p>
		<em>Minimum balance of ₹5000.00 must be maintained in the sending
			account.</em>
	</p>

	<form action="transfer" method="post">
		<label>Your Account Number (SACCNO):</label><br> <select
			name="saccno" required>
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
		</select><br> <br> <label>Beneficiary Account Number:</label><br>
		<input type="text" name="benaccno" required><br> <br>
		<label>Amount:</label><br> <input type="number" step="0.01"
			min="0.01" name="amount" required><br> <br> <input
			type="submit" value="Transfer">
	</form>

	<p>
		<a href="dashboard.jsp">Back to Dashboard</a>
	</p>
</body>
</html>