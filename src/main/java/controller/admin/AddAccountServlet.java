package controller.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.AccountDao;
import model.Account;

@WebServlet("/addAccount")
public class AddAccountServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String cid = request.getParameter("cid");
		String opendateStr = request.getParameter("opendate");
		String balanceStr = request.getParameter("balance");
		String accounttype = request.getParameter("accounttype");

		// Why Date.valueOf() specifically: it expects the exact format
		// "yyyy-MM-dd", which conveniently matches what an HTML
		// <input type="date"> field sends automatically — no manual parsing needed.
		Date opendate = Date.valueOf(opendateStr);
		BigDecimal balance = new BigDecimal(balanceStr);

		Account account = new Account(cid, opendate, balance, accounttype);

		AccountDao dao = new AccountDao();
		String generatedAccno = dao.addAccount(account);

		request.setAttribute("success", generatedAccno != null);
		request.setAttribute("generatedAccno", generatedAccno);

		RequestDispatcher dispatcher = request.getRequestDispatcher("addAccountResult.jsp");
		dispatcher.forward(request, response);
	}
}