package controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.CustomerDao;
import model.Customer;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Why doGet just re-reads from the database instead of reusing the
    // session's Customer object directly: if the admin updated this
    // customer's phone/email since they logged in, the session copy would
    // be stale — re-fetching guarantees the profile page always shows
    // current data, not what was true at login time.
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        CustomerDao dao = new CustomerDao();
        Customer freshCustomer = dao.getCustomerByCid(loggedInCustomer.getCid());

        request.setAttribute("customer", freshCustomer);

        RequestDispatcher dispatcher = request.getRequestDispatcher("profile.jsp");
        dispatcher.forward(request, response);
    }

    // Why doPost here handles the update, in the SAME servlet as doGet:
    // this is one cohesive feature (view profile, edit profile) rather than
    // two separate concerns — GET shows the form pre-filled, POST processes
    // the submission. Splitting these into different servlets would mean
    // duplicating the "get logged-in customer from session" logic twice.
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        String phone = request.getParameter("phone");
        String email = request.getParameter("email");

        // Why we ONLY ever use loggedInCustomer.getCid() here, never a cid
        // from request.getParameter(...): this is the same ownership
        // principle as before — even if someone tampered with a hidden form
        // field to submit a different cid, we ignore it completely and only
        // ever update the account actually tied to this session.
        CustomerDao dao = new CustomerDao();
        boolean success = dao.updateCustomer(loggedInCustomer.getCid(), phone, email);

        // Why we also update the SESSION's copy after a successful save: if
        // we didn't, the session would keep showing the OLD phone/email
        // elsewhere (e.g. if dashboard.jsp ever displays it) until the next
        // login, even though the database is already correct.
        if (success) {
            loggedInCustomer.setPhone(phone);
            loggedInCustomer.setEmail(email);
            session.setAttribute("loggedInCustomer", loggedInCustomer);
        }

        request.setAttribute("updateSuccess", success);
        doGet(request, response); // reload the page with fresh data
    }
}