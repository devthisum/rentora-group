package com.rentora.controller.admin;

import com.rentora.model.User;
import com.rentora.service.AnnouncementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;

/** Full CRUD for platform-wide announcements, broadcast to every logged-in user. */
@WebServlet("/admin/announcements")
public class AdminAnnouncementServlet extends HttpServlet {

    private final AnnouncementService announcementService = new AnnouncementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("announcements", announcementService.getAllForAdmin());
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load announcements.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/announcements.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User admin = (User) session.getAttribute("user");
        String action = req.getParameter("action");

        try {
            switch (action == null ? "" : action) {
                case "create" -> {
                    LocalDate expiry = parseExpiry(req.getParameter("expiryDate"));
                    announcementService.create(admin.getUserId(), req.getParameter("title"),
                            req.getParameter("message"), req.getParameter("priority"),
                            req.getParameter("category"), expiry);
                }
                case "update" -> {
                    long id = Long.parseLong(req.getParameter("announcementId"));
                    LocalDate expiry = parseExpiry(req.getParameter("expiryDate"));
                    announcementService.update(id, req.getParameter("title"), req.getParameter("message"),
                            req.getParameter("priority"), req.getParameter("category"), expiry);
                }
                case "delete" -> {
                    long id = Long.parseLong(req.getParameter("announcementId"));
                    announcementService.delete(id);
                }
                default -> { /* no-op */ }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/announcements");

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/admin/announcements?error=1");
        }
    }

    private LocalDate parseExpiry(String param) {
        return (param != null && !param.isBlank()) ? LocalDate.parse(param) : null;
    }
}
