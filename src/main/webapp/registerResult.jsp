<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
// Why this reads request attributes rather than request parameters:
// this page is reached exclusively via RequestDispatcher.forward() from
// RegisterServlet, which sets these as ATTRIBUTES after already
// processing the registration — there's no form submission happening
// on this page itself.
String generatedCid = (String) request.getAttribute("generatedCid");
Boolean success = (Boolean) request.getAttribute("success");

// Why this redirect guard: same reasoning as profile.jsp and every other
// servlet-fed JSP in this project — if someone reaches this page
// directly (bookmark, typed URL) without going through
// RegisterServlet first, "success" would be null, and we shouldn't
// render a confusing blank result page.
if (success == null) {
	response.sendRedirect("register.jsp");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<title>Registration Result - EBanking</title>
</head>
<body>
	<%
	if (success) {
	%>
	<h3>
		Registration successful! Your Customer ID is:
		<%=generatedCid%></h3>
	<p>Please save this ID — you'll need it to log in.</p>
	<a href="login.jsp">Go to Login</a>
	<%
	} else {
	%>
	<h3>Registration failed. Please check your inputs and try again.</h3>
	<a href="register.jsp">Try Again</a>
	<%
	}
	%>
</body>
</html>