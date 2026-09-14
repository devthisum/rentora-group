package com.rentora.controller;

import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.service.AuthService;
import com.rentora.util.ImageUploadUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;

/**
 * "My Profile" — available to any logged-in user (renter, admin, or maintenance
 * staff) to edit their own name/phone/photo and change their password.
 */
@WebServlet("/profile")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB
public class ProfileServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/common/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User current = (User) session.getAttribute("user");
        String formType = req.getParameter("formType"); // "details" or "password"

        try {
            if ("password".equals(formType)) {
                authService.changePassword(current.getUserId(),
                        req.getParameter("currentPassword"), req.getParameter("newPassword"));
                session.setAttribute("successMessage", "Password updated.");
            } else {
                Part filePart = req.getPart("profileImageFile");
                String uploadedUrl = ImageUploadUtil.saveIfPresent(filePart, getServletContext(), req.getContextPath(), "profiles");
                String finalImage = uploadedUrl != null ? uploadedUrl : current.getProfileImage();

                User updated = authService.updateProfile(current.getUserId(),
                        req.getParameter("fullName"), req.getParameter("phone"), finalImage);

                // Keep the session copy in sync so the navbar avatar/name update immediately.
                session.setAttribute("user", updated);
                session.setAttribute("successMessage", "Profile updated.");
            }
        } catch (ValidationException ve) {
            session.setAttribute("errorMessage", ve.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Could not save your changes.");
        }
        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}
