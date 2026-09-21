package com.rentora.controller.auth;

import com.rentora.exception.ValidationException;
import com.rentora.service.PasswordResetService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Step 2 of forgot-password: renter enters the OTP they received + a new password. */
@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final PasswordResetService passwordResetService = new PasswordResetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("email", req.getParameter("email"));
        req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String otpCode = req.getParameter("otpCode");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            if (newPassword == null || !newPassword.equals(confirmPassword)) {
                throw new ValidationException("Passwords do not match.");
            }
            passwordResetService.verifyOtpAndReset(email, otpCode, newPassword);

            req.setAttribute("successMessage", "Password reset successfully. Please log in with your new password.");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        } catch (ValidationException ve) {
            req.setAttribute("errorMessage", ve.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Something went wrong. Please try again.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
        }
    }
}
