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
		if (benaccno != null) {
			benaccno = benaccno.trim().toUpperCase();   // match the stored ACC00001 format
		}
		BigDecimal amount = ValidationUtil.parsePositiveAmount(request.getParameter("amount"));

		String error = null;
		if (!ValidationUtil.isValidAccno(benaccno)) {
			error = "Enter a valid beneficiary account number (for example ACC00001).";
		} else if (benaccno.equals(saccno)) {
			// Why block this: a transfer to the same account debits and credits
			// one row and logs a meaningless TRANSFER entry.
			error = "You cannot transfer to the same account.";
		} else if (amount == null) {
			error = "Enter a valid amount greater than zero, with at most 2 decimal places.";
		}

		if (error != null) {
			request.setAttribute("success", false);
			request.setAttribute("errorMessage", error);
			request.getRequestDispatcher("transferResult.jsp").forward(request, response);
			return;
		}

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();
		if (!accountDao.isAccountOwnedByCustomer(saccno, loggedInCustomer.getCid())) {
			request.setAttribute("success", false);
			request.getRequestDispatcher("transferResult.jsp").forward(request, response);
			return;
		}

		TransferResult result = new TransactionDao().transfer(saccno, benaccno, amount);
		request.setAttribute("success", result.isSuccess());
		request.setAttribute("warningMessage", result.getWarningMessage());
		request.getRequestDispatcher("transferResult.jsp").forward(request, response);
	}
}