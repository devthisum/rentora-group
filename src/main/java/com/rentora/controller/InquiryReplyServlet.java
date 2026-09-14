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

@WebServlet("/inquiry/reply")
public class InquiryReplyServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        InquiryService inquiryService = (InquiryService) req.getServletContext().getAttribute("inquiryService");

        long threadId = Long.parseLong(req.getParameter("threadId"));
        try {
            boolean isAdmin = "ADMIN".equals(user.getRoleName());
            inquiryService.reply(threadId, user.getUserId(), isAdmin, req.getParameter("message"));
        } catch (Exception e) {
            // fall through — redirect back regardless, the thread page will just show the unchanged conversation
        }
        resp.sendRedirect(req.getContextPath() + "/inquiry/thread?id=" + threadId);
    }
}
