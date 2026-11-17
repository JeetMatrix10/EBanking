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
import dao.FixedDepositDao;
import model.Account;
import model.Customer;
import model.FixedDeposit;

@WebServlet("/bookFD")
public class BookFDServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();
		List<Account> accounts = accountDao.getAccountsByCid(loggedInCustomer.getCid());
		request.setAttribute("accounts", accounts);

		RequestDispatcher dispatcher = request.getRequestDispatcher("fd.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    String accno = request.getParameter("accno");
	    boolean testMode = "on".equals(request.getParameter("testMode"));

	    Integer years = ValidationUtil.parsePositiveInteger(request.getParameter("years"), 30);
	    BigDecimal rate = ValidationUtil.parseRate(request.getParameter("interestRate"));
	    BigDecimal amount = ValidationUtil.parsePositiveAmount(request.getParameter("amount"));

	    String error = null;
	    if (years == null) {
	        error = "Enter the number of years as a whole number from 1 to 30.";
	    } else if (rate == null) {
	        error = "Enter an interest rate above 0 and up to 20 percent.";
	    } else if (amount == null) {
	        error = "Enter a valid amount greater than zero, with at most 2 decimal places.";
	    }

	    if (error != null) {
	        request.setAttribute("success", false);
	        request.setAttribute("errorMessage", error);
	        request.getRequestDispatcher("fdResult.jsp").forward(request, response);
	        return;
	    }

	    HttpSession session = request.getSession();
	    Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

	    AccountDao accountDao = new AccountDao();
	    if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
	        request.setAttribute("success", false);
	        request.getRequestDispatcher("fdResult.jsp").forward(request, response);
	        return;
	    }

	    FixedDeposit fd = new FixedDeposit();
	    fd.setCid(loggedInCustomer.getCid());
	    fd.setAccno(accno);
	    fd.setAmount(amount);
	    fd.setNoOfYears(years);
	    fd.setInterestRate(rate);
	    fd.setBookDate(Timestamp.valueOf(java.time.LocalDateTime.now()));

	    boolean success = new FixedDepositDao().bookFD(fd, testMode);

	    request.setAttribute("success", success);
	    if (success && testMode) {
	        request.setAttribute("testMinutesWait", years * 5);
	    }
	    request.getRequestDispatcher("fdResult.jsp").forward(request, response);
	}
}