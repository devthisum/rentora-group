package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.model.Vehicle;
import com.rentora.service.PromotionService;
import com.rentora.service.WishlistService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/renter/wishlist")
public class WishlistServlet extends HttpServlet {

    private final WishlistService wishlistService = new WishlistService();
    private final PromotionService promotionService = new PromotionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        try {
            String sortBy = req.getParameter("sort");
            String keyword = req.getParameter("q");
            List<Vehicle> vehicles = wishlistService.getSavedVehicles(renter.getUserId(), sortBy, keyword);
            promotionService.applyActivePromotions(vehicles);
            req.setAttribute("vehicles", vehicles);
            req.setAttribute("sortBy", sortBy);
            req.setAttribute("keyword", keyword);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Could not load your saved vehicles.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/wishlist.jsp").forward(req, resp);
    }
}
