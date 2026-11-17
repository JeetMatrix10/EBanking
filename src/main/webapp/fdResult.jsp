<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    Boolean success = (Boolean) request.getAttribute("success");
    Integer testMinutesWait = (Integer) request.getAttribute("testMinutesWait");
    if (success == null) { response.sendRedirect(request.getContextPath() + "/bookFD"); return; }
    String ok = success ? "true" : "false";
    String rtitle = success ? "Fixed Deposit booked" : "FD booking failed";
    String msg = success ? "Your deposit is now active."
                         : "Please check your available balance and try again.";
    String note = (success && testMinutesWait != null)
            ? "Test mode: this FD will mature in " + testMinutesWait + " minute(s)." : "";
    String href = success ? "/myFDs" : "/bookFD";
    String label = success ? "View My Fixed Deposits" : "Try Again";
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Fixed Deposit"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="fd"/>
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