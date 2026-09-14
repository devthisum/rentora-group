-- Run this once against your existing Rentora database to add the new
-- "Booking Staff" role — the front-desk role that confirms pickups,
-- no-shows, and returns (see the accompanying app changes).
--
-- Safe to run even if it's already been applied: INSERT IGNORE skips it
-- silently if the 'BOOKING' role already exists (role_name is UNIQUE).

INSERT IGNORE INTO roles (role_name) VALUES ('BOOKING');

-- Nothing else to change schema-wise — Booking Staff accounts use the same
-- `users` table as everyone else, just with this new role_id.
