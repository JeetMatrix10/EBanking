package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.AccountDao;
import dao.TransactionDao;
import exception.InsufficientBalanceException;
import model.Customer;
import model.Account;

@WebServlet("/withdraw")
public class WithdrawServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        AccountDao accountDao = new AccountDao();
        List<Account> accounts = accountDao.getAccountsByCid(loggedInCustomer.getCid());

        request.setAttribute("accounts", accounts);

        RequestDispatcher dispatcher = request.getRequestDispatcher("withdraw.jsp");
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

        if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
            out.println("<h3>Access denied. This account does not belong to you.</h3>");
            out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
            return;
        }

        TransactionDao dao = new TransactionDao();

        try {
            boolean success = dao.withdraw(accno, amount);
            if (success) {
                out.println("<h3>Withdrawal successful!</h3>");
            } else {
                out.println("<h3>Withdrawal failed. Check the account number.</h3>");
            }
        } catch (InsufficientBalanceException e) {
            out.println("<h3>Withdrawal failed: " + e.getMessage() + "</h3>");
        }

        out.println("<a href='dashboard.jsp'>Back to Dashboard</a>");
    }
}