package com.rentora.controller.admin;

import com.rentora.service.PromotionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Admin edits/removes vehicle promotions here. Creation happens at
 * /booking/promotions (Booking Staff's job) — this servlet intentionally
 * does not support a "create" action, to keep that separation of duties.
 */
@WebServlet("/admin/promotions")
public class AdminPromotionServlet extends HttpServlet {

    private final PromotionService promotionService = new PromotionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("promotions", promotionService.getAll());
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Could not load promotions.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/promotions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        try {
            switch (action == null ? "" : action) {
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

                default -> { /* Promotions are created by Booking Staff at /booking/promotions, not here. */ }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/promotions");

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/admin/promotions?error=" +
                    java.net.URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Something went wrong.", "UTF-8"));
        }
    }
}
