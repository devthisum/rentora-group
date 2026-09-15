-- ============================================================
-- RENTORA - Single-Shop Vehicle Rental System
-- MySQL Database Schema (Normalized, 3NF)
-- ============================================================

DROP DATABASE IF EXISTS rentora_db;
CREATE DATABASE rentora_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rentora_db;

-- ------------------------------------------------------------
-- ROLES & USERS
-- ------------------------------------------------------------
CREATE TABLE roles (
    role_id     INT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(30) NOT NULL UNIQUE   -- RENTER, ADMIN, MAINTENANCE, BOOKING
);

CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id         INT NOT NULL,
    full_name       VARCHAR(120) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(20) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    nic_number      VARCHAR(20) UNIQUE,
    address_street          VARCHAR(150) NULL,
    address_city            VARCHAR(80) NULL,
    address_postal_code     VARCHAR(20) NULL,
    driving_license_number  VARCHAR(40) NULL,
    profile_image   VARCHAR(255),
    status          ENUM('PENDING','ACTIVE','SUSPENDED') DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    INDEX idx_users_email (email)
);

-- ------------------------------------------------------------
-- VEHICLE CATALOG (shop-owned stock — vehicles are added directly
-- by admin/staff, there is no separate "owner" account type)
-- ------------------------------------------------------------
CREATE TABLE vehicle_categories (
    category_id     INT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(60) NOT NULL UNIQUE   -- Car, SUV, Luxury, Sports, Van, Bus, Motorcycle, ThreeWheeler, EV
);

CREATE TABLE vehicles (
    vehicle_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id      INT NOT NULL,
    added_by         BIGINT NULL,               -- admin/staff user who added this vehicle
    vehicle_number   VARCHAR(20) NOT NULL UNIQUE,
    brand            VARCHAR(60) NOT NULL,
    model            VARCHAR(60) NOT NULL,
    year             SMALLINT NOT NULL,
    seats            TINYINT,
    transmission     ENUM('MANUAL','AUTOMATIC') DEFAULT 'MANUAL',
    fuel_type        ENUM('PETROL','DIESEL','ELECTRIC','HYBRID') DEFAULT 'PETROL',
    price_per_day    DECIMAL(10,2) NOT NULL,
    description      TEXT,
    image_url        VARCHAR(500),
    -- AVAILABLE: in the shop, bookable
    -- BOOKED: currently rented out to a customer
    -- CHECKING: just returned, awaiting the post-rental inspection
    -- MAINTENANCE: inspection found a problem, vehicle is being repaired
    status           ENUM('AVAILABLE','BOOKED','CHECKING','MAINTENANCE') DEFAULT 'AVAILABLE',
    average_rating   DECIMAL(3,2) DEFAULT 0.00,
    doors            TINYINT,
    air_conditioner  ENUM('YES','NO') DEFAULT 'YES',
    mileage          INT,                        -- total distance driven, in km
    features         VARCHAR(500),                -- comma-separated equipment list, e.g. "ABS,Air Bags,Cruise Control"
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES vehicle_categories(category_id),
    FOREIGN KEY (added_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_vehicles_status (status)
);

CREATE TABLE vehicle_images (
    image_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id   BIGINT NOT NULL,
    image_url    VARCHAR(255) NOT NULL,
    is_primary   BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- INQUIRIES (threaded messaging between a renter and the shop/admin)
-- ------------------------------------------------------------
CREATE TABLE inquiry_threads (
    thread_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id     BIGINT NOT NULL,
    renter_id      BIGINT NOT NULL,
    status         ENUM('OPEN','CLOSED') DEFAULT 'OPEN',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (renter_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_thread_renter (renter_id)
);

CREATE TABLE inquiry_messages (
    message_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id      BIGINT NOT NULL,
    sender_id      BIGINT NOT NULL,
    message        TEXT NOT NULL,
    is_read        BOOLEAN DEFAULT FALSE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (thread_id) REFERENCES inquiry_threads(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- ADMIN ANNOUNCEMENTS (broadcast board + per-user read tracking)
-- ------------------------------------------------------------
CREATE TABLE announcements (
    announcement_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    message          TEXT NOT NULL,
    priority         ENUM('LOW','NORMAL','HIGH','URGENT') DEFAULT 'NORMAL',
    category         VARCHAR(60),
    posted_by        BIGINT NOT NULL,
    expiry_date      DATE NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES users(user_id)
);

CREATE TABLE announcement_reads (
    user_id           BIGINT NOT NULL,
    announcement_id   BIGINT NOT NULL,
    read_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, announcement_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (announcement_id) REFERENCES announcements(announcement_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- BOOKINGS, PAYMENTS, INVOICES
-- (single shop — no pickup/return location fields needed)
-- ------------------------------------------------------------
CREATE TABLE bookings (
    booking_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    renter_id        BIGINT NOT NULL,
    vehicle_id       BIGINT NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE NOT NULL,
    total_amount     DECIMAL(10,2) NOT NULL,
    late_fee         DECIMAL(10,2) DEFAULT 0.00,   -- 30% of the vehicle's daily price, per day returned late
    coupon_id        BIGINT NULL,
    -- AWAITING_PAYMENT -> CONFIRMED -> ONGOING -> RETURNED (checking) -> COMPLETED
    -- or CANCELLED at any point before ONGOING
    status           ENUM('AWAITING_PAYMENT','CONFIRMED','ONGOING','RETURNED','COMPLETED','CANCELLED') DEFAULT 'AWAITING_PAYMENT',
    returned_at      TIMESTAMP NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (renter_id) REFERENCES users(user_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    INDEX idx_bookings_vehicle_dates (vehicle_id, start_date, end_date),
    INDEX idx_bookings_status (status)
);

CREATE TABLE coupons (
    coupon_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(30) NOT NULL UNIQUE,
    discount_pct   DECIMAL(5,2) NOT NULL,
    valid_from     DATE,
    valid_to       DATE,
    max_uses       INT DEFAULT 0,
    used_count     INT DEFAULT 0,
    active         BOOLEAN DEFAULT TRUE
);

ALTER TABLE bookings ADD FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id);

CREATE TABLE payments (
    payment_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id       BIGINT NOT NULL UNIQUE,
    amount           DECIMAL(10,2) NOT NULL,
    payment_method   ENUM('CARD','WALLET') NOT NULL,
    payment_status   ENUM('PENDING','SUCCESS','FAILED','REFUNDED') DEFAULT 'PENDING',
    transaction_ref  VARCHAR(100),
    paid_at          TIMESTAMP NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- SAVED PAYMENT METHODS
-- This is a simulated payment system — no real card processor is
-- involved anywhere in this app. To keep that safe even as "demo"
-- data, only a masked card number (last 4 digits) and expiry are
-- ever stored; the full card number and CVV are never saved,
-- exactly like the one-off checkout flow on the payment page.
-- ------------------------------------------------------------
CREATE TABLE saved_payment_methods (
    payment_method_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    type                ENUM('CARD','WALLET') NOT NULL,
    label               VARCHAR(60) NOT NULL,       -- e.g. "Personal Visa", "My Wallet"
    masked_number       VARCHAR(30),                -- e.g. "**** **** **** 4242" (CARD only)
    expiry              VARCHAR(5),                 -- MM/YY (CARD only)
    is_default          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE invoices (
    invoice_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id     BIGINT NOT NULL UNIQUE,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    pdf_path       VARCHAR(255),
    issued_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- REVIEWS & WISHLIST
-- ------------------------------------------------------------
CREATE TABLE reviews (
    review_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id   BIGINT NOT NULL UNIQUE,
    renter_id    BIGINT NOT NULL,
    vehicle_id   BIGINT NOT NULL,
    rating       TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment      TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (renter_id) REFERENCES users(user_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
);

CREATE TABLE wishlist (
    wishlist_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    renter_id    BIGINT NOT NULL,
    vehicle_id   BIGINT NOT NULL,
    added_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_wishlist (renter_id, vehicle_id),
    FOREIGN KEY (renter_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- MAINTENANCE WORKFLOW
--
-- One row is opened automatically whenever a rented vehicle is
-- marked "returned". It tracks the post-rental inspection through
-- to resolution:
--
--   CHECKING              -> admin/staff is inspecting the returned vehicle
--   OK                    -> no problem found; vehicle went back to AVAILABLE
--   UNDER_MAINTENANCE     -> a problem was found; vehicle is being repaired
--   AWAITING_CUSTOMER_PAYMENT -> repair cost is billed to the customer
--                                (customer was at fault) and is due within 24h
--   RESOLVED               -> maintenance finished, vehicle back to AVAILABLE
-- ------------------------------------------------------------
CREATE TABLE maintenance (
    maintenance_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id           BIGINT NOT NULL,
    booking_id           BIGINT NULL,          -- the rental that triggered this check, if any
    stage                ENUM('CHECKING','OK','UNDER_MAINTENANCE','AWAITING_CUSTOMER_PAYMENT','RESOLVED') DEFAULT 'CHECKING',
    problem_found        BOOLEAN NULL,
    notes                TEXT,
    estimated_days       INT,                  -- "under maintenance for ___ days"
    repair_cost          DECIMAL(10,2),
    customer_at_fault    BOOLEAN DEFAULT FALSE,
    extra_charge_pct     DECIMAL(5,2),          -- e.g. 20.00 = customer pays 20% extra on top of repair_cost
    extra_charge_amount  DECIMAL(10,2),
    total_charge         DECIMAL(10,2),         -- repair_cost + extra_charge_amount
    charge_paid          BOOLEAN DEFAULT FALSE,
    payment_due_at       TIMESTAMP NULL,        -- customer must pay within 24 hrs of being notified
    checked_by           BIGINT NULL,           -- admin/staff who performed the check
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL,
    FOREIGN KEY (checked_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_maintenance_stage (stage),
    INDEX idx_maintenance_vehicle (vehicle_id)
);

-- ------------------------------------------------------------
-- NOTIFICATIONS, REPORTS, AUDIT
-- ------------------------------------------------------------
CREATE TABLE notifications (
    notification_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    title            VARCHAR(150) NOT NULL,
    message          TEXT NOT NULL,
    is_read          BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id, is_read)
);

CREATE TABLE reports (
    report_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type   VARCHAR(60) NOT NULL,  -- REVENUE, BOOKINGS, MONTHLY
    date_from     DATE,
    date_to       DATE,
    file_path     VARCHAR(255),
    generated_by  BIGINT NOT NULL,
    generated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (generated_by) REFERENCES users(user_id)
);

CREATE TABLE audit_logs (
    log_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    action       VARCHAR(100) NOT NULL,
    entity_name  VARCHAR(60),
    entity_id    BIGINT,
    details      TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- SEED DATA
-- ------------------------------------------------------------
INSERT INTO roles (role_name) VALUES ('RENTER'), ('ADMIN'), ('MAINTENANCE'), ('BOOKING');

INSERT INTO vehicle_categories (category_name) VALUES
('Car'), ('SUV'), ('Luxury'), ('Sports'), ('Van'), ('Bus'), ('Motorcycle'), ('ThreeWheeler'), ('Electric Vehicle');

-- Default admin (password = Admin@123, bcrypt hash to be generated at app startup / seed script)
INSERT INTO users (role_id, full_name, email, phone, password_hash, status)
VALUES (2, 'Rentora Admin', 'admin@rentora.com', '0770000000',
        '$2a$12$examplebcrypthashreplaceatruntime', 'ACTIVE');

-- ------------------------------------------------------------
-- SAMPLE VEHICLES (3 per category, real makes/models/photos) --
-- See migration_seed_more_vehicles.sql for the same data if
-- you're applying this to an existing database instead.
-- ------------------------------------------------------------
-- vehicles (real makes/models, real photos) into EVERY category, so you're
-- not stuck testing with just one or two vehicles per type.
--
-- Uses (SELECT category_id FROM vehicle_categories WHERE category_name = '...')
-- instead of a hardcoded number for category_id, so it works regardless of
-- what order your categories actually ended up in.
--
-- All vehicle_number values are prefixed differently per category and start
-- at 3001+, chosen specifically to not collide with any vehicles you already
-- have in stock. If you get a "Duplicate entry" error, you already have a
-- vehicle using one of these numbers — just edit that one line's number.

-- ============ CAR ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Car'), 'CAR-3001', 'Toyota', 'Corolla', 2022, 5, 'AUTOMATIC', 'PETROL', 6500.00, 'Reliable, fuel-efficient sedan — a comfortable everyday choice for city driving and longer trips alike.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 18500, 'ABS,Air Bags,Bluetooth,Reverse Camera,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Car'), 'CAR-3002', 'Honda', 'Civic', 2023, 5, 'AUTOMATIC', 'PETROL', 7200.00, 'Sporty styling with a smooth ride — one of our most requested sedans.', 'https://images.unsplash.com/photo-1502877338535-766e1452684a?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 9200, 'ABS,Air Bags,Cruise Control,Bluetooth,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Car'), 'CAR-3003', 'Nissan', 'Sunny', 2020, 5, 'MANUAL', 'PETROL', 5500.00, 'Budget-friendly and economical — great for renters watching fuel costs.', 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 41000, 'ABS,Air Bags,Air Conditioner');

-- ============ SUV ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'SUV'), 'SUV-3001', 'Toyota', 'Fortuner', 2022, 7, 'AUTOMATIC', 'DIESEL', 14500.00, 'Spacious 7-seater with real off-road capability — ideal for family trips up-country.', 'https://images.unsplash.com/photo-1617531653332-bd46c24f2068?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 5, 'YES', 32000, 'ABS,Air Bags,4WD,Cruise Control,Reverse Camera,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'SUV'), 'SUV-3002', 'Suzuki', 'Vitara', 2021, 5, 'AUTOMATIC', 'PETROL', 9500.00, 'Compact SUV that''s easy to park in the city but still roomy inside.', 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 5, 'YES', 21000, 'ABS,Air Bags,Bluetooth,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'SUV'), 'SUV-3003', 'Mitsubishi', 'Montero', 2020, 7, 'AUTOMATIC', 'DIESEL', 13000.00, 'Rugged and dependable — a favorite for longer road trips with the whole family.', 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 5, 'YES', 47000, 'ABS,Air Bags,4WD,Air Conditioner');

-- ============ LUXURY ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Luxury'), 'LUX-3001', 'Mercedes-Benz', 'E-Class', 2023, 5, 'AUTOMATIC', 'PETROL', 22000.00, 'Executive comfort with premium leather interior — perfect for business travel or special occasions.', 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 15000, 'ABS,Air Bags,Leather Seats,Cruise Control,Sunroof,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Luxury'), 'LUX-3002', 'BMW', '7 Series', 2022, 5, 'AUTOMATIC', 'PETROL', 25000.00, 'Flagship sedan with commanding presence — chauffeur-ready and boardroom appropriate.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?q=80&w=850&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 12500, 'ABS,Air Bags,Leather Seats,Massage Seats,Sunroof,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Luxury'), 'LUX-3003', 'Audi', 'A6', 2021, 5, 'AUTOMATIC', 'PETROL', 21000.00, 'Understated elegance with Quattro all-wheel drive for a confident, refined ride.', 'https://images.unsplash.com/photo-1617531653332-bd46c24f2068?q=80&w=850&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 19000, 'ABS,Air Bags,Leather Seats,Cruise Control,Air Conditioner');

-- ============ SPORTS ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Sports'), 'SPT-3001', 'BMW', 'M4', 2023, 4, 'AUTOMATIC', 'PETROL', 28000.00, 'Track-bred performance you can drive to dinner — twin-turbo power in a head-turning body.', 'https://images.unsplash.com/photo-1684964355429-83fc3b3bbd45?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 2, 'YES', 8000, 'ABS,Air Bags,Sport Mode,Launch Control,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Sports'), 'SPT-3002', 'Porsche', '911', 2022, 2, 'AUTOMATIC', 'PETROL', 35000.00, 'The icon. Precise handling and unmistakable design for the ultimate weekend drive.', 'https://images.unsplash.com/photo-1502877338535-766e1452684a?q=80&w=850&auto=format&fit=crop', 'AVAILABLE', 2, 'YES', 6200, 'ABS,Air Bags,Sport Mode,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Sports'), 'SPT-3003', 'Ford', 'Mustang GT', 2021, 4, 'AUTOMATIC', 'PETROL', 24000.00, 'A V8 growl and classic American muscle styling — impossible to miss on the road.', 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=850&auto=format&fit=crop', 'AVAILABLE', 2, 'YES', 14000, 'ABS,Air Bags,Sport Mode,Cruise Control,Air Conditioner');

-- ============ VAN ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Van'), 'VAN-3001', 'Toyota', 'HiAce', 2021, 12, 'MANUAL', 'DIESEL', 13500.00, 'The workhorse for group travel — airport transfers, tours, and family gatherings.', 'https://images.unsplash.com/photo-1641199788912-9a7385a35c82?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 55000, 'Air Bags,Sliding Doors,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Van'), 'VAN-3002', 'Nissan', 'Caravan', 2020, 12, 'MANUAL', 'DIESEL', 12500.00, 'Roomy and dependable — a popular pick for church groups and staff transport.', 'https://images.unsplash.com/photo-1645351593532-e098e46f77c8?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 62000, 'Air Bags,Sliding Doors,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Van'), 'VAN-3003', 'KIA', 'Grand Carnival', 2022, 8, 'AUTOMATIC', 'DIESEL', 15000.00, 'A more premium 8-seat option with plush captain''s chairs — comfortable for longer trips.', 'https://images.unsplash.com/photo-1547052779-3cdf2b1ac2b1?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 24000, 'ABS,Air Bags,Captain Seats,Reverse Camera,Air Conditioner');

-- ============ BUS ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Bus'), 'BUS-3001', 'Toyota', 'Coaster', 2021, 28, 'MANUAL', 'DIESEL', 22000.00, 'Mid-size coach for tour groups and school trips — comfortable seating with good luggage space.', 'https://images.unsplash.com/photo-1546347930-c3a16cd9ff0a?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 2, 'YES', 78000, 'Air Bags,PA System,Luggage Rack,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Bus'), 'BUS-3002', 'Isuzu', 'Journey', 2019, 32, 'MANUAL', 'DIESEL', 20000.00, 'Larger capacity option for bigger groups and staff shuttle services.', 'https://images.unsplash.com/photo-1726242839974-36cee4ad0a7e?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 2, 'YES', 95000, 'PA System,Luggage Rack,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Bus'), 'BUS-3003', 'Mitsubishi', 'Rosa', 2020, 26, 'MANUAL', 'DIESEL', 21000.00, 'Well-maintained coach ideal for weekend tours and pilgrimage trips.', 'https://images.unsplash.com/photo-1750834366142-099844db6a6a?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 2, 'YES', 83000, 'PA System,Luggage Rack,Air Conditioner');

-- ============ MOTORCYCLE ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Motorcycle'), 'MC-3001', 'Honda', 'CB150R', 2022, 2, 'MANUAL', 'PETROL', 2500.00, 'Nimble and fuel-efficient — perfect for weaving through city traffic.', 'https://images.unsplash.com/photo-1743952861810-67181b6462d4?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', NULL, 'NO', 12000, 'Helmet Included,Storage Box'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Motorcycle'), 'MC-3002', 'Yamaha', 'FZ', 2021, 2, 'MANUAL', 'PETROL', 2200.00, 'A reliable daily rider with punchy acceleration for quick trips around town.', 'https://images.unsplash.com/photo-1726392935511-c9c72378a5f7?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', NULL, 'NO', 19500, 'Helmet Included'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Motorcycle'), 'MC-3003', 'Royal Enfield', 'Classic 350', 2022, 2, 'MANUAL', 'PETROL', 2800.00, 'Retro styling with that signature thump — great for scenic coastal rides.', 'https://images.unsplash.com/photo-1660648128024-cdd5f8930df6?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', NULL, 'NO', 8700, 'Helmet Included,Saddlebags');

-- ============ THREE WHEELER (TUK-TUK) ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'ThreeWheeler'), 'TW-3001', 'Bajaj', 'RE', 2021, 3, 'MANUAL', 'PETROL', 1800.00, 'The classic Sri Lankan tuk-tuk — open-sided, easy to drive, and perfect for exploring narrow roads.', 'https://images.unsplash.com/photo-1742282302368-823c94c174ea?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', NULL, 'NO', 34000, 'Phone Holder,Storage Box'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'ThreeWheeler'), 'TW-3002', 'TVS', 'King', 2020, 3, 'MANUAL', 'PETROL', 1700.00, 'Economical fuel use and easy handling — a great budget option for short trips.', 'https://images.unsplash.com/photo-1742282302316-c96c1f516776?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', NULL, 'NO', 41000, 'Phone Holder'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'ThreeWheeler'), 'TW-3003', 'Piaggio', 'Ape', 2022, 3, 'MANUAL', 'PETROL', 1900.00, 'Newer model with a smoother ride and better fuel economy than most tuk-tuks in the shop.', 'https://images.unsplash.com/photo-1742282302316-c96c1f516776?q=80&w=700&auto=format&fit=crop', 'AVAILABLE', NULL, 'NO', 9800, 'Phone Holder,Storage Box');

-- ============ ELECTRIC VEHICLE ============
INSERT INTO vehicles (category_id, vehicle_number, brand, model, year, seats, transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) VALUES
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Electric Vehicle'), 'EV-3001', 'Tesla', 'Model 3', 2023, 5, 'AUTOMATIC', 'ELECTRIC', 26000.00, 'Zero emissions with instant torque — Autopilot-equipped for a genuinely futuristic drive.', 'https://images.unsplash.com/photo-1685270386994-ae66d13d021e?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 15000, 'Autopilot,Reverse Camera,Fast Charging,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Electric Vehicle'), 'EV-3002', 'Nissan', 'Leaf', 2021, 5, 'AUTOMATIC', 'ELECTRIC', 12000.00, 'The world''s best-selling EV — practical, quiet, and cheap to run around town.', 'https://images.unsplash.com/photo-1745966325233-52c7ae2656fa?q=80&w=900&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 28000, 'Reverse Camera,Fast Charging,Air Conditioner'),
((SELECT category_id FROM vehicle_categories WHERE category_name = 'Electric Vehicle'), 'EV-3003', 'BYD', 'Atto 3', 2022, 5, 'AUTOMATIC', 'ELECTRIC', 15000.00, 'Long range and a spacious, tech-forward interior — great value for an electric SUV-crossover.', 'https://images.unsplash.com/photo-1745966325233-52c7ae2656fa?q=80&w=700&auto=format&fit=crop', 'AVAILABLE', 4, 'YES', 11000, 'Reverse Camera,Fast Charging,Sunroof,Air Conditioner');

-- ------------------------------------------------------------
-- PASSWORD RESET OTPs (forgot-password flow)
-- ------------------------------------------------------------
CREATE TABLE password_reset_otps (
    otp_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    otp_code    VARCHAR(6) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_otp_user_code (user_id, otp_code)
);
