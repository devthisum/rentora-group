package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.service.InquiryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/renter/inquiries")
public class RenterInquiriesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        InquiryService inquiryService = (InquiryService) req.getServletContext().getAttribute("inquiryService");
        try {
            req.setAttribute("threads", inquiryService.getThreadsForRenter(renter.getUserId()));
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load your conversations.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/inquiries.jsp").forward(req, resp);
    }
}
