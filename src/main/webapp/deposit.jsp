<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Deposit - EBanking</title>
</head>
<body>
    <h2>Deposit Funds</h2>

    <form action="deposit" method="post">
        <label>Account Number:</label><br>
        <input type="text" name="accno" required><br><br>

        <label>Amount:</label><br>
        <input type="number" step="0.01" min="0.01" name="amount" required><br><br>

        <input type="submit" value="Deposit">
    </form>

    <p><a href="dashboard.jsp">Back to Dashboard</a></p>
</body>
</html>