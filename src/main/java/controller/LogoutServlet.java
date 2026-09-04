package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Why getSession(false) again here: if somehow there's no session,
        // there's nothing to invalidate — calling session.invalidate() on a
        // null session would throw a NullPointerException.
        HttpSession session = request.getSession(false);

        if (session != null) {
            // Why invalidate() instead of just removeAttribute(): invalidate()
            // destroys the ENTIRE session, not just the customer data —
            // ensuring absolutely nothing about this login persists,
            // which matters for security (e.g. on a shared/public computer).
            session.invalidate();
        }

        response.sendRedirect("login.jsp");
    }
}