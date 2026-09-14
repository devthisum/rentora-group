package com.rentora.controller;

import com.rentora.model.User;
import com.rentora.service.InquiryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Shared conversation view for both renters and owners — permission is
 * enforced in InquiryService.getThreadDetail (only the two participants
 * may view a thread). Reachable at the top level since it's not role-prefixed.
 */
@WebServlet("/inquiry/thread")
public class InquiryThreadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        InquiryService inquiryService = (InquiryService) req.getServletContext().getAttribute("inquiryService");

        try {
            long threadId = Long.parseLong(req.getParameter("id"));
            boolean isAdmin = "ADMIN".equals(user.getRoleName());
            req.setAttribute("thread", inquiryService.getThreadDetail(threadId, user.getUserId(), isAdmin));
            req.setAttribute("messages", inquiryService.getMessages(threadId));
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Could not load this conversation.");
        }
        req.getRequestDispatcher("/WEB-INF/views/common/inquiry-thread.jsp").forward(req, resp);
    }
}
