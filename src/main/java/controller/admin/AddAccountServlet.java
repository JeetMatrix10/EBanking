package controller.admin;

import java.io.IOException;
import utilities.ValidationUtil;
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
		if (cid != null) {
			cid = cid.trim().toUpperCase();
		}
		Date opendate = ValidationUtil.parseOpenDate(request.getParameter("opendate"));
		BigDecimal balance = ValidationUtil.parseNonNegativeAmount(request.getParameter("balance"));
		String accounttype = request.getParameter("accounttype");

		String error = null;
		if (!ValidationUtil.isValidCid(cid)) {
			error = "Enter a valid Customer ID (for example CUST0001).";
		} else if (opendate == null) {
			error = "Enter a valid open date that is not in the future.";
		} else if (balance == null) {
			error = "Enter a valid opening balance of zero or more, with at most 2 decimal places.";
		} else if (!ValidationUtil.isValidAccountType(accounttype)) {
			error = "Choose Saving or Current as the account type.";
		}

		if (error != null) {
			request.setAttribute("success", false);
			request.setAttribute("errorMessage", error);
			request.getRequestDispatcher("addAccountResult.jsp").forward(request, response);
			return;
		}

		String generatedAccno = new AccountDao().addAccount(new Account(cid, opendate, balance, accounttype));

		request.setAttribute("success", generatedAccno != null);
		request.setAttribute("generatedAccno", generatedAccno);
		request.getRequestDispatcher("addAccountResult.jsp").forward(request, response);
	}
}