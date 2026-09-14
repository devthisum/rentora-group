package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.model.Vehicle;
import com.rentora.service.ReviewService;
import com.rentora.service.VehicleImageService;
import com.rentora.service.VehicleService;
import com.rentora.service.WishlistService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/vehicle-details")
public class VehicleDetailsServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();
    private final VehicleImageService vehicleImageService = new VehicleImageService();
    private final ReviewService reviewService = new ReviewService();
    private final WishlistService wishlistService = new WishlistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = Long.parseLong(req.getParameter("id"));
            Optional<Vehicle> vehicle = vehicleService.getById(id);
            if (vehicle.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Vehicle not found.");
                return;
            }
            req.setAttribute("vehicle", vehicle.get());
            req.setAttribute("galleryImages", vehicleImageService.getGallery(id));
            req.setAttribute("reviews", reviewService.getByVehicle(id));
            req.setAttribute("isFavorited", isFavorited(req, id));
            req.setAttribute("relatedVehicles", getRelatedVehicles(vehicle.get()));
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Unable to load this vehicle.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/vehicle-details.jsp").forward(req, resp);
    }

    /** Up to 4 other AVAILABLE vehicles in the same category, for the "You might also like" strip. */
    private List<Vehicle> getRelatedVehicles(Vehicle current) {
        try {
            return vehicleService.getAll().stream()
                    .filter(v -> v.getVehicleId() != current.getVehicleId())
                    .filter(v -> v.getCategoryId() == current.getCategoryId())
                    .filter(v -> "AVAILABLE".equalsIgnoreCase(v.getDisplayStatus()))
                    .limit(4)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean isFavorited(HttpServletRequest req, long vehicleId) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        User user = (User) session.getAttribute("user");
        if (user == null || !"RENTER".equalsIgnoreCase(user.getRoleName())) return false;
        try {
            return wishlistService.isFavorited(user.getUserId(), vehicleId);
        } catch (Exception e) {
            return false;
        }
    }
}
