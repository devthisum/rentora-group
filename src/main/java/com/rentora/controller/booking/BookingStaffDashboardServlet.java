package com.rentora.controller.booking;

import com.rentora.model.User;
import com.rentora.service.BookingService;
import com.rentora.service.MaintenanceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * The Booking Staff dashboard — the front desk. This is where the shop's
 * "vehicle shows Booked even though the customer never came" problem
 * actually gets fixed day-to-day:
 *
 *   - "Today's Pickups": bookings that are paid (CONFIRMED) and due for
 *     pickup. Staff either confirms the customer took the vehicle (which is
 *     the ONLY moment the vehicle's shop-floor status becomes "Booked" —
 *     see VehicleDAOImpl) or marks a no-show, cancelling the booking and
 *     freeing those dates.
 *   - "Currently Out": vehicles actually picked up (ONGOING), so staff can
 *     confirm when they're physically returned — which hands the vehicle
 *     off into the maintenance/inspection workflow.
 *
 * POST actions (?action=):
 *   confirmPickup  -> CONFIRMED -> ONGOING (vehicle now shows Booked)
 *   markNoShow     -> CONFIRMED -> CANCELLED (dates freed up)
 *   confirmReturn  -> hands off to MaintenanceService.markReturned (same as
 *                     the maintenance dashboard's "Return Vehicle" action)
 */
@WebServlet("/booking/dashboard")
public class BookingStaffDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        try {
            req.setAttribute("todaysPickups", bookingService.getTodaysPickups());
            req.setAttribute("currentlyOut", bookingService.getOngoingOrderedByReturn());
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load the booking desk.");
        }
        // Booking staff act here; admin can view but not act — same pattern as the maintenance board.
        req.setAttribute("canEdit", user != null && "BOOKING".equalsIgnoreCase(user.getRoleName()));
        req.getRequestDispatcher("/WEB-INF/views/booking/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        if (!"BOOKING".equalsIgnoreCase(user.getRoleName())) {
            req.getSession().setAttribute("errorMessage", "Only booking staff can update pickups and returns — admins have view-only access here.");
            resp.sendRedirect(req.getContextPath() + "/booking/dashboard");
            return;
        }

        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");
        MaintenanceService maintenanceService = (MaintenanceService) req.getServletContext().getAttribute("maintenanceService");

        String action = req.getParameter("action");
        try {
            long bookingId = Long.parseLong(req.getParameter("bookingId"));
            switch (action == null ? "" : action) {
                case "confirmPickup" -> {
                    bookingService.confirmPickup(bookingId);
                    req.getSession().setAttribute("successMessage", "Pickup confirmed — vehicle now shows as booked.");
                }
                case "markNoShow" -> {
                    bookingService.markNoShow(bookingId);
                    req.getSession().setAttribute("successMessage", "Booking cancelled as a no-show — dates freed up.");
                }
                case "confirmReturn" -> {
                    maintenanceService.markReturned(bookingId);
                    java.math.BigDecimal lateFee = bookingService.getById(bookingId)
                            .map(com.rentora.model.Booking::getLateFee)
                            .orElse(java.math.BigDecimal.ZERO);
                    String msg = "Return confirmed — vehicle sent for inspection.";
                    if (lateFee.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        msg += " Late fee: Rs. " + lateFee + " — collect this from the customer.";
                    }
                    req.getSession().setAttribute("successMessage", msg);
                }
                default -> req.getSession().setAttribute("errorMessage", "Unknown action.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("errorMessage",
                    e.getMessage() != null ? e.getMessage() : "Could not complete that action.");
        }
        resp.sendRedirect(req.getContextPath() + "/booking/dashboard");
    }
}
