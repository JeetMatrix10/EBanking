package controller.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Why this is a SEPARATE class from LogoutServlet, not a reused/shared one:
// same reasoning as AdminDao vs CustomerDao, and AuthFilter vs
// AdminAuthFilter — admin and customer sessions are intentionally isolated
// from each other throughout this project. A shared logout servlet would
// need to guess or check WHICH kind of session it's destroying, adding
// complexity for no real benefit over just having two small, obvious classes.
@WebServlet("/adminLogout")
public class AdminLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("admin/adminLogin.jsp");
    }
}