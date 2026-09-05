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

// Why "/admin/*" plus the three servlet paths, unlike AuthFilter's flat list
// of individual pages: admin pages all live under one folder, so a wildcard
// is simpler and automatically covers any new admin JSP we add later
// without needing to remember to list it here too. The three servlet paths
// are added separately because they're mapped at the root ("/manageCustomer",
// not "/admin/manageCustomer"), so the wildcard alone wouldn't catch them.
@WebFilter(urlPatterns = {
    "/admin/*", "/manageCustomer", "/updateAccount", "/addAccount"
})
public class AdminAuthFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        // Why this specific exception exists: without it, the wildcard
        // above would block access to the admin login page itself — nobody
        // could ever log in, since the very act of reaching adminLogin.jsp
        // would be treated as "not logged in yet, redirect to login,"
        // creating an infinite redirect loop back to itself.
        if (uri.endsWith("adminLogin.jsp") || uri.endsWith("/adminLogin")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("loggedInAdmin") != null);

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(req.getContextPath() + "/admin/adminLogin.jsp");
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}