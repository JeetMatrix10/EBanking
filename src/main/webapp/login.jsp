<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Login - EBanking</title>
</head>
<body>
    <h2>Customer Login</h2>

    <!-- Why this check exists: reads the "error" query parameter LoginServlet
         redirects with on failed login, and shows a message only if it's
         present — so the form looks clean on a first visit. -->
    <% if ("true".equals(request.getParameter("error"))) { %>
        <p style="color:red;">Invalid Customer ID or Password. Please try again.</p>
    <% } %>

    <form action="login" method="post">
        <label>Customer ID or Email:</label><br>
        <input type="text" name="identifier" required><br><br>

        <label>Password:</label><br>
        <input type="password" name="password" required><br><br>

        <input type="submit" value="Login">
    </form>

    <p>Don't have an account? <a href="register.jsp">Register here</a></p>
</body>
</html>