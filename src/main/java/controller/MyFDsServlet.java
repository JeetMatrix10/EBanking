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

import dao.FixedDepositDao;
import model.Customer;
import model.FixedDeposit;

@WebServlet("/myFDs")
public class MyFDsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Why no ownership check is needed here, unlike deposit/withdraw/FD
        // booking: this method only ever reads FDs belonging to
        // loggedInCustomer.getCid() — there's no accno or fdId coming from
        // the user that could be swapped to someone else's, so there's
        // nothing to validate against.
        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        FixedDepositDao dao = new FixedDepositDao();
        List<FixedDeposit> fds = dao.getFDsByCid(loggedInCustomer.getCid());

        request.setAttribute("fds", fds);

        RequestDispatcher dispatcher = request.getRequestDispatcher("myFDs.jsp");
        dispatcher.forward(request, response);
    }
}