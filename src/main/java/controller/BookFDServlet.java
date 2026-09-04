package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import dao.FixedDepositDao;
import model.Customer;
import model.FixedDeposit;

@WebServlet("/bookFD")
public class BookFDServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accno = request.getParameter("accno");
        String yearsStr = request.getParameter("years");
        String interestRateStr = request.getParameter("interestRate");
        String amountStr = request.getParameter("amount");

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Why the SAME ownership check from deposit/withdraw is repeated
        // here: FD booking deducts money from an account, same risk profile
        // as deposit/withdraw — anyone touching a real account's balance
        // needs this guard, no exceptions.
        AccountDao accountDao = new AccountDao();
        if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
            out.println("<h3>Access denied. This account does not belong to you.</h3>");
            out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
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
        fd.setBookDate(Date.valueOf(java.time.LocalDate.now()));

        FixedDepositDao dao = new FixedDepositDao();
        boolean success = dao.bookFD(fd);

        if (success) {
            out.println("<h3>Fixed Deposit booked successfully!</h3>");
        } else {
            out.println("<h3>FD booking failed. Check the account number and available balance.</h3>");
        }
        out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
    }
}