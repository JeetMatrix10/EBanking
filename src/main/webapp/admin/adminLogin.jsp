<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>Admin Login - EBanking</title>
</head>
<body>
	<h2>Admin Login</h2>

	<%
	if ("true".equals(request.getParameter("error"))) {
	%>
	<p style="color: red;">Invalid username or password.</p>
	<%
	}
	%>

	<!-- Why action="../adminLogin" (with ../): this JSP lives inside the
         /admin/ folder, but the servlet is mapped to /adminLogin at the
         root level, not /admin/adminLogin — so we need to step back up
         one folder in the path. -->
	<form action="../adminLogin" method="post">
		<label>Username:</label><br> <input type="text" name="username"
			required><br> <br> <label>Password:</label><br>
		<input type="password" name="password" required><br> <br>
		<input type="submit" value="Login">
	</form>
</body>
</html>