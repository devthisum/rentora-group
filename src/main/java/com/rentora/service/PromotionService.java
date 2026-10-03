package com.rentora.service;

import com.rentora.dao.impl.PromotionDAOImpl;
import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.PromotionDAO;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.Promotion;
import com.rentora.model.Vehicle;
import com.rentora.observer.NotificationEvent;
import com.rentora.observer.NotificationSubject;
import com.rentora.dao.impl.WishlistDAOImpl;
import com.rentora.dao.interfaces.WishlistDAO;
import com.rentora.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Business logic for staff-created vehicle promotions (price-drop deals). */
public class PromotionService {

    private final PromotionDAO promotionDAO = new PromotionDAOImpl();
    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();
    private final WishlistDAO wishlistDAO = new WishlistDAOImpl();

    /** Observer Pattern: fires "price drop" events to renters who wishlisted the vehicle. May be null (no alerts). */
    private final NotificationSubject notificationSubject;

    /** For read-only uses (display, pricing) — no notifications are sent. */
    public PromotionService() { this(null); }

    public PromotionService(NotificationSubject notificationSubject) {
        this.notificationSubject = notificationSubject;
    }

    public long createPromotion(long vehicleId, String title, String description, String discountType,
                                 BigDecimal discountValue, LocalDate startDate, LocalDate endDate,
                                 long staffUserId) throws Exception {
        Vehicle vehicle = vehicleDAO.findById(vehicleId)
                .orElseThrow(() -> new ValidationException("Vehicle not found."));
        validate(vehicle, title, discountType, discountValue, startDate, endDate);

        Promotion promotion = new Promotion();
        promotion.setVehicleId(vehicleId);
        promotion.setTitle(title.trim());
        promotion.setDescription(description == null ? null : description.trim());
        promotion.setDiscountType(discountType);
        promotion.setDiscountValue(discountValue);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setStatus("ACTIVE");
        promotion.setCreatedBy(staffUserId);

        long id = promotionDAO.create(promotion);
        notifyWishlisters(vehicle, promotion);
        return id;
    }

    /**
     * Tells every renter who wishlisted this vehicle that it just got a deal. Goes through the
     * Observer subject, so the in-app bell and the email channel both react without this class
     * knowing how either one delivers. Never allowed to break promotion creation.
     */
    private void notifyWishlisters(Vehicle vehicle, Promotion promotion) {
        if (notificationSubject == null) return;
        try {
            BigDecimal discounted = promotion.applyTo(vehicle.getPricePerDay());
            String when = promotion.getStartDate().isAfter(LocalDate.now())
                    ? "starts " + promotion.getStartDate() + " and ends " + promotion.getEndDate()
                    : "ends " + promotion.getEndDate();
            String message = vehicle.getBrand() + " " + vehicle.getModel() + " on your wishlist is now Rs. "
                    + discounted.stripTrailingZeros().toPlainString() + "/day (was Rs. "
                    + vehicle.getPricePerDay().stripTrailingZeros().toPlainString() + "). \""
                    + promotion.getTitle() + "\" " + when + ".";
            for (long renterId : wishlistDAO.findRenterIdsByVehicle(vehicle.getVehicleId())) {
                notificationSubject.notifyAll(new NotificationEvent(renterId, "Price drop on your wishlist", message));
            }
        } catch (Exception e) {
            System.err.println("Wishlist promotion alert failed: " + e.getMessage());
        }
    }

    /** Vehicles with an active promotion right now (not archived), biggest discount first. */
    public List<Vehicle> getHotDeals(int limit) throws Exception {
        List<Vehicle> deals = vehicleDAO.findAll().stream()
                .filter(v -> !v.isArchived())
                .collect(Collectors.toList());
        applyActivePromotions(deals);
        return deals.stream()
                .filter(Vehicle::isHasPromotion)
                .sorted(java.util.Comparator.comparingInt(Vehicle::getDiscountPercent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void updatePromotion(long promotionId, String title, String description, String discountType,
                                 BigDecimal discountValue, LocalDate startDate, LocalDate endDate,
                                 String status) throws Exception {
        Promotion promotion = promotionDAO.findById(promotionId)
                .orElseThrow(() -> new ValidationException("Promotion not found."));
        Vehicle vehicle = vehicleDAO.findById(promotion.getVehicleId())
                .orElseThrow(() -> new ValidationException("Vehicle not found."));
        validate(vehicle, title, discountType, discountValue, startDate, endDate);

        promotion.setTitle(title.trim());
        promotion.setDescription(description == null ? null : description.trim());
        promotion.setDiscountType(discountType);
        promotion.setDiscountValue(discountValue);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setStatus("ACTIVE".equalsIgnoreCase(status) ? "ACTIVE" : "INACTIVE");

        promotionDAO.update(promotion);
    }

    public void deletePromotion(long promotionId) throws Exception {
        promotionDAO.delete(promotionId);
    }

    public Optional<Promotion> getById(long promotionId) throws Exception {
        return promotionDAO.findById(promotionId);
    }

    public List<Promotion> getAll() throws Exception {
        return promotionDAO.findAll();
    }

    public List<Promotion> getByVehicle(long vehicleId) throws Exception {
        return promotionDAO.findByVehicle(vehicleId);
    }

    /** The one active, in-date-range promotion for a vehicle right now, if any. */
    public Optional<Promotion> getActiveForVehicle(long vehicleId) throws Exception {
        return promotionDAO.findActiveForVehicle(vehicleId);
    }

    /**
     * What a renter actually pays per day, right now, for this vehicle —
     * the discounted price if an active promotion applies, otherwise the
     * vehicle's normal listed price. This is the single source of truth
     * BookingService uses when calculating a booking total, so a promotion
     * isn't just a cosmetic badge — it actually changes what gets charged.
     */
    public BigDecimal getEffectivePricePerDay(Vehicle vehicle) throws Exception {
        Optional<Promotion> active = promotionDAO.findActiveForVehicle(vehicle.getVehicleId());
        return active.map(p -> p.applyTo(vehicle.getPricePerDay())).orElse(vehicle.getPricePerDay());
    }

    /**
     * Stamps every vehicle in the list with its active promotion's display
     * info (discounted price / badge text), if it has one — one query for
     * the whole list instead of one per card.
     */
    public void applyActivePromotions(List<Vehicle> vehicles) throws Exception {
        if (vehicles.isEmpty()) return;
        List<Promotion> active = promotionDAO.findAllActive();
        Map<Long, Promotion> byVehicleId = active.stream()
                .collect(Collectors.toMap(Promotion::getVehicleId, p -> p, (a, b) -> a));
        for (Vehicle v : vehicles) {
            Promotion promo = byVehicleId.get(v.getVehicleId());
            if (promo != null) {
                v.setHasPromotion(true);
                v.setPromotionTitle(promo.getTitle());
                BigDecimal discounted = promo.applyTo(v.getPricePerDay());
                v.setDiscountedPrice(discounted);
                v.setPromotionDescription(promo.getDescription());
                v.setPromotionEndDate(promo.getEndDate());
                // the promotion runs through the END of its last day
                v.setPromotionEndMillis(promo.getEndDate().atTime(23, 59, 59)
                        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
                if (v.getPricePerDay() != null && v.getPricePerDay().signum() > 0) {
                    v.setDiscountPercent(v.getPricePerDay().subtract(discounted)
                            .multiply(new BigDecimal("100"))
                            .divide(v.getPricePerDay(), 0, java.math.RoundingMode.HALF_UP).intValue());
                }
            }
        }
    }

    public void applyActivePromotion(Vehicle vehicle) throws Exception {
        applyActivePromotions(List.of(vehicle));
    }

    private void validate(Vehicle vehicle, String title, String discountType, BigDecimal discountValue,
                           LocalDate startDate, LocalDate endDate) throws ValidationException {
        if (!ValidationUtil.isNotBlank(title)) {
            throw new ValidationException("Promotion title is required.");
        }
        if (!"PERCENTAGE".equalsIgnoreCase(discountType) && !"FIXED_AMOUNT".equalsIgnoreCase(discountType)) {
            throw new ValidationException("Invalid discount type.");
        }
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Discount value must be greater than zero.");
        }
        if ("PERCENTAGE".equalsIgnoreCase(discountType) && discountValue.compareTo(new BigDecimal("90")) > 0) {
            throw new ValidationException("Percentage discount can't exceed 90%.");
        }
        if ("FIXED_AMOUNT".equalsIgnoreCase(discountType) && discountValue.compareTo(vehicle.getPricePerDay()) >= 0) {
            throw new ValidationException("Fixed discount can't be greater than or equal to the vehicle's daily price.");
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new ValidationException("Invalid promotion date range.");
        }
    }
}
