package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import dao.FixedDepositDao;
import model.Customer;

@WebServlet("/prematureWithdrawFD")
public class PrematureWithdrawFDServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int fdId = Integer.parseInt(request.getParameter("fdId"));
        String accno = request.getParameter("accno");

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        AccountDao accountDao = new AccountDao();
        if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
            out.println("<h3>Access denied. This account does not belong to you.</h3>");
            out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
            return;
        }

        FixedDepositDao dao = new FixedDepositDao();
        BigDecimal payoutAmount = dao.prematureWithdrawFD(fdId, accno);

        if (payoutAmount != null) {
            out.println("<h3>FD closed early. Amount credited (after penalty): " + payoutAmount + "</h3>");
        } else {
            out.println("<h3>Premature withdrawal failed. Check the FD ID and account number.</h3>");
        }
        out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
    }
}