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
			session.setAttribute("loggedInAdmin", admin);

			// Why getContextPath() + "/..." here, matching AuthFilter and
			// AdminAuthFilter's pattern exactly: a plain relative redirect
			// technically happened to work before, but only by coincidence
			// of this servlet's specific URL mapping — using the context
			// path explicitly makes the destination unambiguous regardless
			// of where the redirect is triggered from.
			response.sendRedirect(request.getContextPath() + "/admin/adminDashboard.jsp");
		} else {
			response.sendRedirect(request.getContextPath() + "/admin/adminLogin.jsp?error=true");
		}
	}
}