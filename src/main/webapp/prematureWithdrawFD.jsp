<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="model.FixedDeposit"%>
<%
List<FixedDeposit> activeFDs = (List<FixedDeposit>) request.getAttribute("activeFDs");
if (activeFDs == null) {
	response.sendRedirect("prematureWithdrawFD");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Premature FD Withdrawal - EBanking</title>
</head>
<body>
	<h2>Withdraw Fixed Deposit Before Maturity</h2>
	<p>
		<em>Note: a 1% penalty is deducted from your interest rate, and
			interest is calculated only for the actual time held, not the full
			term.</em>
	</p>

	<form action="prematureWithdrawFD" method="post">
		<label>Select Fixed Deposit:</label><br> <select name="fdId"
			required>
			<%
			for (FixedDeposit fd : activeFDs) {
			%>
			<option value="<%=fd.getFdId()%>">FD #<%=fd.getFdId()%>
				— Account
				<%=fd.getAccno()%> — Amount:
				<%=fd.getAmount()%> — Rate:
				<%=fd.getInterestRate()%>%
			</option>
			<%
			}
			%>
		</select><br> <br> <input type="submit" value="Withdraw Early">
	</form>

	<p>
		<a href="dashboard.jsp">Back to Dashboard</a>
	</p>
</body>
</html>