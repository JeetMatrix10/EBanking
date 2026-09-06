package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Why urlPatterns lists specific pages instead of "/*": login.jsp,
// register.jsp, and index.jsp must stay reachable WITHOUT being logged in
// (that's the whole point of them) — "/*" would lock those out too and
// nobody could ever log in.
@WebFilter(urlPatterns = { "/dashboard.jsp", "/deposit.jsp", "/withdraw.jsp", "/transfer.jsp", "/transactions.jsp",
		"/profile.jsp" })
public class AuthFilter implements Filter {

	// Why we implement Filter instead of extending HttpServlet: a Filter
	// sits IN FRONT of the actual page, deciding whether to let the request
	// continue or block it — that's a fundamentally different job than a
	// Servlet, which handles the request itself.
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		// Why "false" here: getSession(false) returns null if no session
		// exists yet, instead of creating a new empty one. We want to know
		// definitively whether this browser has ALREADY logged in — creating
		// a blank session here would defeat that check.
		HttpSession session = req.getSession(false);

		boolean loggedIn = (session != null && session.getAttribute("loggedInCustomer") != null);

		if (loggedIn) {
			// Why chain.doFilter(...): this passes the request forward to
			// whatever it was actually trying to reach (e.g. dashboard.jsp).
			// Without this call, the request just stops here and nothing loads.
			chain.doFilter(request, response);
		} else {
			// Why redirect instead of forward: redirect sends a fresh browser
			// request to login.jsp, so the address bar updates correctly and
			// the user can't hit "back" into the protected page from history.
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}
}