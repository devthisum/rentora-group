package com.rentora.controller.admin;

import com.rentora.service.VehicleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/vehicles/delete")
public class AdminDeleteVehicleServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = Long.parseLong(req.getParameter("vehicleId"));
            vehicleService.deleteVehicle(id);
            req.getSession().setAttribute("successMessage", "Vehicle removed from stock.");
        } catch (Exception e) {
            req.getSession().setAttribute("errorMessage",
                    e.getMessage() != null ? e.getMessage() : "Could not remove this vehicle.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/vehicles");
    }
}
