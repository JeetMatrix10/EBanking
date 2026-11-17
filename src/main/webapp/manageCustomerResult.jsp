<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String message = (String) request.getAttribute("message");
    if (message == null) { response.sendRedirect(request.getContextPath() + "/admin/adminDashboard.jsp"); return; }
    // The servlet passes one message string; every success message contains "successfully".
    String ok = message.contains("successfully") ? "true" : "false";
    String rtitle = "true".equals(ok) ? "Done" : "Not completed";
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Manage Customer"/>
    <jsp:param name="nav" value="admin"/>
    <jsp:param name="active" value="customers"/>
</jsp:include>
<jsp:include page="/includes/result.jsp">
    <jsp:param name="ok" value="<%= ok %>"/>
    <jsp:param name="rtitle" value="<%= rtitle %>"/>
    <jsp:param name="message" value="<%= message %>"/>
    <jsp:param name="href" value="/admin/users.jsp"/>
    <jsp:param name="label" value="Back to Customers"/>
</jsp:include>
<jsp:include page="/includes/footer.jsp"/>