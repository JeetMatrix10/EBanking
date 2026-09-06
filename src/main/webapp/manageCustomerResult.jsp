<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
String message = (String) request.getAttribute("message");
if (message == null) {
	response.sendRedirect("admin/adminDashboard.jsp");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Manage Customer Result - EBanking Admin</title>
</head>
<body>
	<h3><%=message%></h3>
	<a href="admin/adminDashboard.jsp">Back to Admin Dashboard</a>
</body>
</html>