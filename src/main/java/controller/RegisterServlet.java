package controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.CustomerDao;
import model.Customer;

// Why @WebServlet("/register") instead of editing web.xml manually:
// this annotation registers the URL mapping directly in the class itself —
// when register.jsp's form submits to "register", Tomcat knows to route
// that request here. Modern Servlet projects use annotations over XML
// mapping because it's less error-prone (one less file to keep in sync).
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Why doPost, not doGet: registration submits sensitive data (password),
	// and GET requests expose form data in the URL (visible in browser history,
	// server logs). POST keeps it in the request body instead — standard
	// practice for any form that writes data or handles credentials.
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Why request.getParameter: this reads the value typed into each
		// <input name="..."> field in register.jsp. The string inside
		// getParameter() MUST exactly match the "name" attribute in the JSP form.
		String name = request.getParameter("name");
		String phone = request.getParameter("phone");
		String email = request.getParameter("email");
		String panno = request.getParameter("panno");
		String aadhaarno = request.getParameter("aadhaarno");
		String password = request.getParameter("password");

		// Why we build a Customer object here rather than passing raw strings
		// to the DAO: it keeps registerCustomer()'s method signature clean
		// (one object, not seven separate string parameters), and matches
		// what the DAO already expects.
		Customer customer = new Customer(name, phone, email, panno, aadhaarno, password);

		CustomerDao dao = new CustomerDao();
		String generatedCid = dao.registerCustomer(customer);

		// Why we set content type and write HTML directly here (instead of
		// forwarding to another JSP) for now: it's the fastest way to confirm
		// the whole flow works end-to-end before we build a polished
		// success/failure JSP page. We'll replace this with proper page
		// forwarding once the core flow is proven.
//		response.setContentType("text/html");
//		PrintWriter out = response.getWriter();
//
//		if (generatedCid != null) {
//			out.println("<h3>Registration successful! Your Customer ID is: " + generatedCid + "</h3>");
//			out.println("<p>Please save this ID — you'll need it to log in.</p>");
//			out.println("<a href='login.jsp'>Go to Login</a>");
//		} else {
//			out.println("<h3>Registration failed. Please check your inputs and try again.</h3>");
//			out.println("<a href='register.jsp'>Try Again</a>");
//		}
		// Why this replaces the old out.println() block entirely: this
		// servlet now follows the SAME pattern as every other servlet in
		// the project (ProfileServlet, TransactionServlet, the dropdown
		// servlets) — set result data as request attributes, then forward
		// to a JSP that renders it as proper, consistent HTML, rather than
		// hand-writing HTML strings directly in Java code.
		request.setAttribute("success", generatedCid != null);
		request.setAttribute("generatedCid", generatedCid);

		RequestDispatcher dispatcher = request.getRequestDispatcher("registerResult.jsp");
		dispatcher.forward(request, response);
	}
}