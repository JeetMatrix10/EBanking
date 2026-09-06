package controller.admin;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.CustomerDao;
import model.Customer; // Required to fetch existing customer

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
		String message;

		if ("update".equals(action)) {
			String phone = request.getParameter("phone");
			String email = request.getParameter("email");

			// Fetch the existing customer so we don't lose their current password.
			// Our updated DAO requires 4 parameters now, including the password.
			Customer existingCustomer = dao.getCustomerByCid(cid);

			if (existingCustomer != null) {
				// Pass the existing password right back into the update method
				boolean success = dao.updateCustomer(cid, phone, email, existingCustomer.getPassword());
				message = success ? "Customer updated successfully." : "Update failed. Please try again.";
			} else {
				message = "Update failed. Customer ID not found.";
			}

		} else if ("delete".equals(action)) {
			// Why we check hasActiveAccounts BEFORE calling deleteCustomer:
			// gives a specific, actionable error message instead of letting
			// the delete attempt fail with a raw database constraint error.
			if (dao.hasActiveAccounts(cid)) {
				message = "Cannot delete: this customer still has active accounts. Close their accounts first.";
			} else {
				boolean success = dao.deleteCustomer(cid);
				message = success ? "Customer deleted successfully." : "Delete failed. Check the Customer ID.";
			}

		} else {
			// Why this branch exists, when it didn't before: the original
			// code had NO handling for an unrecognized/missing "action"
			// value — it would silently fall through and just print the
			// "Back to Admin Dashboard" link with no message at all. This
			// makes that case explicit instead of leaving a confusing blank result.
			message = "Unknown action requested.";
		}

		request.setAttribute("message", message);

		RequestDispatcher dispatcher = request.getRequestDispatcher("manageCustomerResult.jsp");
		dispatcher.forward(request, response);
	}
}