package com.rentora.controller.admin;

import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.interfaces.UserDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

/**
 * Admin creates, views, and deletes staff logins for both staff roles:
 *   MAINTENANCE — inspects returned vehicles, opens/resolves repairs
 *   BOOKING     — front desk: confirms pickups, no-shows, and returns
 * Staff can't self-register, only admin can add or remove them.
 */
@WebServlet("/admin/staff")
public class AdminStaffServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("maintenanceStaffList", userDAO.findAllByRole("MAINTENANCE"));
            req.setAttribute("bookingStaffList", userDAO.findAllByRole("BOOKING"));
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load staff accounts.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/staff.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String formAction = req.getParameter("formAction"); // "create" (default) or "delete"

        if ("delete".equals(formAction)) {
            handleDelete(req);
        } else {
            handleCreate(req);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/staff");
    }

    private void handleCreate(HttpServletRequest req) {
        try {
            String role = req.getParameter("role"); // "MAINTENANCE" or "BOOKING"
            authService.createStaffAccount(
                    req.getParameter("fullName"),
                    req.getParameter("email"),
                    req.getParameter("phone"),
                    req.getParameter("password"),
                    role);
            req.getSession().setAttribute("successMessage",
                    ("BOOKING".equalsIgnoreCase(role) ? "Booking staff" : "Maintenance staff") + " account created.");
        } catch (ValidationException ve) {
            req.getSession().setAttribute("errorMessage", ve.getMessage());
        } catch (Exception e) {
            req.getSession().setAttribute("errorMessage", "Could not create this staff account.");
        }
    }

    private void handleDelete(HttpServletRequest req) {
        try {
            long userId = Long.parseLong(req.getParameter("userId"));
            Optional<User> target = userDAO.findById(userId);

            // Defense-in-depth: only ever delete accounts that are actually staff,
            // even if someone tampered with the request — never renters/admins here.
            if (target.isEmpty()
                    || !("MAINTENANCE".equalsIgnoreCase(target.get().getRoleName())
                    || "BOOKING".equalsIgnoreCase(target.get().getRoleName()))) {
                req.getSession().setAttribute("errorMessage", "That account can't be deleted from here.");
                return;
            }

            userDAO.delete(userId);
            req.getSession().setAttribute("successMessage", target.get().getFullName() + "'s account was deleted.");
        } catch (Exception e) {
            // Most likely cause: this staff member is referenced elsewhere (e.g. they
            // posted an announcement, or added a vehicle) and the DB is protecting
            // that history from being orphaned.
            boolean isConstraintIssue = unwrapIsConstraintViolation(e);
            req.getSession().setAttribute("errorMessage", isConstraintIssue
                    ? "Can't delete this account — it's linked to other records (e.g. an announcement they posted, or a vehicle they added), and deleting it would orphan that history."
                    : "Could not delete this staff account.");
        }
    }

    private boolean unwrapIsConstraintViolation(Throwable e) {
        while (e != null) {
            if (e instanceof SQLIntegrityConstraintViolationException) return true;
            e = e.getCause();
        }
        return false;
    }
}
