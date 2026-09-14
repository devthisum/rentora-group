package com.rentora.model;

import java.time.LocalDateTime;

public class Review {
    private long reviewId;
    private long bookingId;
    private long renterId;
    private long vehicleId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private String renterName; // joined for display

    public long getReviewId() { return reviewId; }
    public void setReviewId(long reviewId) { this.reviewId = reviewId; }

    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }

    public long getRenterId() { return renterId; }
    public void setRenterId(long renterId) { this.renterId = renterId; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }
}
