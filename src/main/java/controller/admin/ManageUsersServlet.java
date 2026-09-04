package controller.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.CustomerDao;

@WebServlet("/manageCustomer")
public class ManageUsersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Why "action" comes from a hidden form field, not the URL: this lets
        // both CustomerUpd.jsp and CustomerDel.jsp submit to the SAME servlet
        // path while still telling it which operation to perform.
        String action = request.getParameter("action");
        String cid = request.getParameter("cid");

        CustomerDao dao = new CustomerDao();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if ("update".equals(action)) {
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");

            boolean success = dao.updateCustomer(cid, phone, email);
            if (success) {
                out.println("<h3>Customer updated successfully.</h3>");
            } else {
                out.println("<h3>Update failed. Check the Customer ID.</h3>");
            }

        } else if ("delete".equals(action)) {
            // Why we check hasActiveAccounts BEFORE calling deleteCustomer:
            // gives a specific, actionable error message instead of letting
            // the delete attempt fail with a raw database constraint error.
            if (dao.hasActiveAccounts(cid)) {
                out.println("<h3>Cannot delete: this customer still has active accounts. "
                           + "Close their accounts first.</h3>");
            } else {
                boolean success = dao.deleteCustomer(cid);
                if (success) {
                    out.println("<h3>Customer deleted successfully.</h3>");
                } else {
                    out.println("<h3>Delete failed. Check the Customer ID.</h3>");
                }
            }
        }

        out.println("<a href='adminDashboard.jsp'>Back to Admin Dashboard</a>");
    }
}