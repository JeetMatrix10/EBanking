<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    Boolean success = (Boolean) request.getAttribute("success");
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (success == null) { response.sendRedirect(request.getContextPath() + "/withdraw"); return; }
    String ok = success ? "true" : "false";
    String rtitle = success ? "Withdrawal successful" : "Withdrawal failed";
    // The insufficient-balance message carries the available amount, so show it directly.
    String msg = success ? "The amount has been withdrawn from your account."
               : (errorMessage != null ? errorMessage : "We could not process the withdrawal. Please try again.");
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Withdraw"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="withdraw"/>
</jsp:include>
<jsp:include page="/includes/result.jsp">
    <jsp:param name="ok" value="<%= ok %>"/>
    <jsp:param name="rtitle" value="<%= rtitle %>"/>
    <jsp:param name="message" value="<%= msg %>"/>
</jsp:include>
<jsp:include page="/includes/footer.jsp"/>