package controller;

import java.io.IOException;
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
import model.Transaction;

@WebServlet("/viewTransactions")
public class TransactionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accno = request.getParameter("accno");

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        AccountDao accountDao = new AccountDao();

        if (!accountDao.isAccountOwnedByCustomer(accno, loggedInCustomer.getCid())) {
            request.setAttribute("accessDenied", true);
        } else {
            TransactionDao dao = new TransactionDao();
            List<Transaction> transactions = dao.getTransactionsByAccno(accno);
            request.setAttribute("transactions", transactions);
        }

        request.setAttribute("accno", accno);

        RequestDispatcher dispatcher = request.getRequestDispatcher("transactions.jsp");
        dispatcher.forward(request, response);
    }
}