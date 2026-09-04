<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Customer" %>

<%
    // Why this is declared here, BEFORE the <!DOCTYPE html> line, instead of
    // further down in the body: we need to check for null and potentially
    // redirect BEFORE any HTML has been sent to the browser. If we redirect
    // after HTML has already started printing, the browser would receive a
    // broken half-page plus a redirect instruction, which doesn't work correctly.
    Customer customer = (Customer) request.getAttribute("customer");

    // Why this check matters: if someone reaches this JSP WITHOUT going
    // through ProfileServlet first (typed the URL directly, used a bookmark,
    // hit refresh at the wrong moment), "customer" will be null here. Instead
    // of crashing with a NullPointerException further down when we call
    // customer.getCid(), we redirect them to the servlet that actually
    // prepares this data correctly, then stop this page from continuing.
    if (customer == null) {
        response.sendRedirect("profile");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>My Profile - EBanking</title>
</head>
<body>
    <h2>My Profile</h2>

    <%
        // Why this is declared separately, after the null-check block above:
        // updateSuccess doesn't need the same "redirect if missing" guard —
        // it's allowed to legitimately be null (e.g. on first visit to this
        // page, before any update has ever been submitted), so we just check
        // it normally with an if/else instead of redirecting.
        Boolean updateSuccess = (Boolean) request.getAttribute("updateSuccess");

        if (updateSuccess != null) {
            if (updateSuccess) {
    %>
                <p style="color:green;">Profile updated successfully.</p>
    <%
            } else {
    %>
                <p style="color:red;">Update failed. Please try again.</p>
    <%
            }
        }
    %>

    <!-- Why CID, name, PAN, Aadhaar are shown as plain text, not input
         fields: these are the same identity/KYC fields we decided in
         updateCustomer() shouldn't be editable — showing them as inputs
         here would be misleading, implying they CAN be changed when
         submitting the form wouldn't actually update them. -->
    <p><strong>Customer ID:</strong> <%= customer.getCid() %></p>
    <p><strong>Name:</strong> <%= customer.getName() %></p>
    <p><strong>PAN:</strong> <%= customer.getPanno() %></p>
    <p><strong>Aadhaar:</strong> <%= customer.getAadhaarno() %></p>

    <form action="profile" method="post">
        <label>Phone Number:</label><br>
        <input type="text" name="phone" value="<%= customer.getPhone() %>" required><br><br>

        <label>Email:</label><br>
        <input type="email" name="email" value="<%= customer.getEmail() %>" required><br><br>

        <!-- New Toggle Option -->
        <input type="checkbox" id="changePwdToggle" onclick="togglePasswordSection()">
        <label for="changePwdToggle">Change Password</label><br><br>

        <!-- Hidden Password Section -->
        <div id="passwordSection" style="display: none;">
            <label>New Password:</label><br>
            <input type="password" name="password" id="newPasswordInput"><br><br>
        </div>

        <input type="submit" value="Update Profile">
    </form>

    <p><a href="dashboard.jsp">Back to Dashboard</a></p>

    <!-- JavaScript to handle the toggle -->
    <script>
        function togglePasswordSection() {
            var toggle = document.getElementById("changePwdToggle");
            var section = document.getElementById("passwordSection");
            var input = document.getElementById("newPasswordInput");

            if (toggle.checked) {
                section.style.display = "block"; // Show the field
            } else {
                section.style.display = "none";  // Hide the field
                input.value = "";               // Clear any typed password
            }
        }
    </script>
</body>
</html>