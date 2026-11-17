package controller.admin;

import java.io.IOException;
import utilities.ValidationUtil;
import java.math.BigDecimal;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.AccountDao;

@WebServlet("/updateAccount")
public class AdminTransactionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    String accno = request.getParameter("accno");
	    if (accno != null) {
	        accno = accno.trim().toUpperCase();
	    }
	    // Zero is allowed here (an admin may legitimately zero an account); negatives are not.
	    BigDecimal newBalance = ValidationUtil.parseNonNegativeAmount(request.getParameter("balance"));

	    String error = null;
	    if (!ValidationUtil.isValidAccno(accno)) {
	        error = "Enter a valid account number (for example ACC00001).";
	    } else if (newBalance == null) {
	        error = "Enter a valid balance of zero or more, with at most 2 decimal places.";
	    }

	    if (error != null) {
	        request.setAttribute("success", false);
	        request.setAttribute("errorMessage", error);
	    } else {
	        request.setAttribute("success", new AccountDao().updateAccountBalance(accno, newBalance));
	    }
	    request.getRequestDispatcher("updateAccountResult.jsp").forward(request, response);
	}
}