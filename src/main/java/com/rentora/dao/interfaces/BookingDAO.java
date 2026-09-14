package com.rentora.dao.interfaces;

import com.rentora.model.Booking;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BookingDAO {
    long create(Booking booking) throws Exception;
    Optional<Booking> findById(long bookingId) throws Exception;
    List<Booking> findByRenter(long renterId) throws Exception;
    List<Booking> findAll() throws Exception;
    /** Bookings currently out (CONFIRMED/ONGOING) ordered by soonest due back — feeds the maintenance dashboard. */
    List<Booking> findActiveOrderedByReturn() throws Exception;
    /** Paid (CONFIRMED) bookings whose pickup date has arrived or passed but the customer hasn't been checked in — feeds the Booking Staff dashboard. */
    List<Booking> findPickupsDue() throws Exception;
    /** All not-yet-finished bookings (AWAITING_PAYMENT/CONFIRMED/ONGOING) for one vehicle — feeds the booking calendar. */
    List<Booking> findActiveByVehicle(long vehicleId) throws Exception;
    /** Bookings still AWAITING_PAYMENT that were created before the given cutoff — used by the auto-cancel job. */
    List<Booking> findExpiredAwaitingPayment(java.time.LocalDateTime cutoff) throws Exception;
    boolean updateStatus(long bookingId, String status) throws Exception;
    boolean markReturned(long bookingId, BigDecimal lateFee) throws Exception;
    /** Renter edits their still-unpaid booking's dates/total before paying. */
    boolean updateDates(long bookingId, java.time.LocalDate start, java.time.LocalDate end, java.math.BigDecimal totalAmount) throws Exception;
    boolean hasDateConflict(long vehicleId, java.time.LocalDate start, java.time.LocalDate end) throws Exception;
    boolean hasDateConflict(long vehicleId, java.time.LocalDate start, java.time.LocalDate end, long excludeBookingId) throws Exception;
}
