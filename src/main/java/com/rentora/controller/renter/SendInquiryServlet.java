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

/** Renter asks a question about a vehicle from the vehicle-details page — starts or continues a thread. */
@WebServlet("/renter/inquiry/send")
public class SendInquiryServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        InquiryService inquiryService = (InquiryService) req.getServletContext().getAttribute("inquiryService");

        try {
            long vehicleId = Long.parseLong(req.getParameter("vehicleId"));
            String message = req.getParameter("message");

            long threadId = inquiryService.startOrContinueInquiry(renter.getUserId(), vehicleId, message);
            resp.sendRedirect(req.getContextPath() + "/inquiry/thread?id=" + threadId);

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/vehicle-details?id=" + req.getParameter("vehicleId") + "&error=1");
        }
    }
}
