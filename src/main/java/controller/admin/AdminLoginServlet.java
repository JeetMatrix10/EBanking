package controller.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AdminDao;
import model.Admin;

@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        AdminDao dao = new AdminDao();
        Admin admin = dao.validateAdminLogin(username, password);

        if (admin != null) {
            HttpSession session = request.getSession();

            // Why "loggedInAdmin" as a different attribute name than
            // "loggedInCustomer": this lets us later write a SEPARATE filter
            // (AdminAuthFilter) that checks specifically for admin sessions,
            // without any risk of confusing an admin session with a customer one.
            session.setAttribute("loggedInAdmin", admin);
            response.sendRedirect("admin/adminDashboard.jsp");
        } else {
            response.sendRedirect("admin/adminLogin.jsp?error=true");
        }
    }
}