package com.rentora.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Centralized server-side validation. Every user-submitted field passes
 * through here before reaching the service/DAO layers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0[0-9]{9}$"); // Sri Lankan style local number, adjust per locale

    private static final Pattern NIC_PATTERN =
            Pattern.compile("^([0-9]{9}[vVxX]|[0-9]{12})$");

    private static final Pattern VEHICLE_NUMBER_PATTERN =
            Pattern.compile("^[A-Z]{2,3}-[0-9]{4}$");

    // Min 8 chars, at least one uppercase, one lowercase, one digit, one special char
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");

    private ValidationUtil() { }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidNIC(String nic) {
        return nic != null && NIC_PATTERN.matcher(nic).matches();
    }

    public static boolean isValidVehicleNumber(String vehicleNumber) {
        return vehicleNumber != null && VEHICLE_NUMBER_PATTERN.matcher(vehicleNumber).matches();
    }

    public static boolean isStrongPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidPrice(double price) {
        return price > 0 && price < 1_000_000;
    }

    public static boolean isValidDateRange(LocalDate start, LocalDate end) {
        return start != null && end != null && !start.isAfter(end) && !start.isBefore(LocalDate.now());
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** Basic image file extension validation (call before/along with content-type check). */
    public static boolean isValidImageFileName(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".webp");
    }
}
