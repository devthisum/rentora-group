package com.rentora.controller.auth;

import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");

        try {
            if (firstName == null || firstName.isBlank()) throw new ValidationException("First name is required.");
            if (lastName == null || lastName.isBlank()) throw new ValidationException("Last name is required.");

            String fullName = firstName.trim() + " " + lastName.trim();
            User created = authService.registerRenter(fullName, email, phone, password);
            req.setAttribute("successMessage", "Account created successfully. You may now log in.");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);

        } catch (ValidationException ve) {
            req.setAttribute("errorMessage", ve.getMessage());
            req.setAttribute("firstName", firstName);
            req.setAttribute("lastName", lastName);
            req.setAttribute("email", email);
            req.setAttribute("phone", phone);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Something went wrong. Please try again.");
            req.setAttribute("firstName", firstName);
            req.setAttribute("lastName", lastName);
            req.setAttribute("email", email);
            req.setAttribute("phone", phone);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        }
    }
}
