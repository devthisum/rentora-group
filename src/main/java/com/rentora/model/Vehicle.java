package com.rentora.model;

import java.math.BigDecimal;
import java.util.List;

/** Base Vehicle entity (shop stock — added by admin). Subclassed per category by VehicleFactory. */
public class Vehicle {
    private long vehicleId;
    private int categoryId;
    private String categoryName;
    private long addedBy;
    private String vehicleNumber;
    private String brand;
    private String model;
    private int year;
    private int seats;
    private String transmission;
    private String fuelType;
    private BigDecimal pricePerDay;
    private String description;
    private String imageUrl;
    private String status; // AVAILABLE, BOOKED, CHECKING, MAINTENANCE
    private double averageRating;
    private List<String> imageUrls;

    // Extended technical specs (optional — may be null/0 on rows saved before these fields existed)
    private Integer doors;
    private String airConditioner;   // "YES" or "NO"
    private Integer mileage;         // total distance driven, in km
    private String features;         // comma-separated equipment list, e.g. "ABS,Air Bags,Cruise Control"

    /** Computed at query time: is there a CONFIRMED/ONGOING booking covering *today*? Not persisted. */
    private boolean bookedToday;

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public long getAddedBy() { return addedBy; }
    public void setAddedBy(long addedBy) { this.addedBy = addedBy; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    // ---- Promotion display fields (stamped on by PromotionService, not stored on this table) ----
    private boolean hasPromotion;
    private String promotionTitle;
    private java.math.BigDecimal discountedPrice;

    public boolean isHasPromotion() { return hasPromotion; }
    public void setHasPromotion(boolean hasPromotion) { this.hasPromotion = hasPromotion; }

    public String getPromotionTitle() { return promotionTitle; }
    public void setPromotionTitle(String promotionTitle) { this.promotionTitle = promotionTitle; }

    public java.math.BigDecimal getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(java.math.BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Integer getDoors() { return doors; }
    public void setDoors(Integer doors) { this.doors = doors; }

    public String getAirConditioner() { return airConditioner; }
    public void setAirConditioner(String airConditioner) { this.airConditioner = airConditioner; }

    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    /** Car Equipment list parsed from the comma-separated "features" column, trimmed and blank-filtered. */
    public List<String> getFeatureList() {
        if (features == null || features.isBlank()) return java.util.Collections.emptyList();
        List<String> list = new java.util.ArrayList<>();
        for (String f : features.split(",")) {
            String trimmed = f.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    public boolean isBookedToday() { return bookedToday; }
    public void setBookedToday(boolean bookedToday) { this.bookedToday = bookedToday; }

    /**
     * The status to actually show on a badge: reflects what's true about the
     * vehicle *today*, not a future reservation. A vehicle booked for next
     * week still shows AVAILABLE right now — it only flips to BOOKED (green)
     * on the actual pickup day. CHECKING/MAINTENANCE are always current
     * (they're only ever set the moment a vehicle is physically returned),
     * so those pass through unchanged.
     */
    public String getDisplayStatus() {
        if ("CHECKING".equals(status) || "MAINTENANCE".equals(status)) {
            return status;
        }
        return bookedToday ? "BOOKED" : "AVAILABLE";
    }
}
