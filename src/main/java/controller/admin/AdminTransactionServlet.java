package controller.admin;

import java.io.IOException;
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
		String balanceStr = request.getParameter("balance");
		BigDecimal newBalance = new BigDecimal(balanceStr);

		AccountDao dao = new AccountDao();
		boolean success = dao.updateAccountBalance(accno, newBalance);

		request.setAttribute("success", success);

		RequestDispatcher dispatcher = request.getRequestDispatcher("updateAccountResult.jsp");
		dispatcher.forward(request, response);
	}
}