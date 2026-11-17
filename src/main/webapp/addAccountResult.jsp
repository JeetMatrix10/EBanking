<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    Boolean success = (Boolean) request.getAttribute("success");
    String generatedAccno = (String) request.getAttribute("generatedAccno");
    if (success == null) { response.sendRedirect(request.getContextPath() + "/admin/account.jsp"); return; }
    String ok = success ? "true" : "false";
    String rtitle = success ? "Account created" : "Account creation failed";
    String big = (success && generatedAccno != null) ? generatedAccno : "";
    String msg = success ? "Share this account number with the customer."
                         : "Check that the Customer ID exists.";
    String href = success ? "/admin/adminDashboard.jsp" : "/admin/account.jsp";
    String label = success ? "Back to Admin Dashboard" : "Try Again";
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Create Account"/>
    <jsp:param name="nav" value="admin"/>
    <jsp:param name="active" value="account"/>
</jsp:include>
<jsp:include page="/includes/result.jsp">
    <jsp:param name="ok" value="<%= ok %>"/>
    <jsp:param name="rtitle" value="<%= rtitle %>"/>
    <jsp:param name="big" value="<%= big %>"/>
    <jsp:param name="message" value="<%= msg %>"/>
    <jsp:param name="href" value="<%= href %>"/>
    <jsp:param name="label" value="<%= label %>"/>
</jsp:include>
<jsp:include page="/includes/footer.jsp"/>