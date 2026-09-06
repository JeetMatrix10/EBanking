package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import dao.RecurringDepositDao;
import model.Customer;
import model.Account;
import model.RecurringDeposit;

@WebServlet("/bookRD")
public class BookRDServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();
		List<Account> accounts = accountDao.getAccountsByCid(loggedInCustomer.getCid());
		request.setAttribute("accounts", accounts);

		RequestDispatcher dispatcher = request.getRequestDispatcher("recurring.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String accno = request.getParameter("accno");
		String monthsStr = request.getParameter("months");
		String amountStr = request.getParameter("amount");
		String interestRateStr = request.getParameter("interestRate");
		boolean testMode = "on".equals(request.getParameter("testMode"));

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();
		if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
			request.setAttribute("success", false);
			request.getRequestDispatcher("rdResult.jsp").forward(request, response);
			return;
		}

		RecurringDeposit rd = new RecurringDeposit();
		rd.setCid(loggedInCustomer.getCid());
		rd.setAccno(accno);
		rd.setMonthlyAmount(new BigDecimal(amountStr));
		rd.setNoOfMonths(Integer.parseInt(monthsStr));
		rd.setBookDate(Timestamp.valueOf(java.time.LocalDateTime.now()));
		rd.setInterestRate(new BigDecimal(interestRateStr));
		rd.setTestMode(testMode);

		RecurringDepositDao dao = new RecurringDepositDao();
		boolean success = dao.bookRD(rd, testMode);

		request.setAttribute("success", success);
		request.setAttribute("testMode", testMode);

		RequestDispatcher dispatcher = request.getRequestDispatcher("rdResult.jsp");
		dispatcher.forward(request, response);
	}
}