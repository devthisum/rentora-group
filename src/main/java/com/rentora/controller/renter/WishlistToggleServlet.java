package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.service.WishlistService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/** AJAX endpoint for the heart-icon toggle on vehicle cards — adds/removes and returns the new state as plain text. */
@WebServlet("/renter/wishlist/toggle")
public class WishlistToggleServlet extends HttpServlet {

    private final WishlistService wishlistService = new WishlistService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("unauthorized");
            return;
        }
        User user = (User) session.getAttribute("user");
        if (!"RENTER".equalsIgnoreCase(user.getRoleName())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("forbidden");
            return;
        }

        try {
            long vehicleId = Long.parseLong(req.getParameter("vehicleId"));
            boolean nowFavorited = wishlistService.toggle(user.getUserId(), vehicleId);
            out.print(nowFavorited ? "added" : "removed");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("error");
        }
    }
}
