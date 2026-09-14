-- Run this once against your existing Rentora database to add the new
-- "Technical Specification" / "Car Equipment" fields to the vehicles table.

ALTER TABLE vehicles ADD COLUMN doors TINYINT AFTER average_rating;
ALTER TABLE vehicles ADD COLUMN air_conditioner ENUM('YES','NO') DEFAULT 'YES' AFTER doors;
ALTER TABLE vehicles ADD COLUMN mileage INT AFTER air_conditioner;
ALTER TABLE vehicles ADD COLUMN features VARCHAR(500) AFTER mileage;

-- Optional: backfill your existing vehicles with some sensible defaults so
-- the new "Technical Specification" / "Car Equipment" sections aren't empty.
-- Edit the vehicle_id / values to match your actual stock, or just leave
-- these NULL — the vehicle-details page already handles missing values.
--
-- UPDATE vehicles SET doors = 4, air_conditioner = 'YES', mileage = 500,
--   features = 'ABS,Air Bags,Cruise Control,Air Conditioner,Bluetooth,GPS'
-- WHERE vehicle_id = 1;
