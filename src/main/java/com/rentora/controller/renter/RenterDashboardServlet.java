package com.rentora.controller.renter;

import com.rentora.model.Booking;
import com.rentora.model.Review;
import com.rentora.model.User;
import com.rentora.service.BookingService;
import com.rentora.service.ReviewService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/renter/dashboard")
public class RenterDashboardServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        BookingService bookingService =
                (BookingService) req.getServletContext().getAttribute("bookingService");
        try {
            List<Booking> bookings = bookingService.getByRenter(user.getUserId());
            req.setAttribute("bookings", bookings);

            // Existing review (if any) per COMPLETED booking, keyed by bookingId — lets the
            // JSP show "Leave a Review" vs an "Edit Review" modal pre-filled with their answer.
            Map<Long, Review> reviewsByBooking = new HashMap<>();
            for (Booking b : bookings) {
                if ("COMPLETED".equals(b.getStatus())) {
                    reviewService.getByBooking(b.getBookingId()).ifPresent(r -> reviewsByBooking.put(b.getBookingId(), r));
                }
            }
            req.setAttribute("reviewsByBooking", reviewsByBooking);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load your bookings.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/dashboard.jsp").forward(req, resp);
    }
}
