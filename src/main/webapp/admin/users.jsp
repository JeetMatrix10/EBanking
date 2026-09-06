<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>Manage Customer - EBanking Admin</title>
</head>
<body>
	<h2>Update Customer Details</h2>

	<form action="../manageCustomer" method="post">
		<input type="hidden" name="action" value="update"> <label>Customer
			ID (CID):</label><br> <input type="text" name="cid" required><br>
		<br> <label>Phone Number:</label><br> <input type="text"
			name="phone" required><br> <br> <label>Email:</label><br>
		<input type="email" name="email" required><br> <br>
		<input type="submit" value="Update">
	</form>

	<hr>

	<h2>Delete Customer</h2>
	<form action="../manageCustomer" method="post">
		<input type="hidden" name="action" value="delete"> <label>Customer
			ID (CID):</label><br> <input type="text" name="cid" required><br>
		<br> <input type="submit" value="Delete">
	</form>

	<p>
		<a href="adminDashboard.jsp">Back to Admin Dashboard</a>
	</p>
</body>
</html>