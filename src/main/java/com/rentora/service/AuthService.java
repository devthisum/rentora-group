package com.rentora.service;

import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.interfaces.UserDAO;
import com.rentora.exception.AuthenticationException;
import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.util.PasswordHasher;
import com.rentora.util.ValidationUtil;

import java.util.Optional;

/** Business logic for registration, login, and role-based checks. */
public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();

    // Role IDs match the seeded `roles` table: 1=RENTER, 2=ADMIN, 3=MAINTENANCE, 4=BOOKING
    // (RENTER is fixed at registration time; staff roles are looked up dynamically below
    // instead of trusted as hardcoded IDs, since BOOKING may not exist yet on databases
    // that haven't run migration_booking_staff_role.sql.)
    private static final int ROLE_RENTER = 1;

    public User registerRenter(String fullName, String email, String phone, String password) throws Exception {
        validateBasics(fullName, email, phone, password);
        if (userDAO.existsByEmail(email)) {
            throw new ValidationException("An account with this email already exists.");
        }
        User user = new User();
        user.setRoleId(ROLE_RENTER);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setStatus("ACTIVE");

        long id = userDAO.create(user);
        user.setUserId(id);
        return user;
    }

    /**
     * Admin creates a staff login — staff can't self-register, only admin can
     * add them. Handles both staff roles this app has:
     *   "MAINTENANCE" — inspects returned vehicles, opens/resolves repairs
     *   "BOOKING"     — front desk: confirms pickups, no-shows, and returns
     */
    public User createStaffAccount(String fullName, String email, String phone, String password, String role) throws Exception {
        validateBasics(fullName, email, phone, password);
        if (userDAO.existsByEmail(email)) {
            throw new ValidationException("An account with this email already exists.");
        }
        if (!"MAINTENANCE".equalsIgnoreCase(role) && !"BOOKING".equalsIgnoreCase(role)) {
            throw new ValidationException("Invalid staff role.");
        }
        int roleId = userDAO.findRoleIdByName(role.toUpperCase());
        if (roleId <= 0) {
            throw new ValidationException(
                "The '" + role.toUpperCase() + "' role doesn't exist in the database yet. " +
                "Run database/migration_booking_staff_role.sql against your database, then try again.");
        }

        User user = new User();
        user.setRoleId(roleId);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setStatus("ACTIVE");

        long id = userDAO.create(user);
        user.setUserId(id);
        return user;
    }

    public User login(String email, String password) throws Exception {
        Optional<User> maybeUser = userDAO.findByEmail(email);
        if (maybeUser.isEmpty() || !PasswordHasher.verify(password, maybeUser.get().getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password.");
        }
        User user = maybeUser.get();
        if ("SUSPENDED".equals(user.getStatus())) {
            throw new AuthenticationException("This account has been suspended. Contact support.");
        }
        if ("PENDING".equals(user.getStatus())) {
            throw new AuthenticationException("Your account is pending admin approval.");
        }
        return user;
    }

    /** Any logged-in user (renter, admin, or maintenance staff) edits their own name/phone/photo. */
    public User updateProfile(long userId, String fullName, String phone, String profileImage) throws Exception {
        if (!ValidationUtil.isNotBlank(fullName)) throw new ValidationException("Full name is required.");
        if (!ValidationUtil.isValidPhone(phone)) throw new ValidationException("Invalid phone number.");

        User user = new User();
        user.setUserId(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setProfileImage(profileImage);
        userDAO.update(user);

        return userDAO.findById(userId).orElseThrow(() -> new ValidationException("Account not found."));
    }

    /**
     * Saves the address + driving license info required at checkout before a
     * renter can pay for their first booking. Collected once here, then
     * reused (pre-filled, editable) on every future payment.
     */
    public User saveCheckoutInfo(long userId, String addressStreet, String addressCity, String addressPostalCode, String drivingLicenseNumber) throws Exception {
        if (!ValidationUtil.isNotBlank(addressStreet)) throw new ValidationException("Street address is required.");
        if (!ValidationUtil.isNotBlank(addressCity)) throw new ValidationException("City is required.");
        if (!ValidationUtil.isNotBlank(addressPostalCode)) throw new ValidationException("Postal code is required.");
        if (!ValidationUtil.isNotBlank(drivingLicenseNumber)) throw new ValidationException("Driving license number is required.");

        userDAO.updateCheckoutInfo(userId, addressStreet.trim(), addressCity.trim(), addressPostalCode.trim(), drivingLicenseNumber.trim());
        return userDAO.findById(userId).orElseThrow(() -> new ValidationException("Account not found."));
    }

    public void changePassword(long userId, String currentPassword, String newPassword) throws Exception {
        User user = userDAO.findById(userId).orElseThrow(() -> new ValidationException("Account not found."));
        if (!PasswordHasher.verify(currentPassword, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }
        if (!ValidationUtil.isStrongPassword(newPassword)) {
            throw new ValidationException(
                "New password must be 8+ characters and include uppercase, lowercase, a digit, and a special character.");
        }
        userDAO.updatePassword(userId, PasswordHasher.hash(newPassword));
    }

    private void validateBasics(String fullName, String email, String phone, String password) throws ValidationException {
        if (!ValidationUtil.isNotBlank(fullName)) throw new ValidationException("Full name is required.");
        if (!ValidationUtil.isValidEmail(email)) throw new ValidationException("Invalid email address.");
        if (!ValidationUtil.isValidPhone(phone)) throw new ValidationException("Invalid phone number.");
        if (!ValidationUtil.isStrongPassword(password)) {
            throw new ValidationException(
                "Password must be 8+ characters and include uppercase, lowercase, a digit, and a special character.");
        }
    }
}
