package com.rentora.controller.renter;

import com.rentora.exception.BookingConflictException;
import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.service.BookingService;
import com.rentora.strategy.CardPaymentStrategy;
import com.rentora.strategy.PaymentStrategy;
import com.rentora.strategy.WalletPaymentStrategy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;

/** Handles booking creation for logged-in renters. */
@WebServlet("/renter/book")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");

        // Defense in depth: RoleFilter already blocks non-renters from reaching
        // this URL, but we check again here so booking creation is never
        // possible without an explicit RENTER role, even if routing changes later.
        if (renter == null || !"RENTER".equalsIgnoreCase(renter.getRoleName())) {
            String vehicleId = req.getParameter("vehicleId");
            // Note: redirecting (not forwarding) a new request, so we send the
            // user back to the vehicle page, which already renders its own
            // "Only renter accounts can book vehicles" message for this case.
            resp.sendRedirect(req.getContextPath() + "/vehicle-details?id=" + vehicleId);
            return;
        }

        BookingService bookingService =
                (BookingService) req.getServletContext().getAttribute("bookingService");

        try {
            long vehicleId = Long.parseLong(req.getParameter("vehicleId"));
            LocalDate start = LocalDate.parse(req.getParameter("startDate"));
            LocalDate end = LocalDate.parse(req.getParameter("endDate"));
            String paymentMethod = req.getParameter("paymentMethod"); // CARD or WALLET

            // Strategy Pattern: choose fare calculation algorithm based on payment method
            PaymentStrategy strategy = "WALLET".equalsIgnoreCase(paymentMethod)
                    ? new WalletPaymentStrategy()
                    : new CardPaymentStrategy();

            long bookingId = bookingService.createBooking(
                    renter.getUserId(), vehicleId, start, end, strategy);

            // No owner-approval step — send the renter straight to payment.
            // The booking is confirmed the moment payment succeeds.
            resp.sendRedirect(req.getContextPath() + "/renter/booking-summary?bookingId=" + bookingId);

        } catch (BookingConflictException | ValidationException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/renter/vehicle-details.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Booking failed. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/renter/vehicle-details.jsp").forward(req, resp);
        }
    }
}
