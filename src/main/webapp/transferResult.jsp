<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
Boolean success = (Boolean) request.getAttribute("success");
String warningMessage = (String) request.getAttribute("warningMessage");
if (success == null) {
	response.sendRedirect("transfer");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Transfer Result - EBanking</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>Transfer successful!</h3>
	<%
	if (warningMessage != null) {
	%>
	<p style="color: orange;"><%=warningMessage%></p>
	<%
	}
	%>
	<%
	} else {
	%>
	<!-- Why "beneficiary account number", not "both account numbers":
             saccno comes from a dropdown of the customer's own accounts,
             already verified by isAccountOwnedByCustomer() before this point
             — only benaccno is still free-text and can genuinely be wrong. -->
	<h3>Transfer failed. Check the beneficiary account number and your
		available balance.</h3>
	<%
	}
	%>
	<a href="dashboard.jsp">Back to Dashboard</a>
</body>
</html>