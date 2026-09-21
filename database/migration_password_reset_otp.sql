-- Run this once against your existing Rentora database to add
-- forgot-password support. Each row is one 6-digit OTP emailed to a
-- user; it expires after 10 minutes and can only be used once.

CREATE TABLE IF NOT EXISTS password_reset_otps (
    otp_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    otp_code    VARCHAR(6) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_otp_user_code (user_id, otp_code)
);
