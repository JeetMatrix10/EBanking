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

import dao.FixedDepositDao;
import model.Customer;
import model.FixedDeposit;

@WebServlet("/prematureWithdrawFD")
public class PrematureWithdrawFDServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		FixedDepositDao dao = new FixedDepositDao();
		List<FixedDeposit> activeFDs = dao.getActiveFDsByCid(loggedInCustomer.getCid());
		request.setAttribute("activeFDs", activeFDs);

		RequestDispatcher dispatcher = request.getRequestDispatcher("prematureWithdrawFD.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int fdId = Integer.parseInt(request.getParameter("fdId"));

		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

		FixedDepositDao dao = new FixedDepositDao();

		// Why we look up the FD's OWN accno here instead of trusting a form
		// field: since the dropdown only ever lists FDs already filtered to
		// this customer's cid, and prematureWithdrawFD() re-verifies
		// ownership internally too, we no longer need the customer to
		// separately type/select an account number — the FD record itself
		// already knows which account it's tied to.
		List<FixedDeposit> activeFDs = dao.getActiveFDsByCid(loggedInCustomer.getCid());
		String accno = null;
		for (FixedDeposit fd : activeFDs) {
			if (fd.getFdId() == fdId) {
				accno = fd.getAccno();
				break;
			}
		}

		if (accno == null) {
			// out.println("<h3>Access denied or FD not found.</h3>");
			// out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
			// return;
			request.setAttribute("notFound", true);
			request.getRequestDispatcher("prematureWithdrawResult.jsp").forward(request, response);
			return;
		}

		BigDecimal payoutAmount = dao.prematureWithdrawFD(fdId, accno);
		request.setAttribute("payoutAmount", payoutAmount);

		RequestDispatcher dispatcher = request.getRequestDispatcher("prematureWithdrawResult.jsp");
		dispatcher.forward(request, response);

	}
}