package com.rentora.model;

import java.time.LocalDateTime;

/** Base user entity shared by Renter, Owner, and Admin (role-discriminated). */
public class User {
    private long userId;
    private int roleId;
    private String roleName;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private String nicNumber;
    private String addressStreet;
    private String addressCity;
    private String addressPostalCode;
    private String drivingLicenseNumber;
    private String profileImage;
    private String status; // PENDING, ACTIVE, SUSPENDED
    private LocalDateTime createdAt;

    public User() { }

    public User(long userId, int roleId, String fullName, String email, String phone, String status) {
        this.userId = userId;
        this.roleId = roleId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNicNumber() { return nicNumber; }
    public void setNicNumber(String nicNumber) { this.nicNumber = nicNumber; }

    public String getAddressStreet() { return addressStreet; }
    public void setAddressStreet(String addressStreet) { this.addressStreet = addressStreet; }

    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }

    public String getAddressPostalCode() { return addressPostalCode; }
    public void setAddressPostalCode(String addressPostalCode) { this.addressPostalCode = addressPostalCode; }

    public String getDrivingLicenseNumber() { return drivingLicenseNumber; }
    public void setDrivingLicenseNumber(String drivingLicenseNumber) { this.drivingLicenseNumber = drivingLicenseNumber; }

    /** True once the renter has filled in everything needed before they can pay (address + license). */
    public boolean hasCompleteCheckoutInfo() {
        return notBlank(addressStreet) && notBlank(addressCity) && notBlank(addressPostalCode) && notBlank(drivingLicenseNumber);
    }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
