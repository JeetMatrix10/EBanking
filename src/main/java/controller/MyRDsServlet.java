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

import dao.RecurringDepositDao;
import model.Customer;
import model.RecurringDeposit;

@WebServlet("/myRDs")
public class MyRDsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        RecurringDepositDao dao = new RecurringDepositDao();
        List<RecurringDeposit> rds = dao.getRDsByCid(loggedInCustomer.getCid());
        request.setAttribute("rds", rds);

        request.getRequestDispatcher("myRDs.jsp").forward(request, response);
    }
}