package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.CustomerDao;
import model.Customer;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        CustomerDao dao = new CustomerDao();
        Customer customer = dao.validateLogin(identifier, password);

        if (customer != null) {
            // Why request.getSession() and not "new HttpSession()": you never
            // construct a session directly — the Servlet container manages
            // session objects internally. getSession() either finds this
            // browser's existing session or creates one automatically if
            // none exists yet.
            HttpSession session = request.getSession();

            // Why we store the whole Customer object, not just the cid string:
            // dashboard.jsp and other pages will want to display the
            // customer's name, email, etc. without hitting the database again
            // on every single page load.
            session.setAttribute("loggedInCustomer", customer);

            // Why sendRedirect instead of writing HTML directly here: this
            // sends the browser a fresh request to dashboard.jsp, so the URL
            // bar correctly shows /dashboard.jsp and the page can rely on
            // request.getSession() to check login status independently
            // (this matters once we add AuthFilter next).
            response.sendRedirect("dashboard.jsp");
        } else {
            // Why we redirect back to login.jsp with a query parameter
            // instead of writing an error message directly: keeps the login
            // page reusable as the single place that shows the form AND
            // any error, rather than splitting that logic across two files.
            response.sendRedirect("login.jsp?error=true");
        }
    }
}