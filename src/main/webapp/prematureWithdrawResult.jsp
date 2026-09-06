<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.math.BigDecimal"%>
<%
BigDecimal payoutAmount = (BigDecimal) request.getAttribute("payoutAmount");
Boolean notFound = (Boolean) request.getAttribute("notFound");
if (payoutAmount == null && notFound == null) {
	response.sendRedirect("prematureWithdrawFD");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Premature Withdrawal Result - EBanking</title>
</head>
<body>
	<%
	if (payoutAmount != null) {
	%>
	<h3>
		FD closed early. Amount credited (after penalty):
		<%=payoutAmount%></h3>
	<%
	} else if (notFound != null && notFound) {
	%>
	<h3>Access denied or FD not found.</h3>
	<%
	} else {
	%>
	<h3>Premature withdrawal failed.</h3>
	<%
	}
	%>
	<a href="dashboard.jsp">Back to Dashboard</a>
</body>
</html>