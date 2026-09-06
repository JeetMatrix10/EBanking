package controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import model.Account;
import model.Customer;

@WebServlet("/checkBalance")
public class AccountServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String accno = request.getParameter("accno");

		// Why we read the logged-in customer from the session instead of
		// trusting anything from the request: AuthFilter already guarantees
		// someone is logged in by the time this servlet runs, and the session
		// is server-side data the customer can't tamper with — unlike a form
		// field or URL parameter, which anyone can type anything into.
		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao dao = new AccountDao();

		// Why we check ownership BEFORE fetching/displaying anything: this is
		// the actual security boundary — no account data for accno should
		// ever reach the response unless it belongs to whoever is logged in.

		// Why this is now wrapped in "if (accno != null)", matching the fix
		// already applied to TransactionServlet: without this guard, a
		// request reaching this servlet with NO accno parameter (direct
		// navigation, stale bookmark, etc.) would incorrectly report
		// "access denied" instead of simply having nothing to check yet —
		// isAccountOwnedByCustomer(null, cid) always returns false, which
		// is technically correct but MISLEADING as a user-facing message
		// when nothing was actually requested.
		if (accno != null) {
			if (dao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
				Account account = dao.getAccountByAccno(accno);
				request.setAttribute("account", account);
			} else {
				request.setAttribute("accessDenied", true);
			}
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("dashboard.jsp");
		dispatcher.forward(request, response);
	}
}