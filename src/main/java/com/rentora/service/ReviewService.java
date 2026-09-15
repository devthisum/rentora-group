package com.rentora.service;

import com.rentora.dao.impl.BookingDAOImpl;
import com.rentora.dao.impl.ReviewDAOImpl;
import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.BookingDAO;
import com.rentora.dao.interfaces.ReviewDAO;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.Booking;
import com.rentora.model.Review;

import java.util.List;
import java.util.Optional;

/** Business logic for renter reviews and the vehicle rating they roll up into. */
public class ReviewService {

    private final ReviewDAO reviewDAO = new ReviewDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();

    /** A renter may review a booking only once, and only after it's been marked COMPLETED. */
    public long submitReview(long renterId, long bookingId, int rating, String comment) throws Exception {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5.");
        }
        Optional<Booking> maybeBooking = bookingDAO.findById(bookingId);
        if (maybeBooking.isEmpty() || maybeBooking.get().getRenterId() != renterId) {
            throw new ValidationException("You can only review your own bookings.");
        }
        Booking booking = maybeBooking.get();
        if (!"COMPLETED".equals(booking.getStatus())) {
            throw new ValidationException("You can only review a booking after it has been completed.");
        }
        if (reviewDAO.findByBooking(bookingId).isPresent()) {
            throw new ValidationException("You've already reviewed this booking.");
        }

        Review review = new Review();
        review.setBookingId(bookingId);
        review.setRenterId(renterId);
        review.setVehicleId(booking.getVehicleId());
        review.setRating(rating);
        review.setComment(comment);
        long reviewId = reviewDAO.create(review);

        // Recalculate and persist the vehicle's rolling average rating
        double newAverage = reviewDAO.getAverageRatingForVehicle(booking.getVehicleId());
        vehicleDAO.updateAverageRating(booking.getVehicleId(), newAverage);

        return reviewId;
    }

    public List<Review> getByVehicle(long vehicleId) throws Exception {
        return reviewDAO.findByVehicle(vehicleId);
    }

    public Optional<Review> getByBooking(long bookingId) throws Exception {
        return reviewDAO.findByBooking(bookingId);
    }

    /** A renter can go back and edit their own review at any time. */
    public void updateReview(long reviewId, long renterId, int rating, String comment) throws Exception {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5.");
        }
        Optional<Review> maybeReview = reviewDAO.findById(reviewId);
        if (maybeReview.isEmpty() || maybeReview.get().getRenterId() != renterId) {
            throw new ValidationException("You can only edit your own reviews.");
        }
        Review review = maybeReview.get();
        reviewDAO.update(reviewId, rating, comment);

        // The average shifts whenever a rating changes, so recalculate it.
        double newAverage = reviewDAO.getAverageRatingForVehicle(review.getVehicleId());
        vehicleDAO.updateAverageRating(review.getVehicleId(), newAverage);
    }

    /** A renter can remove their own review at any time. Recalculates the vehicle's average rating afterward. */
    public void deleteReview(long reviewId, long renterId) throws Exception {
        Review review = reviewDAO.findById(reviewId)
                .orElseThrow(() -> new ValidationException("Review not found."));
        if (review.getRenterId() != renterId) {
            throw new ValidationException("You can only delete your own reviews.");
        }
        reviewDAO.delete(reviewId);

        // Recalculate — getAverageRatingForVehicle already returns 0.0 if no reviews remain.
        double newAverage = reviewDAO.getAverageRatingForVehicle(review.getVehicleId());
        vehicleDAO.updateAverageRating(review.getVehicleId(), newAverage);
    }
}
