package com.rentora.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Renders the "Add Vehicle" form. Actual creation is posted to /admin/vehicles. */
@WebServlet("/admin/vehicles/add")
public class AdminAddVehicleFormServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/admin/add-vehicle.jsp").forward(req, resp);
    }
}
