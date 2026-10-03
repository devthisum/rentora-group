package com.rentora.controller.booking;

import com.rentora.model.User;
import com.rentora.service.PromotionService;
import com.rentora.service.VehicleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Full CRUD for vehicle promotions (price-drop deals), owned by Booking
 * Staff — they're the front desk staff who know which vehicles are sitting
 * idle and could use a push. Admin also has edit/delete access from
 * /admin/promotions for oversight, but Booking Staff can fully manage
 * their own promotions here: create, edit, and remove.
 */
@WebServlet("/booking/promotions")
public class BookingPromotionServlet extends HttpServlet {

    private final PromotionService promotionService = new PromotionService();
    private final VehicleService vehicleService = new VehicleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("promotions", promotionService.getAll());
            req.setAttribute("vehicles", vehicleService.getAllActive());
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Could not load promotions.");
        }
        req.getRequestDispatcher("/WEB-INF/views/booking/promotions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User staff = (User) session.getAttribute("user");
        String action = req.getParameter("action");

        try {
            switch (action == null ? "" : action) {
                case "create" -> sharedPromotionService().createPromotion(
                        Long.parseLong(req.getParameter("vehicleId")),
                        req.getParameter("title"),
                        req.getParameter("description"),
                        req.getParameter("discountType"),
                        new BigDecimal(req.getParameter("discountValue")),
                        LocalDate.parse(req.getParameter("startDate")),
                        LocalDate.parse(req.getParameter("endDate")),
                        staff.getUserId());

                case "update" -> promotionService.updatePromotion(
                        Long.parseLong(req.getParameter("promotionId")),
                        req.getParameter("title"),
                        req.getParameter("description"),
                        req.getParameter("discountType"),
                        new BigDecimal(req.getParameter("discountValue")),
                        LocalDate.parse(req.getParameter("startDate")),
                        LocalDate.parse(req.getParameter("endDate")),
                        req.getParameter("status"));

                case "delete" -> promotionService.deletePromotion(Long.parseLong(req.getParameter("promotionId")));

                default -> { /* no-op */ }
            }
            resp.sendRedirect(req.getContextPath() + "/booking/promotions");

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/booking/promotions?error=" +
                    java.net.URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Something went wrong.", "UTF-8"));
        }
    }

    /** The app-wide PromotionService wired to the Observer subject, so creating a deal alerts wishlisters. */
    private PromotionService sharedPromotionService() {
        PromotionService shared = (PromotionService) getServletContext().getAttribute("promotionService");
        return shared != null ? shared : promotionService;
    }
}
