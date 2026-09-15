package com.rentora.dao.interfaces;

import com.rentora.model.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewDAO {
    long create(Review review) throws Exception;
    boolean update(long reviewId, int rating, String comment) throws Exception;
    boolean delete(long reviewId) throws Exception;
    List<Review> findByVehicle(long vehicleId) throws Exception;
    Optional<Review> findByBooking(long bookingId) throws Exception;
    Optional<Review> findById(long reviewId) throws Exception;
    double getAverageRatingForVehicle(long vehicleId) throws Exception;
}
