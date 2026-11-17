package controller;

import java.io.IOException;
import utilities.ValidationUtil;
import java.math.BigDecimal;
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

@WebServlet("/deposit")
public class DepositServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Why doGet exists now, when it didn't before: this handles simply
	// VISITING the deposit page (no form submitted yet) — its only job is
	// to fetch this customer's accounts and hand them to the JSP so the
	// dropdown has something to show.
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();
		List<Account> accounts = accountDao.getAccountsByCid(loggedInCustomer.getCid());
		request.setAttribute("accounts", accounts);

		RequestDispatcher dispatcher = request.getRequestDispatcher("deposit.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String accno = request.getParameter("accno");
		BigDecimal amount = ValidationUtil.parsePositiveAmount(request.getParameter("amount"));

		// Why this runs before the ownership check and any DAO call: bad input
		// should fail cheaply, and getting here with a null amount used to mean
		// an uncaught NumberFormatException instead of a clean message.
		if (amount == null) {
			request.setAttribute("success", false);
			request.setAttribute("errorMessage",
					"Enter a valid amount greater than zero, with at most 2 decimal places.");
			request.getRequestDispatcher("depositResult.jsp").forward(request, response);
			return;
		}

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();
		if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
			request.setAttribute("success", false);
			request.getRequestDispatcher("depositResult.jsp").forward(request, response);
			return;
		}

		TransactionDao dao = new TransactionDao();
		request.setAttribute("success", dao.deposit(accno, amount));
		request.getRequestDispatcher("depositResult.jsp").forward(request, response);
	}
}