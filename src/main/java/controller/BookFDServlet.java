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
		String yearsStr = request.getParameter("years");
		String interestRateStr = request.getParameter("interestRate");
		String amountStr = request.getParameter("amount");

		// Why "testMode" is read this way: an unchecked HTML checkbox sends
		// NO parameter at all (not "false" — nothing), so getParameter()
		// returns null. We treat "the checkbox value equals 'on'" as true,
		// and null/anything else as false.
		boolean testMode = "on".equals(request.getParameter("testMode"));

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		AccountDao accountDao = new AccountDao();

		// Why the SAME ownership check from deposit/withdraw is repeated
		// here: FD booking deducts money from an account, same risk profile
		// as deposit/withdraw — anyone touching a real account's balance
		// needs this guard, no exceptions.
		if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
			request.setAttribute("success", false);
			request.getRequestDispatcher("fdResult.jsp").forward(request, response);
			return;
		}

		FixedDeposit fd = new FixedDeposit();
		fd.setCid(loggedInCustomer.getCid());
		fd.setAccno(accno);
		fd.setAmount(new BigDecimal(amountStr));
		fd.setNoOfYears(Integer.parseInt(yearsStr));
		fd.setInterestRate(new BigDecimal(interestRateStr));

		// Why Date.valueOf(LocalDate.now()) here for bookDate, instead of a
		// form field asking the customer to type today's date: it should
		// never be user-editable — the FD is booked at whatever moment this
		// servlet runs, not at a date someone could otherwise fake to be
		// earlier or later.
		// fd.setBookDate(Date.valueOf(java.time.LocalDate.now()));
		fd.setBookDate(Timestamp.valueOf(java.time.LocalDateTime.now()));

		FixedDepositDao dao = new FixedDepositDao();
		boolean success = dao.bookFD(fd, testMode);

		request.setAttribute("success", success);
		if (success && testMode) {
			request.setAttribute("testMinutesWait", Integer.parseInt(yearsStr) * 5);
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("fdResult.jsp");
		dispatcher.forward(request, response);
	}
}