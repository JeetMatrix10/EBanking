<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
Boolean success = (Boolean) request.getAttribute("success");
String errorMessage = (String) request.getAttribute("errorMessage");
if (success == null) {
	response.sendRedirect("withdraw");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Withdrawal Result - EBanking</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>Withdrawal successful!</h3>
	<%
	} else if (errorMessage != null) {
	%>
	<!-- Why this branch exists separately from the generic failure
             below: InsufficientBalanceException carries a SPECIFIC,
             meaningful message ("Available: X") that's worth showing the
             customer directly, unlike a generic system error where there's
             nothing more useful to say. -->
	<h3>
		Withdrawal failed:
		<%=errorMessage%></h3>
	<%
	} else {
	%>
	<h3>Withdrawal failed due to a system error. Please try again.</h3>
	<%
	}
	%>
	<a href="dashboard.jsp">Back to Dashboard</a>
</body>
</html>