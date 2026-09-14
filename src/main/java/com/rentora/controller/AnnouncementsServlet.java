package com.rentora.controller;

import com.rentora.model.User;
import com.rentora.service.AnnouncementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Public (any logged-in role) read-only announcements board. */
@WebServlet("/announcements")
public class AnnouncementsServlet extends HttpServlet {

    private final AnnouncementService announcementService = new AnnouncementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        try {
            req.setAttribute("announcements", announcementService.getActiveForUser(user.getUserId()));
            announcementService.markAllRead(user.getUserId());
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load announcements.");
        }
        req.getRequestDispatcher("/WEB-INF/views/common/announcements.jsp").forward(req, resp);
    }
}
