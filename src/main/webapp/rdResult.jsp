<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
Boolean success = (Boolean) request.getAttribute("success");
Boolean testMode = (Boolean) request.getAttribute("testMode");
if (success == null) {
	response.sendRedirect("bookRD");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Recurring Deposit Result - EBanking</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>Recurring Deposit booked successfully!</h3>
	<%
	if (testMode != null && testMode) {
	%>
	<p>
		<em>Test mode: installments debit every 5 minutes instead of
			monthly.</em>
	</p>
	<%
	}
	%>
	<%
	} else {
	%>
	<h3>RD booking failed. Check available balance.</h3>
	<%
	}
	%>
	<a href="dashboard.jsp">Back to Dashboard</a>
</body>
</html>