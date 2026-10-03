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
            // Match on the path AFTER the context root, by prefix. A plain contains("/booking/")
            // wrongly caught the renter URLs /renter/booking/cancel and /renter/booking/update-dates,
            // treating a renter as if they were hitting the booking-staff area.
            String path = request.getRequestURI().substring(request.getContextPath().length());
            String role = user.getRoleName();

            // /maintenance/* is shared by ADMIN (full access) and MAINTENANCE staff (their only area)
            if (path.startsWith("/maintenance/")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"MAINTENANCE".equalsIgnoreCase(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for your role.");
                    return;
                }
                chain.doFilter(req, res);
                return;
            }

            // /booking/* is shared by ADMIN (view access) and BOOKING staff (their only area) —
            // the front desk that confirms pickups, no-shows, and returns.
            if (path.startsWith("/booking/")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"BOOKING".equalsIgnoreCase(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for your role.");
                    return;
                }
                chain.doFilter(req, res);
                return;
            }

            String requiredRole = path.startsWith("/admin/") ? "ADMIN"
                    : path.startsWith("/renter/") ? "RENTER" : null;

            if (requiredRole != null && !requiredRole.equalsIgnoreCase(role)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for your role.");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
