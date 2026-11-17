<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    Boolean success = (Boolean) request.getAttribute("success");
    Boolean testMode = (Boolean) request.getAttribute("testMode");
    if (success == null) { response.sendRedirect(request.getContextPath() + "/bookRD"); return; }
    String ok = success ? "true" : "false";
    String rtitle = success ? "Recurring Deposit booked" : "RD booking failed";
    String msg = success ? "Installments will be debited automatically."
                         : "Please check your details and try again.";
    String note = (success && testMode != null && testMode)
            ? "Test mode: installments are debited every 5 minutes instead of monthly." : "";
    String href = success ? "/myRDs" : "/bookRD";
    String label = success ? "View My Recurring Deposits" : "Try Again";
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Recurring Deposit"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="rd"/>
</jsp:include>
<jsp:include page="/includes/result.jsp">
    <jsp:param name="ok" value="<%= ok %>"/>
    <jsp:param name="rtitle" value="<%= rtitle %>"/>
    <jsp:param name="message" value="<%= msg %>"/>
    <jsp:param name="note" value="<%= note %>"/>
    <jsp:param name="href" value="<%= href %>"/>
    <jsp:param name="label" value="<%= label %>"/>
</jsp:include>
<jsp:include page="/includes/footer.jsp"/>