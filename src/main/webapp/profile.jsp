<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="model.Customer"%>
<%@ page import="utilities.ViewUtil"%>
<%
String ctx = request.getContextPath();
Customer customer = (Customer) request.getAttribute("customer");
if (customer == null) {
	response.sendRedirect(ctx + "/profile");
	return;
}
Boolean updateSuccess = (Boolean) request.getAttribute("updateSuccess");
String errorMessage = (String) request.getAttribute("errorMessage");
%>
<jsp:include page="/includes/header.jsp">
	<jsp:param name="title" value="My Profile" />
	<jsp:param name="nav" value="customer" />
	<jsp:param name="active" value="profile" />
</jsp:include>

<div class="card form-card wide">
	<h1 class="page-title">My Profile</h1>
	<p class="page-sub">Name, PAN and Aadhaar are KYC details and
		cannot be changed here.</p>

	<%
	if (updateSuccess != null) {
	%>
	<%
	if (updateSuccess) {
	%>
	<div class="alert alert-success">Profile updated successfully.</div>
	<%
	} else {
	%>
	<div class="alert alert-error"><%=errorMessage != null ? ViewUtil.escape(errorMessage) : "Update failed. Please try again."%></div>
	<%
	}
	%>
	<%
	}
	%>

	<div class="form-row">
		<div class="form-group">
			<label>Customer ID</label>
			<div><%=ViewUtil.escape(customer.getCid())%></div>
		</div>
		<div class="form-group">
			<label>Name</label>
			<div><%=ViewUtil.escape(customer.getName())%></div>
		</div>
	</div>
	<div class="form-row">
		<div class="form-group">
			<label>PAN</label>
			<div><%=ViewUtil.escape(customer.getPanno())%></div>
		</div>
		<div class="form-group">
			<label>Aadhaar</label>
			<div><%=ViewUtil.escape(customer.getAadhaarno())%></div>
		</div>
	</div>

	<form action="<%=ctx%>/profile" method="post">
		<div class="form-row">
			<div class="form-group">
				<label for="phone">Phone number</label> <input type="text"
					id="phone" name="phone" required
					value="<%=ViewUtil.escape(customer.getPhone())%>">
			</div>
			<div class="form-group">
				<label for="email">Email</label> <input type="email" id="email"
					name="email" required
					value="<%=ViewUtil.escape(customer.getEmail())%>">
			</div>
		</div>

		<div class="form-group">
			<label class="check"><input type="checkbox"
				id="changePwdToggle" onclick="togglePasswordSection()"> <span>Change
					password</span></label>
		</div>
		<div class="form-group" id="passwordSection" style="display: none;">
			<label for="newPasswordInput">New password</label> <input
				type="password" name="password" id="newPasswordInput" minlength="8">
			<div class="hint">Leave the box unticked to keep your current
				password.</div>
		</div>

		<button class="btn btn-primary btn-block" type="submit">Update
			Profile</button>
	</form>
</div>

<script>
	function togglePasswordSection() {
		var on = document.getElementById("changePwdToggle").checked;
		document.getElementById("passwordSection").style.display = on ? "block"
				: "none";
		if (!on) {
			document.getElementById("newPasswordInput").value = "";
		}
	}
</script>

<jsp:include page="/includes/footer.jsp" />