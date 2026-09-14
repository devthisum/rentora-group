-- Run this once against your existing Rentora database to add late-fee
-- tracking to bookings — Booking Staff confirming a return now
-- automatically calculates 30% of the vehicle's daily price for every day
-- it comes back late, stored here.

ALTER TABLE bookings ADD COLUMN late_fee DECIMAL(10,2) DEFAULT 0.00 AFTER total_amount;
