<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String ctx = request.getContextPath(); %>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Update Balance"/>
    <jsp:param name="nav" value="admin"/>
    <jsp:param name="active" value="balance"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Update Account Balance</h1>
    <div class="alert alert-warning">This is a manual adjustment. It is logged as an ADMIN_ADJUSTMENT transaction.</div>
    <form action="<%= ctx %>/updateAccount" method="post">
        <div class="form-group">
            <label for="accno">Account number</label>
            <input type="text" id="accno" name="accno" placeholder="ACC00001" required>
        </div>
        <div class="form-group">
            <label for="balance">New balance (&#8377;)</label>
            <input type="number" id="balance" name="balance" step="0.01" required>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Update</button>
    </form>
</div>

<jsp:include page="/includes/footer.jsp"/>