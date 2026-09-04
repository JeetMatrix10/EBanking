<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Update Account - EBanking Admin</title>
</head>
<body>
    <h2>Update Account Balance</h2>
    <p><em>Note: this is a manual adjustment and will be logged as an ADMIN_ADJUSTMENT transaction.</em></p>

    <form action="../updateAccount" method="post">
        <label>Account Number:</label><br>
        <input type="text" name="accno" required><br><br>

        <label>New Balance:</label><br>
        <input type="number" step="0.01" name="balance" required><br><br>

        <input type="submit" value="Update">
    </form>

    <p><a href="adminDashboard.jsp">Back to Admin Dashboard</a></p>
</body>
</html>