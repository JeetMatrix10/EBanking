<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    Boolean success = (Boolean) request.getAttribute("success");
    if (success == null) { response.sendRedirect(request.getContextPath() + "/admin/accountUpd.jsp"); return; }
    String ok = success ? "true" : "false";
    String rtitle = success ? "Balance updated" : "Update failed";
    String msg = success ? "The new balance has been saved and logged as an ADMIN_ADJUSTMENT."
                         : "Update failed. Check the account number.";
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Update Balance"/>
    <jsp:param name="nav" value="admin"/>
    <jsp:param name="active" value="balance"/>
</jsp:include>
<jsp:include page="/includes/result.jsp">
    <jsp:param name="ok" value="<%= ok %>"/>
    <jsp:param name="rtitle" value="<%= rtitle %>"/>
    <jsp:param name="message" value="<%= msg %>"/>
    <jsp:param name="href" value="/admin/adminDashboard.jsp"/>
    <jsp:param name="label" value="Back to Admin Dashboard"/>
</jsp:include>
<jsp:include page="/includes/footer.jsp"/>