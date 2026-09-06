<%-- myRDs.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="model.RecurringDeposit"%>
<!DOCTYPE html>
<html>
<head>
<title>My Recurring Deposits - EBanking</title>
</head>
<body>
	<h2>My Recurring Deposits</h2>
	<%
	List<RecurringDeposit> rds = (List<RecurringDeposit>) request.getAttribute("rds");
	if (rds == null) {
		response.sendRedirect("myRDs");
		return;
	}
	%>
	<%
	if (rds.isEmpty()) {
	%>
	<p>You have no Recurring Deposits yet.</p>
	<%
	} else {
	%>
	<table border="1" cellpadding="5">
		<tr>
			<th>RD ID</th>
			<th>Account</th>
			<th>Monthly Amt</th>
			<th>Months</th>
			<th>Interest Rate</th>
			<th>Paid</th>
			<th>Next Debit</th>
			<th>Status</th>
		</tr>
		<%
		for (RecurringDeposit rd : rds) {
		%>
		<tr>
			<td><%=rd.getRdId()%></td>
			<td><%=rd.getAccno()%></td>
			<td><%=rd.getMonthlyAmount()%></td>
			<td><%=rd.getNoOfMonths()%></td>
			<td><%=rd.getInterestRate()%>%</td>
			<td><%=rd.getInstallmentsPaid()%></td>
			<td><%=rd.getNextDebitDate()%></td>
			<td><%=rd.getStatus()%></td>
		</tr>
		<%
		}
		%>
	</table>
	<%
	}
	%>
	<p>
		<a href="dashboard.jsp">Back to Dashboard</a>
	</p>
</body>
</html>