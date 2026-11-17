<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    Boolean success = (Boolean) request.getAttribute("success");
    String warningMessage = (String) request.getAttribute("warningMessage");
    if (success == null) { response.sendRedirect(request.getContextPath() + "/transfer"); return; }
    String ok = success ? "true" : "false";
    String rtitle = success ? "Transfer successful" : "Transfer failed";
    String msg = success ? "The money has been sent."
                         : "Check the beneficiary account number and your available balance.";
    String note = (success && warningMessage != null) ? warningMessage : "";
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Fund Transfer"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="transfer"/>
</jsp:include>
<jsp:include page="/includes/result.jsp">
    <jsp:param name="ok" value="<%= ok %>"/>
    <jsp:param name="rtitle" value="<%= rtitle %>"/>
    <jsp:param name="message" value="<%= msg %>"/>
    <jsp:param name="note" value="<%= note %>"/>
</jsp:include>
<jsp:include page="/includes/footer.jsp"/>