package com.rentora.controller.admin;

import com.rentora.service.InquiryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** The shop's support inbox — every customer conversation, visible to all admins. */
@WebServlet("/admin/inquiries")
public class AdminInquiriesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InquiryService inquiryService = (InquiryService) req.getServletContext().getAttribute("inquiryService");
        try {
            req.setAttribute("threads", inquiryService.getAllThreads());
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load conversations.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/inquiries.jsp").forward(req, resp);
    }
}
