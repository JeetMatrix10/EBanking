<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
Boolean success = (Boolean) request.getAttribute("success");
if (success == null) {
	response.sendRedirect("admin/accountUpd.jsp");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Update Account Result - EBanking Admin</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>Account balance updated successfully.</h3>
	<%
	} else {
	%>
	<h3>Update failed. Check the account number.</h3>
	<%
	}
	%>
	<a href="admin/adminDashboard.jsp">Back to Admin Dashboard</a>
</body>
</html>