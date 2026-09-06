<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Account" %>

<%
    // Why the same null-check/redirect guard from profile.jsp is repeated
    // here: same reasoning — if someone opens deposit.jsp directly instead
    // of via /deposit, "accounts" would be null and the dropdown loop below
    // would throw a NullPointerException.
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) {
        response.sendRedirect("deposit");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
<title>Deposit - EBanking</title>
</head>
<body>
    <h2>Deposit Funds</h2>

    <form action="deposit" method="post">
        <label>Account Number:</label><br>
        <select name="accno" required>
            <%
                // Why we loop and print <option> tags in a scriptlet rather
                // than a static dropdown: the options are entirely dynamic,
                // different for every customer — there's no way to hardcode
                // them in the JSP.
                for (Account acc : accounts) {
            %>
                <option value="<%= acc.getAccno() %>">
                    <%= acc.getAccno() %> (<%= acc.getAccounttype() %> - Balance: <%= acc.getBalance() %>)
                </option>
            <% } %>
        </select><br><br>
        
        <label>Amount:</label><br>
        <input type="number" step="0.01" min="0.01" name="amount" required><br><br>

        <input type="submit" value="Deposit">
    </form>

    <p><a href="dashboard.jsp">Back to Dashboard</a></p>
</body>
</html>