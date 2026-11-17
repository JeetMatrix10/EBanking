<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Customer" %>
<%@ page import="utilities.ViewUtil" %>
<%
    String ctx = request.getContextPath();
    Customer loggedIn = (Customer) session.getAttribute("loggedInCustomer");
%>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="title" value="Home"/>
    <jsp:param name="nav" value="public"/>
    <jsp:param name="active" value="home"/>
</jsp:include>

<section class="hero">
    <div>
        <h1>Secure net banking, anytime.</h1>
        <p>Manage your accounts, move money, and grow your savings with Fixed and Recurring Deposits, all in one place.</p>
        <ul class="check-list">
            <li>Log in with your Customer ID or email</li>
            <li>Instant deposits, withdrawals and transfers</li>
            <li>Fixed and Recurring Deposits with automatic maturity</li>
        </ul>
    </div>

    <div class="card">
    <% if (loggedIn != null) { %>
        <h2 class="page-title">Welcome back</h2>
        <p class="page-sub"><%= ViewUtil.escape(loggedIn.getName()) %></p>
        <a class="btn btn-primary btn-block" href="<%= ctx %>/dashboard.jsp">Go to Dashboard</a>
    <% } else { %>
        <h2 class="page-title">NetBanking Login</h2>
        <p class="page-sub">Sign in with your Customer ID or email.</p>
        <form action="<%= ctx %>/login" method="post">
            <div class="form-group">
                <label for="identifier">Customer ID or Email</label>
                <input type="text" id="identifier" name="identifier" required>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button class="btn btn-primary btn-block" type="submit">Login</button>
        </form>
        <p class="hint mt">New customer? <a href="<%= ctx %>/register.jsp">Register here</a></p>
    <% } %>
    </div>
</section>

<%-- Your wireframe has an "images of bank" strip. Put image files in
     webapp/images, then uncomment this block and fix the file names.
<div class="grid grid-3 mt">
    <img class="banner-img" src="<%= ctx %>/images/bank1.jpg" alt="Branch">
    <img class="banner-img" src="<%= ctx %>/images/bank2.jpg" alt="Customer service">
    <img class="banner-img" src="<%= ctx %>/images/bank3.jpg" alt="Online banking">
</div>
--%>

<h2 class="section-title">What you can do</h2>
<div class="grid grid-3">
    <div class="card feature"><h3>Accounts</h3><p>View every account you hold and its balance at a glance.</p></div>
    <div class="card feature"><h3>Fund Transfer</h3><p>Send money to any account, with full transaction history.</p></div>
    <div class="card feature"><h3>Fixed Deposits</h3><p>Lock in a rate and earn interest at maturity.</p></div>
    <div class="card feature"><h3>Recurring Deposits</h3><p>Save a fixed amount every month, automatically.</p></div>
</div>

<jsp:include page="/includes/footer.jsp"/>