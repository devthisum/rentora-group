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
        } catch (com.rentora.exception.ValidationException ve) {
            req.getSession().setAttribute("errorMessage", ve.getMessage());
        } catch (Exception e) {
            // Never show raw SQL/driver messages to the admin.
            req.getSession().setAttribute("errorMessage", "Could not remove this vehicle. If it has booking history, archive it instead.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/vehicles");
    }
}
