package com.rentora.controller.renter;

import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.interfaces.UserDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.Booking;
import com.rentora.model.User;
import com.rentora.service.AuthService;
import com.rentora.service.BookingService;
import com.rentora.service.PaymentService;
import com.rentora.service.SavedPaymentMethodService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

/**
 * Payment page for a booking that's awaiting payment, and the endpoint that
 * processes it. Successful payment confirms the booking immediately — there
 * is no owner approval step anywhere in this flow.
 *
 * Before payment details, the renter must also have their address and
 * driving license on file — collected right here on first checkout, then
 * pre-filled (still editable) on every booking after that.
 */
@WebServlet("/renter/payment")
public class PaymentServlet extends HttpServlet {

    private final SavedPaymentMethodService savedPaymentMethodService = new SavedPaymentMethodService();
    private final UserDAO userDAO = new UserDAOImpl();
    private final AuthService authService = new AuthService();

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
                // Already paid (or cancelled) — nothing to pay for, send them to their dashboard instead.
                resp.sendRedirect(req.getContextPath() + "/renter/dashboard");
                return;
            }
            req.setAttribute("booking", booking);
            req.setAttribute("savedMethods", savedPaymentMethodService.getByUser(renter.getUserId()));

            // Fetch a fresh copy (not the possibly-stale session object) so previously-saved
            // checkout info (address/license) is correctly pre-filled if they've paid before.
            User freshRenter = userDAO.findById(renter.getUserId()).orElse(renter);
            req.setAttribute("renterProfile", freshRenter);

        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load this booking.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/payment.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        PaymentService paymentService = (PaymentService) req.getServletContext().getAttribute("paymentService");

        long bookingId = Long.parseLong(req.getParameter("bookingId"));
        try {
            // Checkout info (address + driving license) is required before payment can proceed —
            // save/update it first so it's on file even if the payment step itself then fails.
            User updated = authService.saveCheckoutInfo(
                    renter.getUserId(),
                    req.getParameter("addressStreet"),
                    req.getParameter("addressCity"),
                    req.getParameter("addressPostalCode"),
                    req.getParameter("drivingLicenseNumber"));
            session.setAttribute("user", updated); // keep the session copy in sync

            String paymentMethod = req.getParameter("paymentMethod"); // CARD or WALLET
            paymentService.processPayment(bookingId, renter.getUserId(), paymentMethod);

            resp.sendRedirect(req.getContextPath() + "/renter/payment/success?bookingId=" + bookingId);

        } catch (ValidationException ve) {
            resp.sendRedirect(req.getContextPath() + "/renter/payment?bookingId=" + bookingId + "&error=" +
                    java.net.URLEncoder.encode(ve.getMessage(), "UTF-8"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/renter/payment?bookingId=" + bookingId + "&error=1");
        }
    }
}
