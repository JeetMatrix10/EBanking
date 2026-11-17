package controller;

import java.io.IOException;
import utilities.ValidationUtil;
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
	    boolean testMode = "on".equals(request.getParameter("testMode"));

	    Integer months = ValidationUtil.parsePositiveInteger(request.getParameter("months"), 120);
	    BigDecimal rate = ValidationUtil.parseRate(request.getParameter("interestRate"));
	    BigDecimal amount = ValidationUtil.parsePositiveAmount(request.getParameter("amount"));

	    String error = null;
	    if (months == null) {
	        error = "Enter the number of months as a whole number from 1 to 120.";
	    } else if (rate == null) {
	        error = "Enter an interest rate above 0 and up to 20 percent.";
	    } else if (amount == null) {
	        error = "Enter a valid monthly amount greater than zero, with at most 2 decimal places.";
	    }

	    if (error != null) {
	        request.setAttribute("success", false);
	        request.setAttribute("errorMessage", error);
	        request.getRequestDispatcher("rdResult.jsp").forward(request, response);
	        return;
	    }

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
	    rd.setMonthlyAmount(amount);
	    rd.setNoOfMonths(months);
	    rd.setInterestRate(rate);
	    rd.setTestMode(testMode);
	    rd.setBookDate(Timestamp.valueOf(java.time.LocalDateTime.now()));

	    boolean success = new RecurringDepositDao().bookRD(rd, testMode);

	    request.setAttribute("success", success);
	    request.setAttribute("testMode", testMode);
	    request.getRequestDispatcher("rdResult.jsp").forward(request, response);
	}
}