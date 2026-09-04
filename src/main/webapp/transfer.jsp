<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Fund Transfer - EBanking</title>
</head>
<body>
    <h2>Fund Transfer</h2>
    <p><em>Minimum balance of ₹5000.00 must be maintained in the sending account.</em></p>

    <form action="transfer" method="post">
        <label>Your Account Number (SACCNO):</label><br>
        <input type="text" name="saccno" required><br><br>

        <label>Beneficiary Account Number:</label><br>
        <input type="text" name="benaccno" required><br><br>

        <label>Amount:</label><br>
        <input type="number" step="0.01" min="0.01" name="amount" required><br><br>

        <input type="submit" value="Transfer">
    </form>

    <p><a href="dashboard.jsp">Back to Dashboard</a></p>
</body>
</html>