package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.model.Vehicle;
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
            req.setAttribute("vehicles", results);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Unable to load vehicles right now.");
        }

        req.setAttribute("favoritedIds", getFavoritedIds(req));
        req.getRequestDispatcher("/WEB-INF/views/renter/vehicle-list.jsp").forward(req, resp);
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
