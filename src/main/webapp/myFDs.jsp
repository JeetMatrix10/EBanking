<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.FixedDeposit" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Fixed Deposits - EBanking</title>
</head>
<body>
    <h2>My Fixed Deposits</h2>

    <%
        // Why the same "if null, redirect to servlet" guard from
        // profile.jsp is used here too: this JSP has the exact same risk —
        // if opened directly instead of via /myFDs, "fds" would be null and
        // the loop below would throw a NullPointerException.
        List<FixedDeposit> fds = (List<FixedDeposit>) request.getAttribute("fds");
        if (fds == null) {
            response.sendRedirect("myFDs");
            return;
        }
    %>

    <% if (fds.isEmpty()) { %>
        <p>You have no Fixed Deposits yet.</p>
    <% } else { %>
        <table border="1" cellpadding="5">
            <tr>
                <th>FD ID</th>
                <th>Account No</th>
                <th>Amount</th>
                <th>Years</th>
                <th>Interest Rate</th>
                <th>Booked On</th>
                <th>Matures On</th>
                <th>Status</th>
            </tr>
            <% for (FixedDeposit fd : fds) { %>
                <tr>
                    <td><%= fd.getFdId() %></td>
                    <td><%= fd.getAccno() %></td>
                    <td><%= fd.getAmount() %></td>
                    <td><%= fd.getNoOfYears() %></td>
                    <td><%= fd.getInterestRate() %>%</td>
                    <td><%= fd.getBookDate() %></td>
                    <td><%= fd.getMaturityDate() %></td>
                    <td><%= fd.getStatus() %></td>
                </tr>
            <% } %>
        </table>
    <% } %>

    <p><a href="prematureWithdrawFD">Withdraw an FD Early</a></p>
    <p><a href="dashboard.jsp">Back to Dashboard</a></p>
</body>
</html>