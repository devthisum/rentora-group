package com.rentora.service;

import com.rentora.dao.impl.PasswordResetOtpDAOImpl;
import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.interfaces.PasswordResetOtpDAO;
import com.rentora.dao.interfaces.UserDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.PasswordResetOtp;
import com.rentora.model.User;
import com.rentora.util.MailService;
import com.rentora.util.PasswordHasher;
import com.rentora.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.Optional;

/** Business logic for the "forgot password" flow: request an OTP by email, then verify it to set a new password. */
public class PasswordResetService {

    private final UserDAO userDAO = new UserDAOImpl();
    private final PasswordResetOtpDAO otpDAO = new PasswordResetOtpDAOImpl();
    private final MailService mailService = MailService.getInstance();

    private static final int OTP_VALID_MINUTES = 10;

    /**
     * Generates and emails a fresh OTP for the given email address.
     * Always succeeds silently if the email doesn't exist — this avoids
     * leaking which emails are registered to an attacker probing the form.
     */
    public void requestOtp(String email) throws Exception {
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Please enter a valid email address.");
        }
        Optional<User> maybeUser = userDAO.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return; // silently no-op — don't reveal whether the account exists
        }
        User user = maybeUser.get();

        otpDAO.invalidatePreviousForUser(user.getUserId());

        String code = mailService.generateOtp();
        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setUserId(user.getUserId());
        otp.setOtpCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
        otpDAO.create(otp);

        mailService.sendPasswordResetOtp(user.getEmail(), user.getFullName(), code);
    }

    /** Verifies the OTP and, if valid, sets the new password. Throws ValidationException with a user-facing message otherwise. */
    public void verifyOtpAndReset(String email, String otpCode, String newPassword) throws Exception {
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isStrongPassword(newPassword)) {
            throw new ValidationException(
                "New password must be 8+ characters and include uppercase, lowercase, a digit, and a special character.");
        }

        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid code. Please request a new one."));

        PasswordResetOtp otp = otpDAO.findLatestByUserAndCode(user.getUserId(), otpCode)
                .orElseThrow(() -> new ValidationException("Invalid code. Please check the code and try again."));

        if (otp.isUsed()) {
            throw new ValidationException("This code has already been used. Please request a new one.");
        }
        if (otp.isExpired()) {
            throw new ValidationException("This code has expired. Please request a new one.");
        }

        userDAO.updatePassword(user.getUserId(), PasswordHasher.hash(newPassword));
        otpDAO.markUsed(otp.getOtpId());
    }
}
