<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Customer" %>
<%@ page import="model.Account" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard - EBanking</title>
</head>
<body>

<%
    // Why we read the session here without a null-check for "logged in":
    // AuthFilter already guarantees this page is never reached unless
    // loggedInCustomer exists in the session — so by the time this code
    // runs, we can safely assume it's there.
    Customer customer = (Customer) session.getAttribute("loggedInCustomer");
%>

    <h2>Welcome, <%= customer.getName() %></h2>
    <p>Customer ID: <%= customer.getCid() %></p>

    <ul>
        <li><a href="deposit.jsp">Deposit</a></li>
        <li><a href="withdraw.jsp">Withdraw</a></li>
        <li><a href="transfer.jsp">Fund Transfer</a></li>
        <li><a href="transactions.jsp">Transaction History</a></li>
        <li><a href="fd.jsp">Fixed Deposit</a></li>
        <li><a href="myFDs.jsp">My Fixed Deposits</a></li>
        <li><a href="prematureWithdrawFD.jsp">Withdraw FD Early</a></li>
        <li><a href="recurring.jsp">Recurring Deposit</a></li>
        <li><a href="myRDs">My Recurring Deposits</a></li>
        <li><a href="profile.jsp">My Profile</a></li>
        <li><a href="logout">Logout</a></li>
    </ul>

	<hr>
	<h3>Check Account Balance</h3>
	<form action="checkBalance" method="get">
    	<label>Account Number:</label>
    	<input type="text" name="accno" required>
    	<input type="submit" value="Check Balance">
	</form>

	<%
    	// Why this cast/check is wrapped in an if: this attribute is ONLY set
    	// when the page is reached via AccountServlet's forward() — a normal
    	// direct visit to dashboard.jsp (e.g. right after login) won't have it,
    	// so we must handle both cases without throwing a NullPointerException.
    	Account account = (Account) request.getAttribute("account");
		Boolean accessDenied = (Boolean) request.getAttribute("accessDenied");
		if (account != null) {
	%>
    	<p><strong>Account No:</strong> <%= account.getAccno() %></p>
    	<p><strong>Balance:</strong> <%= account.getBalance() %></p>
    	<p><strong>Account Type:</strong> <%= account.getAccounttype() %></p>
	<%
    	} else if (accessDenied != null && accessDenied) {
	%>
    	<p style="color:red;">Access denied. This account does not belong to you.</p>
	<%
    	} else if (request.getParameter("accno") != null) {
	%>
    	<p>No account found with that number.</p>
	<%
    	}
	%>

</body>
</html>