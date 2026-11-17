<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String ctx = request.getContextPath(); %>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Manage Customers"/>
    <jsp:param name="nav" value="admin"/>
    <jsp:param name="active" value="customers"/>
</jsp:include>

<div class="card form-card">
    <h1 class="page-title">Update Customer</h1>
    <p class="page-sub">Contact details only. KYC details cannot be edited.</p>
    <form action="<%= ctx %>/manageCustomer" method="post">
        <input type="hidden" name="action" value="update">
        <div class="form-group">
            <label for="ucid">Customer ID</label>
            <input type="text" id="ucid" name="cid" required>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label for="uphone">Phone</label>
                <input type="text" id="uphone" name="phone" required>
            </div>
            <div class="form-group">
                <label for="uemail">Email</label>
                <input type="email" id="uemail" name="email" required>
            </div>
        </div>
        <button class="btn btn-primary btn-block" type="submit">Update</button>
    </form>
</div>

<div class="card form-card">
    <h1 class="page-title">Delete Customer</h1>
    <p class="page-sub">Blocked while the customer still has accounts.</p>
    <form action="<%= ctx %>/manageCustomer" method="post"
          onsubmit="return confirm('Delete this customer? This cannot be undone.');">
        <input type="hidden" name="action" value="delete">
        <div class="form-group">
            <label for="dcid">Customer ID</label>
            <input type="text" id="dcid" name="cid" required>
        </div>
        <button class="btn btn-danger btn-block" type="submit">Delete</button>
    </form>
</div>

<jsp:include page="/includes/footer.jsp"/>