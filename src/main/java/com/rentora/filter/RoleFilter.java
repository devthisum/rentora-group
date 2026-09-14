package com.rentora.filter;

import com.rentora.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Role-Based Access Control: ensures a RENTER can't hit /admin/* URLs, etc. */
@WebFilter(urlPatterns = {"/renter/*", "/admin/*", "/maintenance/*", "/booking/*"})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            String path = request.getRequestURI();
            String role = user.getRoleName();

            // /maintenance/* is shared by ADMIN (full access) and MAINTENANCE staff (their only area)
            if (path.contains("/maintenance/")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"MAINTENANCE".equalsIgnoreCase(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for your role.");
                }
                chain.doFilter(req, res);
                return;
            }

            // /booking/* is shared by ADMIN (view access) and BOOKING staff (their only area) —
            // the front desk that confirms pickups, no-shows, and returns.
            if (path.contains("/booking/")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"BOOKING".equalsIgnoreCase(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for your role.");
                }
                chain.doFilter(req, res);
                return;
            }

            String requiredRole = path.contains("/admin/") ? "ADMIN"
                    : path.contains("/renter/") ? "RENTER" : null;

            if (requiredRole != null && !requiredRole.equalsIgnoreCase(role)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for your role.");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
