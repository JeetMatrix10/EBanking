<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
Boolean success = (Boolean) request.getAttribute("success");
if (success == null) {
	response.sendRedirect("deposit");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Deposit Result - EBanking</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>Deposit successful!</h3>
	<%
	} else {
	%>
	<h3>Deposit failed due to a system error. Please try again.</h3>
	<%
	}
	%>
	<a href="dashboard.jsp">Back to Dashboard</a>
</body>
</html>