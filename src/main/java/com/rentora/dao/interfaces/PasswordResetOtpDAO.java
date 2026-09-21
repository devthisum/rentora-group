package com.rentora.dao.interfaces;

import com.rentora.model.PasswordResetOtp;
import java.util.Optional;

public interface PasswordResetOtpDAO {
    long create(PasswordResetOtp otp) throws Exception;

    /** Finds the most recent OTP for this user+code pair, valid or not — the service layer checks validity. */
    Optional<PasswordResetOtp> findLatestByUserAndCode(long userId, String otpCode) throws Exception;

    boolean markUsed(long otpId) throws Exception;

    /** Invalidates any earlier unused OTPs for this user when a new one is requested, so only the newest code works. */
    void invalidatePreviousForUser(long userId) throws Exception;
}
