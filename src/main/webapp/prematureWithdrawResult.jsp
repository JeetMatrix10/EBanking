<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="utilities.ViewUtil" %>
<%
    BigDecimal payoutAmount = (BigDecimal) request.getAttribute("payoutAmount");
    Boolean notFound = (Boolean) request.getAttribute("notFound");
    if (payoutAmount == null && notFound == null) {
        response.sendRedirect(request.getContextPath() + "/prematureWithdrawFD");
        return;
    }
    boolean success = payoutAmount != null;
    String ok = success ? "true" : "false";
    String rtitle = success ? "FD closed early" : "Withdrawal failed";
    String msg = success ? "Amount credited after penalty: " + ViewUtil.money(payoutAmount)
               : (notFound != null && notFound ? "Access denied or FD not found." : "The early withdrawal could not be processed.");
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Withdraw FD Early"/>
    <jsp:param name="nav" value="customer"/>
    <jsp:param name="active" value="fd"/>
</jsp:include>
<jsp:include page="/includes/result.jsp">
    <jsp:param name="ok" value="<%= ok %>"/>
    <jsp:param name="rtitle" value="<%= rtitle %>"/>
    <jsp:param name="message" value="<%= msg %>"/>
    <jsp:param name="href" value="/myFDs"/>
    <jsp:param name="label" value="View My Fixed Deposits"/>
</jsp:include>
<jsp:include page="/includes/footer.jsp"/>