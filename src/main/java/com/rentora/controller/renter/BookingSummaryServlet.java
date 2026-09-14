package com.rentora.controller.renter;

import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.model.Booking;
import com.rentora.model.User;
import com.rentora.model.Vehicle;
import com.rentora.service.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Step 1 of checkout — a full review of the booking (vehicle specs, renter
 * info, dates, price breakdown) before moving on to the separate payment
 * page. Splitting these into two pages keeps this one free to show
 * everything in detail, and keeps the payment page focused purely on
 * entering payment details.
 *
 * The 10-minute payment countdown is shared across both pages — it's always
 * calculated live from booking.createdAt, so time spent here counts against
 * the same window as the payment page, not a separate timer.
 */
@WebServlet("/renter/booking-summary")
public class BookingSummaryServlet extends HttpServlet {

    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");

        try {
            long bookingId = Long.parseLong(req.getParameter("bookingId"));
            Optional<Booking> maybeBooking = bookingService.getById(bookingId);

            if (maybeBooking.isEmpty() || maybeBooking.get().getRenterId() != renter.getUserId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to view this booking.");
                return;
            }
            Booking booking = maybeBooking.get();
            if (!"AWAITING_PAYMENT".equals(booking.getStatus())) {
                // Already paid (or cancelled) — nothing to review, send them to their dashboard instead.
                resp.sendRedirect(req.getContextPath() + "/renter/dashboard");
                return;
            }
            req.setAttribute("booking", booking);
            req.setAttribute("renter", renter);

            Optional<Vehicle> vehicle = vehicleDAO.findById(booking.getVehicleId());
            vehicle.ifPresent(v -> req.setAttribute("vehicle", v));

            long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate()) + 1;
            req.setAttribute("numDays", days);

        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load this booking.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/booking-summary.jsp").forward(req, resp);
    }
}
