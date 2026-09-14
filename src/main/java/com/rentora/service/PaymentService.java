package com.rentora.service;

import com.rentora.dao.impl.BookingDAOImpl;
import com.rentora.dao.impl.PaymentDAOImpl;
import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.BookingDAO;
import com.rentora.dao.interfaces.PaymentDAO;
import com.rentora.dao.interfaces.UserDAO;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.Booking;
import com.rentora.model.Payment;
import com.rentora.model.User;
import com.rentora.observer.NotificationEvent;
import com.rentora.observer.NotificationSubject;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Handles payment for a booking. Once payment succeeds, the booking is
 * confirmed immediately — there is no owner approval step in this flow.
 */
public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();
    private final NotificationSubject notificationSubject;

    public PaymentService(NotificationSubject notificationSubject) {
        this.notificationSubject = notificationSubject;
    }

    /**
     * Processes payment for a booking that's awaiting payment and confirms it.
     *
     * NOTE: This simulates a successful payment for demonstration/coursework
     * purposes — no real payment gateway is integrated. In a production system
     * this method would call out to a provider (Stripe, PayHere, etc.) and
     * only mark the payment SUCCESS once the gateway confirms it.
     */
    public Payment processPayment(long bookingId, long renterId, String paymentMethod) throws Exception {
        Optional<Booking> maybeBooking = bookingDAO.findById(bookingId);
        if (maybeBooking.isEmpty() || maybeBooking.get().getRenterId() != renterId) {
            throw new ValidationException("You don't have permission to pay for this booking.");
        }
        Booking booking = maybeBooking.get();
        if (!"AWAITING_PAYMENT".equals(booking.getStatus())) {
            throw new ValidationException("This booking is not awaiting payment.");
        }
        // Defense in depth: the background job cancels expired holds every minute,
        // but also re-check here in case payment lands in that same window.
        if (booking.getCreatedAt() != null && booking.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(10))) {
            bookingDAO.updateStatus(bookingId, "CANCELLED");
            throw new ValidationException("This booking's 10-minute payment window has expired. Please book again.");
        }

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionRef("TXN-" + System.currentTimeMillis() + "-" + bookingId);
        payment.setPaidAt(LocalDateTime.now());
        long paymentId = paymentDAO.create(payment);
        payment.setPaymentId(paymentId);

        bookingDAO.updateStatus(bookingId, "CONFIRMED");
        vehicleDAO.updateStatus(booking.getVehicleId(), "BOOKED");

        String vehicleLabel = booking.getVehicleBrand() + " " + booking.getVehicleModel();
        notificationSubject.notifyAll(new NotificationEvent(
                booking.getRenterId(), "Booking Confirmed",
                "Your payment was successful — your booking for " + vehicleLabel + " is confirmed."));
        for (User admin : userDAO.findAllByRole("ADMIN")) {
            notificationSubject.notifyAll(new NotificationEvent(
                    admin.getUserId(), "Vehicle Booked",
                    vehicleLabel + " (" + booking.getVehicleNumber() + ") has been booked and paid for (" +
                            booking.getStartDate() + " to " + booking.getEndDate() + ")."));
        }

        return payment;
    }

    public Optional<Payment> getPaymentForBooking(long bookingId) throws Exception {
        return paymentDAO.findByBooking(bookingId);
    }
}
