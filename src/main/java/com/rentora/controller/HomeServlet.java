package com.rentora.controller;

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
import java.util.List;
import java.util.Set;

/**
 * Home / landing page. Pulls a handful of real approved listings for the
 * "Popular Cars" section so the homepage reflects live marketplace data
 * instead of static placeholder content.
 */
@WebServlet("")
public class HomeServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();
    private final WishlistService wishlistService = new WishlistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Vehicle> approved = vehicleService.search(Collections.emptyMap());
            // Show at most 6 on the homepage teaser grid
            req.setAttribute("featuredVehicles", approved.size() > 6 ? approved.subList(0, 6) : approved);
        } catch (Exception e) {
            req.setAttribute("featuredVehicles", Collections.emptyList());
        }

        req.setAttribute("favoritedIds", getFavoritedIds(req));
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
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
