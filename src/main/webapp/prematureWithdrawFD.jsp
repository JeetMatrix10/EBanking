<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Premature FD Withdrawal - EBanking</title>
</head>
<body>
    <h2>Withdraw Fixed Deposit Before Maturity</h2>
    <p><em>Note: a 1% penalty is deducted from your interest rate, and interest is
       calculated only for the actual time held, not the full term.</em></p>

    <form action="prematureWithdrawFD" method="post">
        <label>FD ID:</label><br>
        <input type="number" name="fdId" required><br><br>

        <label>Account Number (to credit):</label><br>
        <input type="text" name="accno" required><br><br>

        <input type="submit" value="Withdraw Early">
    </form>

    <p><a href="dashboard.jsp">Back to Dashboard</a></p>
</body>
</html>