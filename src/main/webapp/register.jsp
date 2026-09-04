<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Register - EBanking</title>
</head>
<body>
    <h2>Customer Registration</h2>

    <!-- Why action="register" and method="post": this must match the
         @WebServlet("/register") mapping and the doPost() method we wrote.
         If this said method="get", doPost() would never run — you'd need
         a doGet() instead, or Tomcat would throw a 405 error. -->
    <form action="register" method="post">

        <!-- Why each input's "name" attribute matters: this is the exact
             string RegisterServlet.java uses in request.getParameter("cid"),
             etc. Typos here silently break the servlet with no compile-time
             warning, since JSP form field names aren't checked by the Java
             compiler. -->
        <label>Full Name:</label><br>
        <input type="text" name="name" required><br><br>

        <label>Phone Number:</label><br>
        <input type="text" name="phone" required><br><br>

        <label>Email:</label><br>
        <input type="email" name="email" required><br><br>

        <label>PAN Number:</label><br>
        <input type="text" name="panno"><br><br>

        <label>Aadhaar Number:</label><br>
        <input type="text" name="aadhaarno"><br><br>

        <label>Password:</label><br>
        <input type="password" name="password" required><br><br>

        <input type="submit" value="Register">
    </form>
</body>
</html>