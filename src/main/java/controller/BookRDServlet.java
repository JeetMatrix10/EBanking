package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import dao.RecurringDepositDao;
import model.Customer;
import model.RecurringDeposit;

@WebServlet("/bookRD")
public class BookRDServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accno = request.getParameter("accno");
        String monthsStr = request.getParameter("months");
        String amountStr = request.getParameter("amount");
        boolean testMode = "on".equals(request.getParameter("testMode"));

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

        RecurringDeposit rd = new RecurringDeposit();
        rd.setCid(loggedInCustomer.getCid());
        rd.setAccno(accno);
        rd.setMonthlyAmount(new BigDecimal(amountStr));
        rd.setNoOfMonths(Integer.parseInt(monthsStr));
        rd.setBookDate(Timestamp.valueOf(java.time.LocalDateTime.now()));

        RecurringDepositDao dao = new RecurringDepositDao();
        boolean success = dao.bookRD(rd, testMode);

        if (success) {
            out.println("<h3>Recurring Deposit booked successfully!</h3>");
            if (testMode) {
                out.println("<p><em>Test mode: installments debit every 5 minutes instead of monthly.</em></p>");
            }
        } else {
            out.println("<h3>RD booking failed.</h3>");
        }
        out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
    }
}