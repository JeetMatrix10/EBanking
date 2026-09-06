<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
Boolean success = (Boolean) request.getAttribute("success");
Integer testMinutesWait = (Integer) request.getAttribute("testMinutesWait");
if (success == null) {
	response.sendRedirect("bookFD");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Fixed Deposit Result - EBanking</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>Fixed Deposit booked successfully!</h3>
	<%
	if (testMinutesWait != null) {
	%>
	<p>
		<em>Test mode: this FD will mature in <%=testMinutesWait%>
			minute(s) instead of years. Check your account balance again after
			that time.
		</em>
	</p>
	<%
	}
	%>
	<%
	} else {
	%>
	<h3>FD booking failed. Check available balance.</h3>
	<%
	}
	%>
	<a href="dashboard.jsp">Back to Dashboard</a>
</body>
</html>