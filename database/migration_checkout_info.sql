-- Run this once against your existing Rentora database to add the fields
-- needed for checkout (address + driving license), collected on the
-- payment page before a renter can pay for their first booking.

ALTER TABLE users ADD COLUMN address_street VARCHAR(150) NULL AFTER nic_number;
ALTER TABLE users ADD COLUMN address_city VARCHAR(80) NULL AFTER address_street;
ALTER TABLE users ADD COLUMN address_postal_code VARCHAR(20) NULL AFTER address_city;
ALTER TABLE users ADD COLUMN driving_license_number VARCHAR(40) NULL AFTER address_postal_code;
