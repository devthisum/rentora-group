-- Run this once against your existing Rentora database to add staff-managed
-- promotions (price-drop deals) on individual vehicles.

CREATE TABLE IF NOT EXISTS promotions (
    promotion_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id      BIGINT NOT NULL,
    title           VARCHAR(150) NOT NULL,
    description     VARCHAR(500),
    discount_type   ENUM('PERCENTAGE','FIXED_AMOUNT') NOT NULL,
    discount_value  DECIMAL(10,2) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
    created_by      BIGINT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    INDEX idx_promo_vehicle_dates (vehicle_id, status, start_date, end_date)
);
