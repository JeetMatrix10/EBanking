<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="model.Admin"%>
<%
// Why session.getAttribute("loggedInAdmin") is safe to assume non-null
// here without a manual check: AdminAuthFilter already guarantees this
// page is unreachable without a valid admin session — by the time this
// code runs, the filter has already done that verification for us.
Admin admin = (Admin) session.getAttribute("loggedInAdmin");
%>
<!DOCTYPE html>
<html>
<head>
<title>Admin Dashboard - EBanking</title>
</head>
<body>
	<h2>
		Welcome,
		<%=admin.getUsername()%>
		(Admin)
	</h2>

	<ul>
		<li><a href="account.jsp">Create Customer Account</a></li>
		<li><a href="accountUpd.jsp">Update Account Balance</a></li>
		<li><a href="users.jsp">Manage Customers (Update/Delete)</a></li>
		<li><a href="../adminLogout">Logout</a></li>
	</ul>
</body>
</html>