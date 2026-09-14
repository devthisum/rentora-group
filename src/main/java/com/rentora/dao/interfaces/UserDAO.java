package com.rentora.dao.interfaces;

import com.rentora.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    long create(User user) throws Exception;
    Optional<User> findById(long userId) throws Exception;
    Optional<User> findByEmail(String email) throws Exception;
    boolean existsByEmail(String email) throws Exception;
    List<User> findAllByRole(String roleName) throws Exception;
    boolean updateStatus(long userId, String status) throws Exception;
    boolean update(User user) throws Exception;
    /** Saves the checkout-required fields (address + driving license) collected on the payment page. */
    boolean updateCheckoutInfo(long userId, String addressStreet, String addressCity, String addressPostalCode, String drivingLicenseNumber) throws Exception;
    boolean updatePassword(long userId, String newPasswordHash) throws Exception;
    boolean delete(long userId) throws Exception;
    /** Looks up a role's ID by name from the actual `roles` table — returns -1 if it doesn't exist. */
    int findRoleIdByName(String roleName) throws Exception;
}
