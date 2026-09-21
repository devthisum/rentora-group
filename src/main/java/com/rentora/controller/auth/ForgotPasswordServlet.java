package com.rentora.controller.auth;

import com.rentora.service.PasswordResetService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Step 1 of forgot-password: renter enters their email, we email them a 6-digit OTP. */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final PasswordResetService passwordResetService = new PasswordResetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        try {
            passwordResetService.requestOtp(email);
            // Always show the same success message whether or not the email exists,
            // so the form can't be used to check which emails are registered.
            resp.sendRedirect(req.getContextPath() + "/reset-password?email=" + java.net.URLEncoder.encode(email, "UTF-8"));
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Something went wrong. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
        }
    }
}
