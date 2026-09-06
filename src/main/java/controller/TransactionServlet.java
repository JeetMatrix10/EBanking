package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import dao.TransactionDao;
import model.Customer;
import model.Account;
import model.Transaction;

@WebServlet("/viewTransactions")
public class TransactionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String accno = request.getParameter("accno");

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();

		// Why we ALWAYS fetch the dropdown list, regardless of whether accno
		// was submitted: the dropdown needs to be populated every time this
		// page loads, whether it's the very first visit (no accno yet) or a
		// follow-up after selecting one.
		List<Account> accounts = accountDao.getAccountsByCid(loggedInCustomer.getCid());
		request.setAttribute("accounts", accounts);

		// Why this whole block is now wrapped in "if (accno != null)": on
		// the FIRST visit to this page, no account has been selected yet —
		// there's nothing to check ownership of and nothing to deny access
		// to. Only run the ownership check and fetch transactions once the
		// customer has actually picked something from the dropdown and submitted.

		if (accno != null) {
			if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
				request.setAttribute("accessDenied", true);
			} else {
				TransactionDao dao = new TransactionDao();
				List<Transaction> transactions = dao.getTransactionsByAccno(accno);
				request.setAttribute("transactions", transactions);
			}
			request.setAttribute("accno", accno);
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("transactions.jsp");
		dispatcher.forward(request, response);
	}
}