package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.service.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Lets a renter cancel their own still-unpaid booking from My Bookings. */
@WebServlet("/renter/booking/cancel")
public class CancelBookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");

        try {
            long bookingId = Long.parseLong(req.getParameter("bookingId"));
            bookingService.cancelBooking(bookingId, renter.getUserId());
            session.setAttribute("successMessage", "Booking cancelled.");
        } catch (Exception e) {
            session.setAttribute("errorMessage",
                    e.getMessage() != null ? e.getMessage() : "Could not cancel this booking.");
        }
        resp.sendRedirect(req.getContextPath() + "/renter/dashboard");
    }
}
