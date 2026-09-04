package controller.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

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

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (success) {
            out.println("<h3>Account balance updated successfully.</h3>");
        } else {
            out.println("<h3>Update failed. Check the account number.</h3>");
        }
        out.println("<a href='adminDashboard.jsp'>Back to Admin Dashboard</a>");
    }
}