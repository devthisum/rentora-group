package com.rentora.dao.interfaces;

import com.rentora.model.Payment;
import java.util.Optional;

public interface PaymentDAO {
    long create(Payment payment) throws Exception;
    Optional<Payment> findByBooking(long bookingId) throws Exception;
}
