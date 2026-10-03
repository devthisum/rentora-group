package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.model.Vehicle;
import com.rentora.service.PromotionService;
import com.rentora.service.VehicleService;
import com.rentora.service.WishlistService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Public vehicle search/browse page — no login required to browse. */
@WebServlet("/vehicles")
public class VehicleSearchServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();
    private final WishlistService wishlistService = new WishlistService();
    private final PromotionService promotionService = new PromotionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> filters = new HashMap<>();
        addIfPresent(req, filters, "category");
        addIfPresent(req, filters, "minPrice");
        addIfPresent(req, filters, "maxPrice");
        addIfPresent(req, filters, "transmission");
        addIfPresent(req, filters, "fuelType");
        addIfPresent(req, filters, "q");

        try {
            List<Vehicle> results = vehicleService.search(filters);
            promotionService.applyActivePromotions(results);
            if ("1".equals(req.getParameter("deals"))) {
                // "On sale" view: only vehicles with an active promotion, biggest discount first
                results = results.stream().filter(Vehicle::isHasPromotion)
                        .sorted(java.util.Comparator.comparingInt(Vehicle::getDiscountPercent).reversed())
                        .collect(java.util.stream.Collectors.toList());
            }
            results = sorted(results, req.getParameter("sort"));
            req.setAttribute("vehicles", results);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Unable to load vehicles right now.");
        }

        req.setAttribute("favoritedIds", getFavoritedIds(req));
        req.getRequestDispatcher("/WEB-INF/views/renter/vehicle-list.jsp").forward(req, resp);
    }

    /** Price used for sorting = what the renter would actually pay per day (promotion price if a deal is on). */
    private static java.math.BigDecimal effectivePrice(Vehicle v) {
        return v.isHasPromotion() && v.getDiscountedPrice() != null ? v.getDiscountedPrice() : v.getPricePerDay();
    }

    /** Applies the chosen sort; an unknown/empty value keeps the default order. */
    private static List<Vehicle> sorted(List<Vehicle> list, String sort) {
        if (sort == null || list == null || list.size() < 2) return list;
        java.util.Comparator<Vehicle> byPrice = java.util.Comparator.comparing(VehicleSearchServlet::effectivePrice);
        java.util.Comparator<Vehicle> cmp = switch (sort) {
            case "price_asc" -> byPrice;
            case "price_desc" -> byPrice.reversed();
            case "rating" -> java.util.Comparator.comparingDouble(Vehicle::getAverageRating).reversed().thenComparing(byPrice);
            case "discount" -> java.util.Comparator.comparingInt(Vehicle::getDiscountPercent).reversed().thenComparing(byPrice);
            case "newest" -> java.util.Comparator.comparingInt(Vehicle::getYear).reversed().thenComparing(byPrice);
            default -> null;
        };
        if (cmp == null) return list;
        return list.stream().sorted(cmp).collect(java.util.stream.Collectors.toList());
    }

    private void addIfPresent(HttpServletRequest req, Map<String, String> filters, String key) {
        String value = req.getParameter(key);
        if (value != null && !value.isBlank()) filters.put(key, value);
    }

    private Set<Long> getFavoritedIds(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return Collections.emptySet();
        User user = (User) session.getAttribute("user");
        if (user == null || !"RENTER".equalsIgnoreCase(user.getRoleName())) return Collections.emptySet();
        try {
            return wishlistService.getFavoritedVehicleIds(user.getUserId());
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}
