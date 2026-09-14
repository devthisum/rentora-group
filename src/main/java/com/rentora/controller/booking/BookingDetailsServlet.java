package com.rentora.controller.booking;

import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.UserDAO;
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

import java.io.IOException;
import java.util.Optional;

/**
 * Full detail view of a single booking — customer info (name, phone, email,
 * NIC), the vehicle rented, dates, and pricing. Opened from a "See Details"
 * button on the Booking Desk / admin dashboard, in a new tab.
 * Reachable by both ADMIN and BOOKING staff (RoleFilter's /booking/* rule
 * already covers both).
 */
@WebServlet("/booking/booking-details")
public class BookingDetailsServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAOImpl();
    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");
        try {
            long bookingId = Long.parseLong(req.getParameter("id"));
            Optional<Booking> maybeBooking = bookingService.getById(bookingId);
            if (maybeBooking.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return;
            }
            Booking booking = maybeBooking.get();
            req.setAttribute("booking", booking);

            Optional<User> renter = userDAO.findById(booking.getRenterId());
            renter.ifPresent(u -> req.setAttribute("renter", u));

            Optional<Vehicle> vehicle = vehicleDAO.findById(booking.getVehicleId());
            vehicle.ifPresent(v -> req.setAttribute("vehicle", v));

        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load this booking's details.");
        }
        req.getRequestDispatcher("/WEB-INF/views/booking/booking-details.jsp").forward(req, resp);
    }
}
