package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import dao.TransactionDao;
import model.Customer;
import model.Account;

@WebServlet("/deposit")
public class DepositServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Why doGet exists now, when it didn't before: this handles simply
    // VISITING the deposit page (no form submitted yet) — its only job is
    // to fetch this customer's accounts and hand them to the JSP so the
    // dropdown has something to show.
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        AccountDao accountDao = new AccountDao();
        List<Account> accounts = accountDao.getAccountsByCid(loggedInCustomer.getCid());

        request.setAttribute("accounts", accounts);

        RequestDispatcher dispatcher = request.getRequestDispatcher("deposit.jsp");
        dispatcher.forward(request, response);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accno = request.getParameter("accno");
        String amountStr = request.getParameter("amount");
        BigDecimal amount = new BigDecimal(amountStr);

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        AccountDao accountDao = new AccountDao();

        // Why this check happens BEFORE calling TransactionDao at all: no
        // reason to even attempt the deposit if ownership fails — fail fast,
        // before touching the transaction/balance logic.
        if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
            out.println("<h3>Access denied. This account does not belong to you.</h3>");
            out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
            return;
        }

        TransactionDao dao = new TransactionDao();
        boolean success = dao.deposit(accno, amount);

        if (success) {
            out.println("<h3>Deposit successful!</h3>");
        } else {
            out.println("<h3>Deposit failed. Check the account number.</h3>");
        }
        out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
    }
}