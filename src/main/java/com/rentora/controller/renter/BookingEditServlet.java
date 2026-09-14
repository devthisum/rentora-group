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

/**
 * Lets a renter change their booking's dates while it's still on the payment
 * page (AWAITING_PAYMENT, within the 10-minute window) — recalculates the
 * total and re-checks the vehicle is free for the new dates.
 */
@WebServlet("/renter/booking/update-dates")
public class BookingEditServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");

        long bookingId = Long.parseLong(req.getParameter("bookingId"));
        String redirectTo = "booking-summary".equals(req.getParameter("redirectTo")) ? "booking-summary" : "payment";
        try {
            LocalDate start = LocalDate.parse(req.getParameter("startDate"));
            LocalDate end = LocalDate.parse(req.getParameter("endDate"));
            String paymentMethod = req.getParameter("paymentMethod");

            PaymentStrategy strategy = "WALLET".equalsIgnoreCase(paymentMethod)
                    ? new WalletPaymentStrategy()
                    : new CardPaymentStrategy();

            bookingService.updateBookingDates(bookingId, renter.getUserId(), start, end, strategy);
            resp.sendRedirect(req.getContextPath() + "/renter/" + redirectTo + "?bookingId=" + bookingId + "&updated=1");

        } catch (BookingConflictException | ValidationException ex) {
            resp.sendRedirect(req.getContextPath() + "/renter/" + redirectTo + "?bookingId=" + bookingId + "&error=" +
                    java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/renter/" + redirectTo + "?bookingId=" + bookingId + "&error=1");
        }
    }
}
