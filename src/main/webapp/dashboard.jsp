<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Customer" %>
<%@ page import="model.Account" %>
<%@ page import="dao.AccountDao" %>
<%@ page import="java.util.List" %>

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

    // Why this fetch happens directly in the JSP here, unlike deposit/withdraw/FD:
    // dashboard.jsp is reached straight after login (via sendRedirect), with
    // no servlet doGet() step forwarding to it first — adding one just for
    // this dropdown would mean rewriting the login flow. Keeping this one
    // inline lookup is a reasonable, contained exception, consistent with
    // dashboard.jsp already doing similar inline lookups elsewhere on this page.
    AccountDao dashboardAccountDao = new AccountDao();
    List<Account> myAccounts = dashboardAccountDao.getAccountsByCid(customer.getCid());
	%>

    <h2>Welcome, <%= customer.getName() %></h2>
    <p>Customer ID: <%= customer.getCid() %></p>

    <ul>
		<li><a href="deposit">Deposit</a></li>
    	<li><a href="withdraw">Withdraw</a></li>
    	<li><a href="transfer">Fund Transfer</a></li>
    	<li><a href="viewTransactions">Transaction History</a></li>
    	
    	<li><a href="bookFD">Fixed Deposit</a></li>
    	<li><a href="myFDs">My Fixed Deposits</a></li>
    	<li><a href="bookRD">Recurring Deposit</a></li>
    	<li><a href="myRDs">My Recurring Deposits</a></li>
    	<li><a href="profile">My Profile</a></li>
    	<li><a href="logout">Logout</a></li>    
    </ul>

	<hr>
	<h3>Check Account Balance</h3>
	<form action="checkBalance" method="get">
    	<label>Account Number:</label>
    	<select name="accno" required>
        <% for (Account acc : myAccounts) { %>
            <option value="<%= acc.getAccno() %>">
                <%= acc.getAccno() %> (<%= acc.getAccounttype() %>)
            </option>
        <% } %>
    	</select>
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