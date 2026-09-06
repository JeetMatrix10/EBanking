package controller;

import java.io.IOException;
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
import model.TransferResult;

@WebServlet("/transfer")
public class TransferServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();
		List<Account> accounts = accountDao.getAccountsByCid(loggedInCustomer.getCid());
		request.setAttribute("accounts", accounts);

		RequestDispatcher dispatcher = request.getRequestDispatcher("transfer.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String saccno = request.getParameter("saccno");
		String benaccno = request.getParameter("benaccno");
		String amountStr = request.getParameter("amount");
		BigDecimal amount = new BigDecimal(amountStr);

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();

		// Why only saccno is checked, not benaccno: you're only allowed to
		// move money OUT of your own account — sending it TO someone else's
		// account is the entire purpose of a transfer, so benaccno belonging
		// to a different customer is expected and correct, not a violation.
		if (!accountDao.isAccountOwnedByCustomer(saccno, loggedInCustomer.getCid())) {
			// out.println("<h3>Access denied. The sending account does not belong to
			// you.</h3>");
			// out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
			// return;
			request.setAttribute("success", false);
			request.getRequestDispatcher("transferResult.jsp").forward(request, response);
			return;
		}

		TransactionDao dao = new TransactionDao();
		TransferResult result = dao.transfer(saccno, benaccno, amount);

		request.setAttribute("success", result.isSuccess());
		request.setAttribute("warningMessage", result.getWarningMessage());

		RequestDispatcher dispatcher = request.getRequestDispatcher("transferResult.jsp");
		dispatcher.forward(request, response);
	}
}