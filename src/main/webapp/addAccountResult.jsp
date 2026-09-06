<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
String generatedAccno = (String) request.getAttribute("generatedAccno");
Boolean success = (Boolean) request.getAttribute("success");
if (success == null) {
	response.sendRedirect("admin/account.jsp");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Add Account Result - EBanking Admin</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>
		Account created successfully! Account No:
		<%=generatedAccno%></h3>
	<a href="admin/adminDashboard.jsp">Back to Dashboard</a>
	<%
	} else {
	%>
	<h3>Account creation failed. Check the Customer ID exists.</h3>
	<a href="admin/account.jsp">Try Again</a>
	<%
	}
	%>
</body>
</html>s