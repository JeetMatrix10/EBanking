<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Add Account - EBanking Admin</title>
</head>
<body>
    <h2>Create New Account</h2>

    <form action="../addAccount" method="post">
        <label>Customer ID (CID):</label><br>
        <input type="text" name="cid" required><br><br>

        <label>Open Date:</label><br>
        <input type="date" name="opendate" required><br><br>

        <label>Initial Balance:</label><br>
        <input type="number" step="0.01" name="balance" required><br><br>

        <label>Account Type:</label><br>
        <select name="accounttype">
            <option value="Saving">Saving</option>
            <option value="Current">Current</option>
        </select><br><br>

        <input type="submit" value="Add Account">
    </form>
</body>
</html>