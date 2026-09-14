package com.rentora.controller.renter;

import com.rentora.model.Booking;
import com.rentora.model.User;
import com.rentora.service.BookingService;
import com.rentora.service.PaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/renter/payment/success")
public class PaymentSuccessServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");
        PaymentService paymentService = (PaymentService) req.getServletContext().getAttribute("paymentService");

        try {
            long bookingId = Long.parseLong(req.getParameter("bookingId"));
            Optional<Booking> maybeBooking = bookingService.getById(bookingId);
            if (maybeBooking.isEmpty() || maybeBooking.get().getRenterId() != renter.getUserId()) {
                resp.sendRedirect(req.getContextPath() + "/renter/dashboard");
                return;
            }
            req.setAttribute("booking", maybeBooking.get());
            paymentService.getPaymentForBooking(bookingId).ifPresent(p -> req.setAttribute("payment", p));

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/renter/dashboard");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/payment-success.jsp").forward(req, resp);
    }
}
